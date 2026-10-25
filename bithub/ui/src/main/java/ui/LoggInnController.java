package ui;

import java.util.Objects;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

/**
 * Håndterer innlogging og navigasjon til registreringsskjermen.
 *
 * <p>Innlogging utføres i bakgrunnen slik at JavaFX ikke blokkeres. Sesjonen oppdateres først når
 * serveren bekrefter innloggingen.
 */
public final class LoggInnController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Label statusLabel;

    private AuthenticationClient authenticationClient;
    private Navigation navigation;
    private Consumer<AuthenticationResult> loginSuccessHandler;

    private final BackgroundTaskRunner taskRunner = new BackgroundTaskRunner();

    /**
     * Binder skjemafeltene til lastetilstanden.
     */
    @FXML
    private void initialize() {
        loginButton.disableProperty().bind(taskRunner.runningProperty());
        emailField.disableProperty().bind(taskRunner.runningProperty());
        passwordField.disableProperty().bind(taskRunner.runningProperty());

        statusLabel.setVisible(false);
        statusLabel.managedProperty().bind(statusLabel.visibleProperty());
    }

    /**
     * Setter tjenesten som utfører innloggingen.
     *
     * @param authenticationClient tjenesten for autentisering
     */
    public void setAuthenticationClient(AuthenticationClient authenticationClient) {
        this.authenticationClient = Objects.requireNonNull(authenticationClient);
    }

    /**
     * Setter navigasjonen til de andre skjermene.
     *
     * @param navigation applikasjonens navigasjon
     */
    public void setNavigation(Navigation navigation) {
        this.navigation = Objects.requireNonNull(navigation);
    }

    /**
     * Setter handlingen som utføres etter vellykket innlogging.
     *
     * @param loginSuccessHandler handlingen ved vellykket innlogging
     */
    public void setLoginSuccessHandler(Consumer<AuthenticationResult> loginSuccessHandler) {
        this.loginSuccessHandler = Objects.requireNonNull(loginSuccessHandler);
    }

    /**
     * Validerer feltene og sender innloggingsforespørselen i bakgrunnen.
     */
    @FXML
    private void handleLogin() {
        if (taskRunner.runningProperty().get()) {
            return;
        }

        if (authenticationClient == null || loginSuccessHandler == null || navigation == null) {
            throw new IllegalStateException("Innloggingen er ikke konfigurert.");
        }

        String email = emailField.getText().strip();
        String password = passwordField.getText();

        if (email.isBlank() || password.isBlank()) {
            showError("Skriv inn både e-postadresse og passord.");
            return;
        }

        showStatus("Logger inn ...", Color.BLACK);

        taskRunner.execute(() -> authenticationClient.login(email, password), result -> {
            loginSuccessHandler.accept(result);
            passwordField.clear();
            navigation.showHome(loginButton);
        }, error -> {
            if (error instanceof IllegalArgumentException && error.getMessage() != null
                    && !error.getMessage().isBlank()) {
                showError(error.getMessage());
            } else {
                showError("Kunne ikke fullføre innloggingen. Prøv igjen senere.");
            }
        });
    }

    /**
     * Avbryter eventuell innlogging og åpner registreringsskjermen.
     */
    @FXML
    private void handleRegister() {
        if (navigation == null) {
            throw new IllegalStateException("Navigasjonen er ikke konfigurert.");
        }

        taskRunner.cancel();
        navigation.showRegistration(registerButton);
    }

    /**
     * Viser en statusmelding med valgt farge.
     *
     * @param message meldingen som skal vises
     * @param color   tekstfargen
     */
    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setTextFill(color);
        statusLabel.setVisible(true);
    }

    /**
     * Viser en feilmelding uten å slette skjemainnholdet.
     *
     * @param message feilmeldingen
     */
    private void showError(String message) {
        showStatus(message, Color.RED);
    }
}
