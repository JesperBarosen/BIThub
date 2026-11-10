package ui.controller;

import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import ui.Navigation;
import ui.client.AuthenticationClient;
import ui.session.UserSession;
import ui.util.BackgroundTaskRunner;

/**
 * Viser profilen til den innloggede brukeren og lar brukeren endre navn, gå til passordbytte eller
 * logge ut.
 * 
 * <p>Navn og e- post hentes fra {@link UserSession} som fikk dem fra serveren ved innlogging.
 * Navnebytte og utlogging sendes til serveren i bakgrunnen med {@link BackgroundTaskRunner}.
 */
public final class ProfileController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private Button changeNameButton;

    @FXML
    private Button saveNameButton;

    @FXML
    private Button cancelNameButton;

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
    private final BooleanProperty editingName = new SimpleBooleanProperty(false);
    private final BackgroundTaskRunner taskRunner = new BackgroundTaskRunner();

    /**
     * Deaktiverer knappene mens et kall til serveren pågår. Navnebytte og passordbytte krever
     * innlogging. «LAGRE» og «AVBRYT» vises bare mens navnet redigeres, i stedet for «ENDRE NAVN».
     */
    @FXML
    private void initialize() {
        logoutButton.disableProperty().bind(taskRunner.runningProperty());
        changePasswordButton.disableProperty()
                .bind(taskRunner.runningProperty().or(loggedIn.not()));
        changeNameButton.disableProperty()
                .bind(taskRunner.runningProperty().or(loggedIn.not()));
        saveNameButton.disableProperty().bind(taskRunner.runningProperty());
        cancelNameButton.disableProperty().bind(taskRunner.runningProperty());

        changeNameButton.visibleProperty().bind(editingName.not());
        changeNameButton.managedProperty().bind(editingName.not());
        saveNameButton.visibleProperty().bind(editingName);
        saveNameButton.managedProperty().bind(editingName);
        cancelNameButton.visibleProperty().bind(editingName);
        cancelNameButton.managedProperty().bind(editingName);
        nameField.editableProperty().bind(editingName.and(taskRunner.runningProperty().not()));
        nameField.focusTraversableProperty().bind(editingName);
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
     * Gjør navnefeltet redigerbart, slik at brukeren kan skrive et nytt navn.
     */
    @FXML
    private void handleChangeName() {
        if (taskRunner.runningProperty().get() || !loggedIn.get()) {
            return;
        }
        editingName.set(true);
        showStatus("", Color.BLACK);
        nameField.requestFocus();
        nameField.selectAll();
    }

    /**
     * Sender det nye navnet til serveren. Navnet i sesjonen endres først når serveren har lagret
     * det. Ved feil vises en melding, og brukeren kan rette navnet eller avbryte.
     */
    @FXML
    private void handleSaveName() {
        if (taskRunner.runningProperty().get() || !editingName.get()) {
            return;
        }
        String newName = nameField.getText() == null ? "" : nameField.getText().strip();
        if (newName.isEmpty()) {
            showStatus("Navnet kan ikke være tomt.", Color.RED);
            return;
        }
        if (newName.equals(userSession.getName())) {
            stopEditingName();
            return;
        }
        if (!userSession.isLoggedIn() || authenticationClient == null) {
            showStatus("Du må være logget inn for å endre navn.", Color.RED);
            return;
        }

        String token = userSession.getToken();
        String email = userSession.getEmail();

        showStatus("Lagrer navnet ...", Color.BLACK);
        taskRunner.execute(() -> authenticationClient.changeName(token, email, newName),
                savedName -> {
                    userSession.updateName(savedName);
                    stopEditingName();
                    showStatus("Navnet er endret.", Color.GREEN);
                }, error -> {
                    if (error instanceof IllegalArgumentException && error.getMessage() != null
                            && !error.getMessage().isBlank()) {
                        showStatus(error.getMessage(), Color.RED);
                    } else {
                        showStatus("Kunne ikke lagre navnet. Prøv igjen senere.", Color.RED);
                    }
                });
    }

    /**
     * Avbryter navnebyttet og viser navnet fra sesjonen igjen.
     */
    @FXML
    private void handleCancelName() {
        if (taskRunner.runningProperty().get()) {
            return;
        }
        stopEditingName();
        showStatus("", Color.BLACK);
    }

    /**
     * Gjør navnefeltet skrivebeskyttet igjen og viser navnet fra sesjonen.
     */
    private void stopEditingName() {
        editingName.set(false);
        nameField.setText(userSession.getName());
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

        // Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
        showStatus("Logger ut ...", Color.BLACK);
        taskRunner.execute(() -> {
            authenticationClient.logout(token);
            return null;
        }, ignored -> navigation.showLogin(logoutButton),
                error -> navigation.showLogin(logoutButton));
        // Slutt på kode laget med hjelp av KI.
    }

    /**
     * Går tilbake til siden profilen ble åpnet fra, eller til hjemmesiden.
     */
    @FXML
    private void handleBack() {
        taskRunner.cancel();
        navigation.returnFromProfile(backButton);
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