package ui.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.service.ReviewService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import ui.App;
import ui.Navigation;
import ui.client.InMemoryReviewRepository;
import ui.session.UserSession;

/**
 * Tester siden for passordbytte med en falsk autentiseringstjeneste, uten REST-server.
 */
class ChangePasswordControllerTest extends ApplicationTest {

    private Stage stage;
    private UserSession userSession;
    private RecordingAuthenticationClient authenticationClient;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        userSession = new UserSession();
        userSession.signIn("test-token", "Kari Nordmann", "kari@stud.ntnu.no");
        authenticationClient = new RecordingAuthenticationClient();

        InMemoryReviewRepository repository = new InMemoryReviewRepository();
        Navigation navigation = new Navigation(new ReviewService(repository), repository,
                userSession, authenticationClient);

        FXMLLoader loader = new FXMLLoader(App.class.getResource("ChangePassword.fxml"));
        final Parent root = loader.load();
        ChangePasswordController controller = loader.getController();
        controller.setAuthenticationClient(authenticationClient);
        controller.setNavigation(navigation);
        controller.setUserSession(userSession);

        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void emptyFieldsShowErrorWithoutContactingServer() {
        fill("Gammelt123", "", "");
        interact(() -> button("#changeButton").fire());

        assertEquals("Fyll ut alle feltene.", statusLabel().getText());
        assertEquals(0, authenticationClient.changePasswordCalls.get());
    }

    @Test
    void differentNewPasswordsShowError() {
        fill("Gammelt123", "Nytt12345", "Nytt12346");
        interact(() -> button("#changeButton").fire());

        assertEquals("Det nye passordet er ikke likt i begge feltene.",
                statusLabel().getText());
        assertEquals(0, authenticationClient.changePasswordCalls.get());
    }

    @Test
    void tooShortNewPasswordShowsError() {
        fill("Gammelt123", "kort", "kort");
        interact(() -> button("#changeButton").fire());

        assertEquals("Det nye passordet må ha minst 8 tegn.", statusLabel().getText());
        assertEquals(0, authenticationClient.changePasswordCalls.get());
    }

    @Test
    void sameOldAndNewPasswordShowsError() {
        fill("Gammelt123", "Gammelt123", "Gammelt123");
        interact(() -> button("#changeButton").fire());

        assertEquals("Det nye passordet må være forskjellig fra det gamle.",
                statusLabel().getText());
        assertEquals(0, authenticationClient.changePasswordCalls.get());
    }

    @Test
    void successfulChangeSendsTokenAndShowsConfirmation() throws Exception {
        fill("Gammelt123", "Nytt12345", "Nytt12345");
        interact(() -> button("#changeButton").fire());
        awaitStatus("Passordet er endret.");

        assertEquals(1, authenticationClient.changePasswordCalls.get());
        assertEquals("test-token", authenticationClient.receivedToken);
        assertEquals("kari@stud.ntnu.no", authenticationClient.receivedEmail);
        assertEquals("Gammelt123", authenticationClient.receivedOldPassword);
        assertEquals("Nytt12345", authenticationClient.receivedNewPassword);
        assertEquals("", passwordField("#oldPasswordField").getText());
        assertEquals("", passwordField("#newPasswordField").getText());
        assertEquals("", passwordField("#repeatPasswordField").getText());
    }

    @Test
    void wrongOldPasswordShowsServerMessageAndKeepsFields() throws Exception {
        authenticationClient.error = new IllegalArgumentException("Det gamle passordet er feil.");

        fill("Feil12345", "Nytt12345", "Nytt12345");
        interact(() -> button("#changeButton").fire());
        awaitStatus("Det gamle passordet er feil.");

        assertEquals("Feil12345", passwordField("#oldPasswordField").getText());
        assertEquals("Nytt12345", passwordField("#newPasswordField").getText());
        assertFalse(button("#changeButton").isDisabled());
    }

    @Test
    void networkFailureGivesNoFalseConfirmation() throws Exception {
        authenticationClient.error = new UncheckedIOException(
                new IOException("Ingen kontakt med serveren."));

        fill("Gammelt123", "Nytt12345", "Nytt12345");
        interact(() -> button("#changeButton").fire());
        awaitStatus("Kunne ikke bekrefte at passordet ble endret. Prøv igjen senere.");

        assertEquals("Gammelt123", passwordField("#oldPasswordField").getText());
        assertEquals(1, authenticationClient.changePasswordCalls.get());
    }

    @Test
    void disablesFormWhileChangeIsRunning() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        authenticationClient.requestStarted = started;
        authenticationClient.releaseRequest = release;

        fill("Gammelt123", "Nytt12345", "Nytt12345");
        try {
            interact(() -> button("#changeButton").fire());
            assertTrue(started.await(5, TimeUnit.SECONDS));

            interact(() -> {
                assertTrue(button("#changeButton").isDisabled());
                assertTrue(passwordField("#oldPasswordField").isDisabled());
                assertEquals("Bytter passord ...", statusLabel().getText());
            });
        } finally {
            release.countDown();
        }

        awaitStatus("Passordet er endret.");
        assertEquals(1, authenticationClient.changePasswordCalls.get());
    }

    @Test
    void backOpensProfileAndHomeOpensHomePage() {
        interact(() -> button("#backButton").fire());
        assertNotNull(stage.getScene().getRoot().lookup("#logoutButton"));

        interact(() -> button("#changePasswordButton").fire());
        interact(() -> button("#homeButton").fire());
        assertNotNull(stage.getScene().getRoot().lookup("#writeReviewButton"));
    }

    /**
     * Fyller inn de tre passordfeltene.
     */
    private void fill(String oldPassword, String newPassword, String repeatedPassword) {
        interact(() -> {
            passwordField("#oldPasswordField").setText(oldPassword);
            passwordField("#newPasswordField").setText(newPassword);
            passwordField("#repeatPasswordField").setText(repeatedPassword);
        });
    }

    /**
     * Venter til en bestemt statusmelding vises, fordi passordbyttet skjer i bakgrunnen.
     */
    private void awaitStatus(String message) throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> WaitForAsyncUtils
                .asyncFx(() -> message.equals(statusLabel().getText())).get(5, TimeUnit.SECONDS));
    }

    private PasswordField passwordField(String selector) {
        return (PasswordField) stage.getScene().getRoot().lookup(selector);
    }

    private Button button(String selector) {
        return (Button) stage.getScene().getRoot().lookup(selector);
    }

    private Label statusLabel() {
        return (Label) stage.getScene().getRoot().lookup("#statusLabel");
    }
}
