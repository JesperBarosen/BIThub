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
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
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

    // Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
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

    /**
     * Sjekker at hvert passordfelt kreves, selv om de to andre feltene er fylt ut.
     *
     * @param fieldId feltet som tømmes
     */
    @ParameterizedTest
    @ValueSource(strings = { "#oldPasswordField", "#newPasswordField", "#repeatPasswordField" })
    void rejectsChangeWhenAnySinglePasswordFieldIsMissing(String fieldId) {
        fill("Gammelt123", "Nytt12345", "Nytt12345");

        interact(() -> {
            passwordField(fieldId).clear();
            button("#changeButton").fire();
        });

        assertEquals("Fyll ut alle feltene.", statusLabel().getText());
        assertEquals(0, authenticationClient.changePasswordCalls.get());
    }

    /**
     * Sjekker at en avsluttet innlogging stopper passordbyttet før serveren kontaktes.
     */
    @Test
    void loggedOutUserCannotChangePassword() {
        fill("Gammelt123", "Nytt12345", "Nytt12345");

        interact(() -> {
            userSession.clear();
            button("#changeButton").fire();
        });

        assertEquals("Du må være logget inn for å bytte passord.", statusLabel().getText());
        assertEquals(0, authenticationClient.changePasswordCalls.get());
        assertFalse(userSession.isLoggedIn());
    }

    @Test
    void differentNewPasswordsShowError() {
        fill("Gammelt123", "Nytt12345", "Nytt12346");
        interact(() -> button("#changeButton").fire());

        assertEquals("Det nye passordet er ikke likt i begge feltene.", statusLabel().getText());
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
    void showPasswordShowsAllThreePasswordFields() {
        fill("Gammelt123", "Nytt12345", "Nytt12345");

        interact(() -> checkBox("#showPasswordCheckBox").fire());

        assertFalse(passwordField("#oldPasswordField").isVisible());
        assertTrue(textField("#shownOldPasswordField").isVisible());
        assertEquals("Gammelt123", textField("#shownOldPasswordField").getText());
        assertEquals("Nytt12345", textField("#shownNewPasswordField").getText());
        assertEquals("Nytt12345", textField("#shownRepeatPasswordField").getText());

        interact(() -> checkBox("#showPasswordCheckBox").fire());

        assertTrue(passwordField("#oldPasswordField").isVisible());
        assertTrue(passwordField("#newPasswordField").isVisible());
        assertTrue(passwordField("#repeatPasswordField").isVisible());
        assertFalse(textField("#shownNewPasswordField").isVisible());
    }

    @Test
    void changeWorksWhilePasswordIsShown() throws Exception {
        interact(() -> {
            checkBox("#showPasswordCheckBox").fire();
            textField("#shownOldPasswordField").setText("Gammelt123");
            textField("#shownNewPasswordField").setText("Nytt12345");
            textField("#shownRepeatPasswordField").setText("Nytt12345");
            button("#changeButton").fire();
        });
        awaitStatus("Passordet er endret.");

        assertEquals("Gammelt123", authenticationClient.receivedOldPassword);
        assertEquals("Nytt12345", authenticationClient.receivedNewPassword);
        assertEquals("", textField("#shownNewPasswordField").getText());
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

    /**
     * Sjekker at feil uten brukbar melding gir generell feil uten å tømme passordfeltene.
     *
     * @param message feilmeldingen fra tjenesten
     * @throws Exception hvis ventingen på feilmeldingen feiler
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    void rejectionWithoutMessageKeepsPasswordFields(String message) throws Exception {
        authenticationClient.error = new IllegalArgumentException(message);
        fill("Gammelt123", "Nytt12345", "Nytt12345");

        interact(() -> button("#changeButton").fire());

        awaitStatus("Kunne ikke bekrefte at passordet ble endret. Prøv igjen senere.");
        assertEquals("Gammelt123", passwordField("#oldPasswordField").getText());
        assertEquals("Nytt12345", passwordField("#newPasswordField").getText());
        assertEquals("Nytt12345", passwordField("#repeatPasswordField").getText());
        assertFalse(button("#changeButton").isDisabled());
        assertEquals(1, authenticationClient.changePasswordCalls.get());
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

    // Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
    /**
     * Sjekker at en ekstra handling ikke sender et nytt passordbytte mens første kall venter.
     *
     * @throws Exception hvis bakgrunnskallet ikke starter eller svaret ikke vises
     */
    @Test
    void repeatedPasswordChangeWhileRunningSendsOnlyOneRequest() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        authenticationClient.requestStarted = started;
        authenticationClient.releaseRequest = release;
        fill("Gammelt123", "Nytt12345", "Nytt12345");

        try {
            interact(() -> button("#changeButton").fire());
            assertTrue(started.await(5, TimeUnit.SECONDS));

            interact(() -> {
                // Simulerer en handling som var på vei da knappen ble deaktivert.
                button("#changeButton").getOnAction().handle(new ActionEvent());
                assertTrue(button("#changeButton").isDisabled());
                assertEquals(1, authenticationClient.changePasswordCalls.get());
            });
        } finally {
            release.countDown();
        }

        awaitStatus("Passordet er endret.");
        assertEquals(1, authenticationClient.changePasswordCalls.get());
        assertEquals("", passwordField("#oldPasswordField").getText());
        assertEquals("", passwordField("#newPasswordField").getText());
        assertEquals("", passwordField("#repeatPasswordField").getText());
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

    private TextField textField(String selector) {
        return (TextField) stage.getScene().getRoot().lookup(selector);
    }

    private CheckBox checkBox(String selector) {
        return (CheckBox) stage.getScene().getRoot().lookup(selector);
    }

    private Button button(String selector) {
        return (Button) stage.getScene().getRoot().lookup(selector);
    }

    private Label statusLabel() {
        return (Label) stage.getScene().getRoot().lookup("#statusLabel");
    }
}
