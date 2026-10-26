package ui;

import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

/**
 * Viser profilen til den innloggede brukeren og lar brukeren gå til passordbytte eller logge ut.
 * 
 * <p>Navn og e- post hentes fra {@link UserSession} som fikk dem fra serveren ved innlogging.
 * Utlogging sendes til serveren i bakgrunnen med {@link BackgroundTaskRunner}.
 */
public final class ProfilController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private Button changePasswordButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Label statusLabel;

    @FXML
    private Button backButton;

    @FXML
    private Button homeButton;

    private Navigation navigation;
    private UserSession userSession;
    private AuthenticationClient authenticationClient;

    private final BooleanProperty loggedIn = new SimpleBooleanProperty(false);
    private final BackgroundTaskRunner taskRunner = new BackgroundTaskRunner();

    /**
     * Deaktiverer knappene mens utloggingen pågår. Passordbytte krever innlogging.
     */
    @FXML
    private void initialize() {
        logoutButton.disableProperty().bind(taskRunner.runningProperty());
        changePasswordButton.disableProperty()
                .bind(taskRunner.runningProperty().or(loggedIn.not()));
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
     * Setter sesjonen og viser navnet og e- posten til den innloggede brukeren.
     *
     * @param userSession sesjonen som er felles for alle skjermene
     * @throws NullPointerException kastes hvis userSession er null
     */
    public void setUserSession(UserSession userSession) {
        this.userSession = Objects.requireNonNull(userSession);
        showUser();
    }

    /**
     * Setter tjenesten som logger ut på serveren. Uten tjenesten fjernes bare innloggingen i
     * klienten.
     *
     * @param authenticationClient tjenesten for autentisering
     * @throws NullPointerException kastes hvis authenticationClient er null
     */
    public void setAuthenticationClient(AuthenticationClient authenticationClient) {
        this.authenticationClient = Objects.requireNonNull(authenticationClient);
    }

    /**
     * Viser brukeropplysningene fra sesjonen eller en melding hvis ingen er innlogget.
     */
    private void showUser() {
        boolean signedIn = userSession.isLoggedIn();
        loggedIn.set(signedIn);
        if (signedIn) {
            nameField.setText(userSession.getName());
            emailField.setText(userSession.getEmail());
            logoutButton.setText("LOGG UT");
            showStatus("", Color.BLACK);
        } else {
            nameField.clear();
            emailField.clear();
            logoutButton.setText("LOGG INN");
            showStatus("Du er ikke logget inn.", Color.BLACK);
        }
    }

    /**
     * Åpner siden for å bytte passord.
     */
    @FXML
    private void handleChangePassword() {
        if (taskRunner.runningProperty().get() || !loggedIn.get()) {
            return;
        }
        navigation.showChangePassword(changePasswordButton);
    }

    /**
     * Logger ut og viser innloggingsskjerm.
     * 
     * <p>Tokenet fjernes fra klienten med en gang slik at det ikke sendes med flere forespørsler.
     * Deretter ber klienten serveren å slette tokenet. Innloggingsskjermen vises også hvis serveren
     * ikke svarer fordi brukeren uansett er logget ut i klienten. Tokenene på serveren ligger i
     * minnet og forsvinner når serveren starter på nytt.
     */
    @FXML
    private void handleLogout() {
        if (taskRunner.runningProperty().get()) {
            return;
        }

        String token = userSession == null ? null : userSession.getToken();
        if (userSession != null) {
            userSession.clear();
        }
        loggedIn.set(false);

        if (token == null || authenticationClient == null) {
            navigation.showLogin(logoutButton);
            return;
        }

        showStatus("Logger ut ...", Color.BLACK);
        taskRunner.execute(() -> {
            authenticationClient.logout(token);
            return null;
        }, ignored -> navigation.showLogin(logoutButton),
                error -> navigation.showLogin(logoutButton));
    }

    /**
     * Går tilbake til hjemmesiden.
     */
    @FXML
    private void handleBack() {
        taskRunner.cancel();
        navigation.showHome(backButton);
    }

    /**
     * Går til hjemmesiden.
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
     * @param color   tekstfargen
     */
    private void showStatus(String message, Color color) {
        statusLabel.setTextFill(color);
        statusLabel.setText(message);
    }

}
