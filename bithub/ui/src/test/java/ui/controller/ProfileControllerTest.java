package ui.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.service.ReviewService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import ui.App;
import ui.Navigation;
import ui.client.InMemoryReviewRepository;
import ui.client.RemoteReviewRepository;
import ui.session.UserSession;

/**
 * Tester profilsiden med en falsk autentiseringstjeneste, uten REST- server.
 */
class ProfileControllerTest extends ApplicationTest {

    private Stage stage;
    private UserSession userSession;
    private RecordingAuthenticationClient authenticationClient;
    private Navigation navigation;

    /**
     * Tokenet sesjonen sist ga videre, slik {@link RemoteReviewRepository} ville fått det.
     */
    private final AtomicReference<String> forwardedToken = new AtomicReference<>();

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        userSession = new UserSession(forwardedToken::set);
        userSession.signIn("test-token", "Kari Nordmann", "kari@stud.ntnu.no");
        authenticationClient = new RecordingAuthenticationClient();

        InMemoryReviewRepository repository = new InMemoryReviewRepository();
        navigation = new Navigation(new ReviewService(repository), repository, userSession,
                authenticationClient);

        FXMLLoader loader = new FXMLLoader(App.class.getResource("Profile.fxml"));
        final Parent root = loader.load();
        ProfileController controller = loader.getController();
        controller.setAuthenticationClient(authenticationClient);
        controller.setNavigation(navigation);
        controller.setUserSession(userSession);

        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void showsNameAndEmailOfLoggedInUser() {
        assertEquals("Kari Nordmann", textField("#nameField").getText());
        assertEquals("kari@stud.ntnu.no", textField("#emailField").getText());
        assertFalse(textField("#nameField").isEditable());
        assertFalse(textField("#emailField").isEditable());
        assertFalse(button("#changePasswordButton").isDisabled());
        assertEquals("LOGG UT", button("#logoutButton").getText());
        assertTrue(button("#changeNameButton").isVisible());
        assertFalse(button("#saveNameButton").isVisible());
        assertFalse(button("#cancelNameButton").isVisible());
    }

    @Test
    void changeNameMakesNameFieldEditableWithSaveAndCancel() {
        interact(() -> button("#changeNameButton").fire());

        assertTrue(textField("#nameField").isEditable());
        assertFalse(button("#changeNameButton").isVisible());
        assertTrue(button("#saveNameButton").isVisible());
        assertTrue(button("#cancelNameButton").isVisible());
    }

    @Test
    void savingNameUpdatesServerAndSession() throws Exception {
        interact(() -> {
            button("#changeNameButton").fire();
            textField("#nameField").setText("  Kari Normann ");
            button("#saveNameButton").fire();
        });
        awaitStatus("Navnet er endret.");

        assertEquals(1, authenticationClient.changeNameCalls.get());
        assertEquals("test-token", authenticationClient.receivedToken);
        assertEquals("kari@stud.ntnu.no", authenticationClient.receivedEmail);
        assertEquals("Kari Normann", authenticationClient.receivedName);
        assertEquals("Kari Normann", userSession.getName());
        assertEquals("Kari Normann", textField("#nameField").getText());
        assertFalse(textField("#nameField").isEditable());
        assertTrue(button("#changeNameButton").isVisible());
        assertEquals("test-token", userSession.getToken());
    }

    @Test
    void cancelRestoresNameWithoutCallingServer() {
        interact(() -> {
            button("#changeNameButton").fire();
            textField("#nameField").setText("Noe Annet");
            button("#cancelNameButton").fire();
        });

        assertEquals("Kari Nordmann", textField("#nameField").getText());
        assertFalse(textField("#nameField").isEditable());
        assertEquals(0, authenticationClient.changeNameCalls.get());
        assertEquals("Kari Nordmann", userSession.getName());
    }

    @Test
    void emptyOrUnchangedNameIsNotSentToServer() {
        interact(() -> {
            button("#changeNameButton").fire();
            textField("#nameField").setText("   ");
            button("#saveNameButton").fire();
        });
        assertEquals("Navnet kan ikke være tomt.", statusLabel().getText());
        assertTrue(textField("#nameField").isEditable());

        interact(() -> {
            textField("#nameField").setText("Kari Nordmann");
            button("#saveNameButton").fire();
        });
        assertFalse(textField("#nameField").isEditable());
        assertEquals(0, authenticationClient.changeNameCalls.get());
    }

    @Test
    void rejectedNameShowsMessageAndKeepsOldName() throws Exception {
        authenticationClient.error =
                new IllegalArgumentException("Navn må være mellom 3 og 30 tegn.");

        interact(() -> {
            button("#changeNameButton").fire();
            textField("#nameField").setText("Ka");
            button("#saveNameButton").fire();
        });
        awaitStatus("Navn må være mellom 3 og 30 tegn.");

        assertEquals("Kari Nordmann", userSession.getName());
        assertTrue(textField("#nameField").isEditable());
        assertEquals("Ka", textField("#nameField").getText());
    }

    @Test
    void serverErrorShowsGeneralMessage() throws Exception {
        authenticationClient.error = new UncheckedIOException(
                new IOException("Ingen kontakt med serveren."));

        interact(() -> {
            button("#changeNameButton").fire();
            textField("#nameField").setText("Kari Normann");
            button("#saveNameButton").fire();
        });
        awaitStatus("Kunne ikke lagre navnet. Prøv igjen senere.");

        assertEquals("Kari Nordmann", userSession.getName());
    }

    @Test
    void buttonsAreDisabledWhileNameIsSaved() throws Exception {
        authenticationClient.requestStarted = new CountDownLatch(1);
        authenticationClient.releaseRequest = new CountDownLatch(1);

        interact(() -> {
            button("#changeNameButton").fire();
            textField("#nameField").setText("Kari Normann");
            button("#saveNameButton").fire();
        });
        assertTrue(authenticationClient.requestStarted.await(5, TimeUnit.SECONDS));

        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(button("#saveNameButton").isDisabled());
        assertTrue(button("#cancelNameButton").isDisabled());
        assertTrue(button("#logoutButton").isDisabled());
        assertTrue(button("#changePasswordButton").isDisabled());
        assertFalse(textField("#nameField").isEditable());

        authenticationClient.releaseRequest.countDown();
        awaitStatus("Navnet er endret.");
        assertFalse(button("#logoutButton").isDisabled());
    }

    @Test
    void logoutRemovesTokenOnServerAndInClient() throws Exception {
        interact(() -> button("#logoutButton").fire());
        awaitLogin();

        assertEquals(1, authenticationClient.logoutCalls.get());
        assertEquals("test-token", authenticationClient.receivedToken);
        assertFalse(userSession.isLoggedIn());
        assertNull(userSession.getToken());
        assertNull(forwardedToken.get());
    }

    @Test
    void logoutSignsOutInClientEvenIfServerIsUnavailable() throws Exception {
        authenticationClient.error = new UncheckedIOException(
                new IOException("Ingen kontakt med serveren."));
        
        interact(() -> button("#logoutButton").fire());
        awaitLogin();

        assertEquals(1, authenticationClient.logoutCalls.get());
        assertFalse(userSession.isLoggedIn());
        assertNull(forwardedToken.get());
    }

    @Test
    void changePasswordButtonOpensPasswordPage() {
        interact(() -> button("#changePasswordButton").fire());

        assertNotNull(stage.getScene().getRoot().lookup("#oldPasswordField"));
    }

    @Test
    void profileWithoutLoginShowsMessageAndCannotChangePassword() {
        interact(() -> {
            userSession.clear();
            navigation.showProfile(button("#homeButton"));
        });

        assertEquals("", textField("#nameField").getText());
        assertEquals("Du er ikke logget inn.", statusLabel().getText());
        assertTrue(button("#changePasswordButton").isDisabled());
        assertTrue(button("#changeNameButton").isDisabled());
        assertEquals("LOGG INN", button("#logoutButton").getText());

        interact(() -> button("#logoutButton").fire());

        assertNotNull(stage.getScene().getRoot().lookup("#loginButton"));
        assertEquals(0, authenticationClient.logoutCalls.get());
    }

    @Test
    void backAndHomeOpenHomePage() {
        interact(() -> button("#backButton").fire());
        assertNotNull(stage.getScene().getRoot().lookup("#writeReviewButton"));

        interact(() -> navigation.showProfile(button("#profileButton")));
        interact(() -> button("#homeButton").fire());
        assertNotNull(stage.getScene().getRoot().lookup("#writeReviewButton"));
    }

    /**
     * Venter til innloggingsskjermen har erstattet profilen.
     */
    private void awaitLogin() throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> WaitForAsyncUtils.asyncFx(
                        () -> stage.getScene().getRoot().lookup("#loginButton") != null)
                        .get(5, TimeUnit.SECONDS));
    }

    /**
     * Venter til statusfeltet viser den forventede meldingen.
     */
    private void awaitStatus(String expected) throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> WaitForAsyncUtils.asyncFx(() -> expected.equals(statusLabel().getText()))
                        .get(5, TimeUnit.SECONDS));
    }

    private TextField textField(String selector) {
        return (TextField) stage.getScene().getRoot().lookup(selector);
    }

    private Button button(String selector) {
        return (Button) stage.getScene().getRoot().lookup(selector);
    }

    private Label statusLabel() {
        return (Label) stage.getScene().getRoot().lookup("#statusLabel");
    }
}