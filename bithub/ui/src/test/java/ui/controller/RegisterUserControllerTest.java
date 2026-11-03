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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import ui.Navigation;
import ui.client.AuthenticationClient;
import ui.client.AuthenticationResult;
import ui.client.InMemoryReviewRepository;
import ui.session.UserSession;

/**
 * Tester registreringsskjermen med en falsk autentiseringstjeneste.
 */
class RegisterUserControllerTest extends ApplicationTest {

    private Stage stage;
    private UserSession userSession;
    private FakeAuthenticationClient authenticationClient;
    private final AtomicReference<String> sentToken = new AtomicReference<>();

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        userSession = new UserSession(sentToken::set);
        authenticationClient = new FakeAuthenticationClient();

        InMemoryReviewRepository repository = new InMemoryReviewRepository();
        Navigation navigation = new Navigation(new ReviewService(repository), repository,
                userSession, authenticationClient);

        navigation.showLogin(stage);
        stage.show();
        button("#registerButton").fire();
    }

    @Test
    void showsErrorWhenFieldsAreMissing() {
        interact(() -> {
            textField("#nameField").setText("Kari Nordmann");
            button("#registerButton").fire();
        });

        assertEquals("Fyll ut alle feltene.", statusLabel().getText());
        assertTrue(statusLabel().isVisible());
        assertEquals(0, authenticationClient.registerCalls.get());
    }

    /**
     * Sjekker hvert obligatoriske felt separat, slik at de andre feltene fortsatt er gyldige.
     *
     * @param fieldId feltet som tømmes
     */
    @ParameterizedTest
    @ValueSource(strings = { "#nameField", "#emailField", "#repeatEmailField", "#passwordField",
        "#repeatPasswordField" })
    void showsErrorWhenAnySingleFieldIsMissing(String fieldId) {
        fillValidForm();

        interact(() -> {
            textField(fieldId).clear();
            button("#registerButton").fire();
        });

        assertEquals("Fyll ut alle feltene.", statusLabel().getText());
        assertTrue(statusLabel().isVisible());
        assertEquals(0, authenticationClient.registerCalls.get());
    }

    @Test
    void showsErrorWhenEmailsDiffer() {
        fillForm("kari@ntnu.no", "kari@stud.ntnu.no", "Hemmelig123", "Hemmelig123");

        interact(() -> button("#registerButton").fire());

        assertEquals("E-postadressene er ikke like.", statusLabel().getText());
        assertEquals(0, authenticationClient.registerCalls.get());
    }

    @Test
    void showsErrorWhenPasswordsDiffer() {
        fillForm("kari@ntnu.no", "kari@ntnu.no", "Hemmelig123", "Hemmelig124");

        interact(() -> button("#registerButton").fire());

        assertEquals("Passordene er ikke like.", statusLabel().getText());
        assertEquals(0, authenticationClient.registerCalls.get());
    }

    @Test
    void showPasswordShowsBothPasswordFields() {
        fillValidForm();

        interact(() -> checkBox("#showPasswordCheckBox").fire());

        assertFalse(passwordField("#passwordField").isVisible());
        assertFalse(passwordField("#repeatPasswordField").isVisible());
        assertTrue(textField("#shownPasswordField").isVisible());
        assertTrue(textField("#shownRepeatPasswordField").isVisible());
        assertEquals("Hemmelig123", textField("#shownPasswordField").getText());
        assertEquals("Hemmelig123", textField("#shownRepeatPasswordField").getText());

        interact(() -> checkBox("#showPasswordCheckBox").fire());

        assertTrue(passwordField("#passwordField").isVisible());
        assertTrue(passwordField("#repeatPasswordField").isVisible());
        assertFalse(textField("#shownPasswordField").isVisible());
        assertFalse(textField("#shownRepeatPasswordField").isVisible());
    }

    @Test
    void registrationWorksWhilePasswordIsShown() throws Exception {
        interact(() -> {
            checkBox("#showPasswordCheckBox").fire();
            textField("#nameField").setText("Kari Nordmann");
            textField("#emailField").setText("kari@ntnu.no");
            textField("#repeatEmailField").setText("kari@ntnu.no");
            textField("#shownPasswordField").setText("Hemmelig123");
            textField("#shownRepeatPasswordField").setText("Hemmelig123");
            button("#registerButton").fire();
        });
        awaitHome();

        assertTrue(userSession.isLoggedIn());
        assertEquals("kari@ntnu.no", authenticationClient.lastEmail);
    }

    @Test
    void successfulRegistrationSignsInAndOpensHome() throws Exception {
        fillValidForm();

        interact(() -> button("#registerButton").fire());
        awaitHome();

        assertTrue(userSession.isLoggedIn());
        assertEquals("Kari Nordmann", userSession.getName());
        assertEquals("kari@ntnu.no", userSession.getEmail());
        assertEquals("test-token", sentToken.get());
        assertEquals("Kari Nordmann", authenticationClient.lastName);
        assertEquals("kari@ntnu.no", authenticationClient.lastEmail);
    }

    @Test
    void serverRejectionKeepsFormAndShowsMessage() throws Exception {
        authenticationClient.registerError = new IllegalArgumentException(
                "Det finnes allerede en bruker med denne e-posten.");
        fillValidForm();

        interact(() -> button("#registerButton").fire());
        awaitStatus("Det finnes allerede en bruker med denne e-posten.");

        assertFalse(userSession.isLoggedIn());
        assertEquals("kari@ntnu.no", textField("#emailField").getText());
        assertEquals("Hemmelig123", passwordField("#passwordField").getText());
        assertFalse(button("#registerButton").isDisabled());
    }

    @Test
    void networkFailureShowsGeneralMessage() throws Exception {
        authenticationClient.registerError = new UncheckedIOException(
                new IOException("Ingen kontakt med serveren."));
        fillValidForm();

        interact(() -> button("#registerButton").fire());

        awaitStatus("Kunne ikke fullføre registreringen. Prøv igjen senere.");
        assertFalse(userSession.isLoggedIn());
    }

    @Test
    void disablesFormWhileRegistrationIsRunning() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        authenticationClient.requestStarted = started;
        authenticationClient.releaseRequest = release;
        fillValidForm();

        try {
            interact(() -> button("#registerButton").fire());
            assertTrue(started.await(5, TimeUnit.SECONDS));

            interact(() -> {
                assertTrue(button("#registerButton").isDisabled());
                assertTrue(textField("#nameField").isDisabled());
                assertTrue(passwordField("#repeatPasswordField").isDisabled());
                assertEquals("Registrerer bruker ...", statusLabel().getText());
            });
        } finally {
            release.countDown();
        }

        awaitHome();
        assertEquals(1, authenticationClient.registerCalls.get());
    }

    @Test
    void backButtonOpensLoginScreen() {
        interact(() -> button("#backButton").fire());

        assertNotNull(stage.getScene().getRoot().lookup("#loginButton"));
        assertFalse(userSession.isLoggedIn());
    }

    /**
     * Fyller inn et gyldig skjema.
     */
    private void fillValidForm() {
        fillForm("kari@ntnu.no", "kari@ntnu.no", "Hemmelig123", "Hemmelig123");
    }

    /**
     * Fyller inn skjemaet med navnet Kari Nordmann og de oppgitte verdiene.
     */
    private void fillForm(String email, String repeatedEmail, String password,
            String repeatedPassword) {
        interact(() -> {
            textField("#nameField").setText("Kari Nordmann");
            textField("#emailField").setText(email);
            textField("#repeatEmailField").setText(repeatedEmail);
            passwordField("#passwordField").setText(password);
            passwordField("#repeatPasswordField").setText(repeatedPassword);
        });
    }

    /**
     * Venter til hjemmesiden har erstattet registreringsskjermen.
     */
    private void awaitHome() throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> WaitForAsyncUtils.asyncFx(
                        () -> stage.getScene().getRoot().lookup("#writeReviewButton") != null)
                        .get(5, TimeUnit.SECONDS));
    }

    /**
     * Venter til en bestemt statusmelding vises.
     */
    private void awaitStatus(String message) throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> WaitForAsyncUtils
                .asyncFx(() -> message.equals(statusLabel().getText())).get(5, TimeUnit.SECONDS));
    }

    private TextField textField(String selector) {
        return (TextField) stage.getScene().getRoot().lookup(selector);
    }

    private PasswordField passwordField(String selector) {
        return (PasswordField) stage.getScene().getRoot().lookup(selector);
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

    /**
     * Returnerer kontrollerbare svar uten å kontakte en REST-server.
     */
    private static final class FakeAuthenticationClient implements AuthenticationClient {

        private final AtomicInteger registerCalls = new AtomicInteger();
        private volatile RuntimeException registerError;
        private volatile CountDownLatch requestStarted;
        private volatile CountDownLatch releaseRequest;
        private volatile String lastName;
        private volatile String lastEmail;

        @Override
        public AuthenticationResult login(String email, String password) {
            throw new UnsupportedOperationException("Innlogging testes i LoginControllerTest.");
        }

        @Override
        public AuthenticationResult register(String name, String email, String password) {
            registerCalls.incrementAndGet();
            lastName = name;
            lastEmail = email;

            CountDownLatch started = requestStarted;
            if (started != null) {
                started.countDown();
            }

            CountDownLatch release = releaseRequest;
            if (release != null) {
                try {
                    if (!release.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Testforespørselen ble ikke frigitt.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Testforespørselen ble avbrutt.", e);
                }
            }

            RuntimeException error = registerError;
            if (error != null) {
                throw error;
            }

            return new AuthenticationResult("test-token", name, email);
        }
    }
}
