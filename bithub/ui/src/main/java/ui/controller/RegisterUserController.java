package ui.controller;

import java.util.Objects;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import ui.Navigation;
import ui.client.AuthenticationClient;
import ui.client.AuthenticationResult;
import ui.util.BackgroundTaskRunner;
import ui.util.PasswordVisibility;

// Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
/**
 * Håndterer registrering av nye brukere.
 *
 * <p>Registreringen utføres i bakgrunnen slik at JavaFX ikke blokkeres. Serveren logger brukeren
 * inn i det samme kallet, så appen går rett til hjemmesiden etter vellykket registrering. Navn,
 * e-post og passord valideres på serveren; her sjekkes bare at feltene er fylt ut og like.
 */
public final class RegisterUserController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField repeatEmailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField repeatPasswordField;

    @FXML
    private TextField shownPasswordField;

    @FXML
    private TextField shownRepeatPasswordField;

    @FXML
    private CheckBox showPasswordCheckBox;

    @FXML
    private Button registerButton;

    @FXML
    private Button backButton;

    @FXML
    private Label statusLabel;

    private AuthenticationClient authenticationClient;
    private Navigation navigation;
    private Consumer<AuthenticationResult> registrationSuccessHandler;

    private final BackgroundTaskRunner taskRunner = new BackgroundTaskRunner();

    /**
     * Binder skjemafeltene til lastetilstanden og kobler til «Vis passord».
     */
    @FXML
    private void initialize() {
        registerButton.disableProperty().bind(taskRunner.runningProperty());
        nameField.disableProperty().bind(taskRunner.runningProperty());
        emailField.disableProperty().bind(taskRunner.runningProperty());
        repeatEmailField.disableProperty().bind(taskRunner.runningProperty());
        passwordField.disableProperty().bind(taskRunner.runningProperty());
        repeatPasswordField.disableProperty().bind(taskRunner.runningProperty());
        PasswordVisibility.connect(showPasswordCheckBox, passwordField, shownPasswordField);
        PasswordVisibility.connect(showPasswordCheckBox, repeatPasswordField,
                shownRepeatPasswordField);

        statusLabel.setVisible(false);
        statusLabel.managedProperty().bind(statusLabel.visibleProperty());
    }

    /**
     * Setter tjenesten som utfører registreringen.
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
     * Setter handlingen som utføres etter vellykket registrering.
     *
     * @param registrationSuccessHandler handlingen som får token og brukeropplysninger
     */
    public void setRegistrationSuccessHandler(
            Consumer<AuthenticationResult> registrationSuccessHandler) {
        this.registrationSuccessHandler = Objects.requireNonNull(registrationSuccessHandler);
    }

    /**
     * Sjekker skjemaet og sender registreringen i bakgrunnen.
     */
    @FXML
    private void handleRegister() {
        if (taskRunner.runningProperty().get()) {
            return;
        }

        if (authenticationClient == null || registrationSuccessHandler == null
                || navigation == null) {
            throw new IllegalStateException("Registreringen er ikke konfigurert.");
        }

        String name = nameField.getText().strip();
        String email = emailField.getText().strip();
        String repeatedEmail = repeatEmailField.getText().strip();
        String password = passwordField.getText();
        String repeatedPassword = repeatPasswordField.getText();

        String formError = checkForm(name, email, repeatedEmail, password, repeatedPassword);
        if (formError != null) {
            showError(formError);
            return;
        }

        showStatus("Registrerer bruker ...", Color.BLACK);

        taskRunner.execute(() -> authenticationClient.register(name, email, password), result -> {
            registrationSuccessHandler.accept(result);
            passwordField.clear();
            repeatPasswordField.clear();
            navigation.showHome(registerButton);
        }, error -> {
            if (error instanceof IllegalArgumentException && error.getMessage() != null
                    && !error.getMessage().isBlank()) {
                showError(error.getMessage());
            } else {
                showError("Kunne ikke fullføre registreringen. Prøv igjen senere.");
            }
        });
    }

    /**
     * Avbryter eventuell registrering og går tilbake til innloggingen.
     */
    @FXML
    private void handleBack() {
        if (navigation == null) {
            throw new IllegalStateException("Navigasjonen er ikke konfigurert.");
        }

        taskRunner.cancel();
        navigation.showLogin(backButton);
    }

    /**
     * Sjekker at alle feltene er fylt ut, og at e-post og passord er skrevet likt to ganger.
     *
     * @return feilmeldingen som skal vises, eller null hvis skjemaet kan sendes
     */
    private static String checkForm(String name, String email, String repeatedEmail,
            String password, String repeatedPassword) {
        if (name.isBlank() || email.isBlank() || repeatedEmail.isBlank() || password.isBlank()
                || repeatedPassword.isBlank()) {
            return "Fyll ut alle feltene.";
        }
        if (!email.equalsIgnoreCase(repeatedEmail)) {
            return "E-postadressene er ikke like.";
        }
        if (!password.equals(repeatedPassword)) {
            return "Passordene er ikke like.";
        }
        return null;
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