package ui.controller;

import java.util.Objects;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.paint.Color;
import ui.Navigation;
import ui.client.AuthenticationClient;
import ui.session.UserSession;
import ui.util.BackgroundTaskRunner;

/**
 * Lar den innloggede brukeren bytte passord etter å ha skrevet inn det gamle passordet.
 * 
 * <p>Passordbyttet sendes til serveren i bakgrunnen med {@link BackgroundTaskRunner} etter
 * mønsteret i {@code docs/release3/javafx.md}. Bekreftelsen vises bare når serveren har bekreftet
 * byttet. Ved feil beholdes det brukeren har skrevet inn.
 */
public final class ChangePasswordController {

    /**
     * Samme minstelengde som {@code core.model.User} krever, slik at feilmeldingen kan vises på
     * norsk.
     */
    private static final int MIN_PASSWORD_LENGTH = 8;

    @FXML
    private PasswordField oldPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField repeatPasswordField;

    @FXML
    private Button changeButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Button backButton;

    @FXML
    private Button homeButton;

    private Navigation navigation;
    private UserSession userSession;
    private AuthenticationClient authenticationClient;

    private final BackgroundTaskRunner taskRunner = new BackgroundTaskRunner();

    /**
     * Deaktiverer skjemaet mens passordbyttet sendes.
     */
    @FXML
    private void initialize() {
        changeButton.disableProperty().bind(taskRunner.runningProperty());
        oldPasswordField.disableProperty().bind(taskRunner.runningProperty());
        newPasswordField.disableProperty().bind(taskRunner.runningProperty());
        repeatPasswordField.disableProperty().bind(taskRunner.runningProperty());
    }

    /**
     * Setter navigasjonen som kontrolleren skal bruke.
     *
     * @param navigation navigasjonsobjektet
     * @throws NullPointerException kastes hvis navigation er null
     */
    public void setNavigation(Navigation navigation) {
        this.navigation = Objects.requireNonNull(navigation);
    }

    /**
     * Setter sesjonen med tokenet og e-posten som brukes ved passordbyttet.
     *
     * @param userSession sesjonen som er felles for alle skjermene
     * @throws NullPointerException kastes hvis userSession er null
     */
    public void setUserSession(UserSession userSession) {
        this.userSession = Objects.requireNonNull(userSession);
    }

    /**
     * Setter tjenesten som sender passordbyttet til serveren.
     *
     * @param authenticationClient tjenesten for autentisering
     * @throws NullPointerException kastes hvis authenticationClient er null
     */
    public void setAuthenticationClient(AuthenticationClient authenticationClient) {
        this.authenticationClient = Objects.requireNonNull(authenticationClient);
    }

    /**
     * Sjekker feltene og sender passordbyttet i bakgrunnen.
     *
     * <p>Klienten sjekker bare det som gir raskere tilbakemelding til brukeren. Serveren sjekker
     * det gamle passordet og validerer det nye på nytt.
     */
    @FXML
    private void handleChangePassword() {
        if (taskRunner.runningProperty().get()) {
            return;
        }

        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String repeatedPassword = repeatPasswordField.getText();

        if (oldPassword.isBlank() || newPassword.isBlank() || repeatedPassword.isBlank()) {
            showError("Fyll ut alle feltene.");
            return;
        }
        if (!newPassword.equals(repeatedPassword)) {
            showError("Det nye passordet er ikke likt i begge feltene.");
            return;
        }
        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            showError("Det nye passordet må ha minst " + MIN_PASSWORD_LENGTH + " tegn.");
            return;
        }
        if (newPassword.equals(oldPassword)) {
            showError("Det nye passordet må være forskjellig fra det gamle.");
            return;
        }
        if (userSession == null || !userSession.isLoggedIn() || authenticationClient == null) {
            showError("Du må være logget inn for å bytte passord.");
            return;
        }

        String token = userSession.getToken();
        String email = userSession.getEmail();

        showStatus("Bytter passord ...", Color.BLACK);
        taskRunner.execute(() -> {
            authenticationClient.changePassword(token, email, oldPassword, newPassword);
            return null;
        }, ignored -> {
            showStatus("Passordet er endret.", Color.GREEN);
            clearFields();
        }, error -> {
            if (error instanceof IllegalArgumentException && error.getMessage() != null
                    && !error.getMessage().isBlank()) {
                showError(error.getMessage());
            } else {
                showError("Kunne ikke bekrefte at passordet ble endret. Prøv igjen senere.");
            }
        });

    }

    /**
     * Avbryter ventingen og går tilbake til profilen.
     */
    @FXML
    private void handleBack() {
        taskRunner.cancel();
        navigation.showProfile(backButton);
    }

    /**
     * Avbryter ventingen og går til hjemmesiden.
     */
    @FXML
    private void handleHome() {
        taskRunner.cancel();
        navigation.showHome(homeButton);
    }

    /**
     * Viser en statusmelding i valgt farge.
     *
     * @param message meldingen som skal vises
     * @param color tekstfargen som meldingen skal vises i
     */
    private void showStatus(String message, Color color) {
        statusLabel.setTextFill(color);
        statusLabel.setText(message);
    }

    /**
     * Viser en feilmelding i rødt.
     *
     * @param message feilmeldingen som skal vises
     */
    private void showError(String message) {
        showStatus(message, Color.RED);
    }

    /**
     * Tømmer alle passordfeltene.
     */
    private void clearFields() {
        oldPasswordField.clear();
        newPasswordField.clear();
        repeatPasswordField.clear();
    }
}
