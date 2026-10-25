package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.ReviewService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Tester innloggingsskjermen med en falsk autentiseringstjeneste.
 */
class LoggInnControllerTest extends ApplicationTest {

    private Stage stage;
    private UserSession userSession;
    private FakeAuthenticationClient authenticationClient;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        userSession = new UserSession();
        authenticationClient = new FakeAuthenticationClient();

        InMemoryReviewRepository repository = new InMemoryReviewRepository();
        ReviewService reviewService = new ReviewService(repository);
        Navigation navigation = new Navigation(reviewService, repository);

        FXMLLoader loader = new FXMLLoader(App.class.getResource("LoggInn.fxml"));
        final Parent root = loader.load();

        LoggInnController controller = loader.getController();
        controller.setAuthenticationClient(authenticationClient);
        controller.setNavigation(navigation);
        controller.setLoginSuccessHandler(
                result -> userSession.signIn(result.token(), result.name(), result.email()));

        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void showsErrorWhenRequiredFieldsAreMissing() {
        interact(() -> {
            emailField().setText("kari@ntnu.no");
            button("#loginButton").fire();
        });

        assertEquals("Skriv inn både e-postadresse og passord.", statusLabel().getText());
        assertEquals(0, authenticationClient.loginCalls.get());
        assertTrue(statusLabel().isVisible());
    }

    @Test
    void successfulLoginStoresSessionAndOpensHome() throws Exception {
        enterCredentials();

        interact(() -> button("#loginButton").fire());
        awaitHome();

        assertEquals("test-token", userSession.getToken());
        assertEquals(1, authenticationClient.loginCalls.get());
    }

    @Test
    void invalidCredentialsKeepFormAndShowError() throws Exception {
        authenticationClient.loginError = new IllegalArgumentException(
                "Feil e-post eller passord.");

        enterCredentials();
        interact(() -> button("#loginButton").fire());

        awaitStatus("Feil e-post eller passord.");

        assertEquals("kari@ntnu.no", emailField().getText());
        assertEquals("FeilPassord123", passwordField().getText());
        assertFalse(button("#loginButton").isDisabled());
        assertEquals(1, authenticationClient.loginCalls.get());
    }

    @Test
    void networkFailureShowsErrorWithoutClearingForm() throws Exception {
        authenticationClient.loginError = new UncheckedIOException(
                new IOException("Ingen kontakt med serveren."));

        enterCredentials();
        interact(() -> button("#loginButton").fire());

        awaitStatus("Kunne ikke fullføre innloggingen. Prøv igjen senere.");

        assertEquals("kari@ntnu.no", emailField().getText());
        assertEquals("FeilPassord123", passwordField().getText());
        assertFalse(button("#loginButton").isDisabled());
    }

    @Test
    void disablesFormWhileLoginIsRunning() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        authenticationClient.requestStarted = started;
        authenticationClient.releaseRequest = release;

        enterCredentials();

        try {
            interact(() -> button("#loginButton").fire());
            assertTrue(started.await(5, TimeUnit.SECONDS));

            interact(() -> {
                assertTrue(button("#loginButton").isDisabled());
                assertTrue(emailField().isDisabled());
                assertTrue(passwordField().isDisabled());
                assertEquals("Logger inn ...", statusLabel().getText());
            });
        } finally {
            release.countDown();
        }

        awaitHome();
        assertEquals(1, authenticationClient.loginCalls.get());
    }

    @Test
    void registerButtonOpensRegistrationScreen() {
        Parent previousRoot = stage.getScene().getRoot();

        interact(() -> button("#registerButton").fire());

        assertNotSame(previousRoot, stage.getScene().getRoot());
        assertTrue(stage.getScene().getRoot().lookupAll(".label").stream()
                .filter(Label.class::isInstance).map(Label.class::cast)
                .anyMatch(label -> "REGISTRER NY BRUKER:".equals(label.getText())));
    }

    /**
     * Fyller inn gyldige testverdier i innloggingsskjemaet.
     */
    private void enterCredentials() {
        interact(() -> {
            emailField().setText("kari@ntnu.no");
            passwordField().setText("FeilPassord123");
        });
    }

    /**
     * Venter til hjemmesiden har erstattet innloggingsskjermen.
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

    private TextField emailField() {
        return (TextField) stage.getScene().getRoot().lookup("#emailField");
    }

    private PasswordField passwordField() {
        return (PasswordField) stage.getScene().getRoot().lookup("#passwordField");
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

        private final AtomicInteger loginCalls = new AtomicInteger();
        private volatile RuntimeException loginError;
        private volatile CountDownLatch requestStarted;
        private volatile CountDownLatch releaseRequest;

        @Override
        public AuthenticationResult login(String email, String password) {
            loginCalls.incrementAndGet();

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

            RuntimeException error = loginError;
            if (error != null) {
                throw error;
            }

            return new AuthenticationResult("test-token", "Kari Nordmann", "kari@ntnu.no");
        }

        @Override
        public AuthenticationResult register(String name, String email, String password) {
            throw new UnsupportedOperationException("Registrering testes i en egen kontroller.");
        }
    }
}
