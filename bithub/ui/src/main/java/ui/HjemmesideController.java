package ui;

import java.util.List;
import java.util.Objects;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Handlingene på startsiden.
 *
 * <p>Gjester kan bare lese anmeldelser. For dem skjules knappene for å skrive anmeldelser, «Mine
 * anmeldelser» og profilen, og det vises en knapp for å logge inn eller registrere seg i stedet.
 */
public class HjemmesideController {

    @FXML
    private Button readReviewsButton;

    @FXML
    private Button writeReviewButton;

    @FXML
    private Button myReviewsButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button loginButton;

    private Navigation navigation;

    /**
     * Lar knappene som er skjult, slippe å ta plass på siden.
     */
    @FXML
    private void initialize() {
        for (Button button : List.of(writeReviewButton, myReviewsButton, profileButton,
                loginButton)) {
            button.managedProperty().bind(button.visibleProperty());
        }
    }

    /**
     * Setter navigasjonen som kontrolleren skal bruke.
     *
     * @param navigation navigasjonsobjektet
     */
    public void setNavigation(Navigation navigation) {
        this.navigation = Objects.requireNonNull(navigation);
    }

    /**
     * Viser knappene som passer for den som bruker appen. En innlogget bruker får alle valgene,
     * mens en gjest bare kan lese anmeldelser, logge inn eller registrere seg.
     *
     * @param userSession sesjonen som sier om noen er logget inn
     * @throws NullPointerException hvis userSession er null
     */
    public void setUserSession(UserSession userSession) {
        boolean loggedIn = Objects.requireNonNull(userSession).isLoggedIn();
        writeReviewButton.setVisible(loggedIn);
        myReviewsButton.setVisible(loggedIn);
        profileButton.setVisible(loggedIn);
        loginButton.setVisible(!loggedIn);
    }

    /**
     * Åpner emnevelgeren for å lese anmeldelser.
     */
    @FXML
    private void handleReadReviews() {
        navigation.showCourseSelectionForReading(readReviewsButton);
    }

    /**
     * Åpner emnevelgeren for å skrive en anmeldelse.
     */
    @FXML
    private void handleWriteReview() {
        navigation.showCourseSelection(writeReviewButton);
    }

    /**
     * Åpner siden med brukerens egne anmeldelser.
     */
    @FXML
    private void handleMyReviews() {
        navigation.showMyReviews(myReviewsButton);
    }

    /**
     * Åpner profilsiden.
     */
    @FXML
    private void handleProfile() {
        navigation.showProfile(profileButton);
    }

    /**
     * Åpner innloggingen, der en gjest kan logge inn eller gå videre til registrering.
     */
    @FXML
    private void handleLogin() {
        navigation.showLogin(loginButton);
    }
}