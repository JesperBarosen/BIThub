package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.model.Course;
import core.model.Review;
import core.service.DefaultCourses;
import core.service.ReviewService;
import java.util.concurrent.TimeUnit;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import ui.client.AuthenticationClient;
import ui.client.AuthenticationResult;
import ui.client.InMemoryReviewRepository;
import ui.session.UserSession;

// Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
/**
 * TestFX-tester for gjesteflyten: fortsette som gjest fra innloggingen, lese anmeldelser, og logge
 * inn eller registrere seg senere uten å starte appen på nytt.
 *
 * <p>Appen settes opp slik App gjør, men med lagring i minnet og en falsk autentiseringstjeneste,
 * slik at testene ikke trenger serveren.
 */
class GuestFlowTest extends ApplicationTest {

    private Stage stage;
    private UserSession userSession;

    /**
     * Viser innloggingsskjermen med standardemnene og én anmeldelse av IT1901.
     *
     * @param stage vinduet testene kjører i
     */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        InMemoryReviewRepository repository = new InMemoryReviewRepository();
        repository.addCoursesIfMissing(DefaultCourses.all());
        repository.addReview("IT1901", "Informatikk prosjektarbeid I",
                new Review("id-1", "kari@ntnu.no", "Kari Nordmann", false, 4, "Bra fag"));

        userSession = new UserSession();
        Navigation navigation = new Navigation(new ReviewService(repository), repository,
                userSession, new FakeAuthenticationClient());
        navigation.showLogin(stage);
        stage.show();
    }

    /**
     * Verifiserer at en gjest kommer til hjemmesiden uten innlogging, og at bare lesing og
     * innlogging vises der.
     */
    @Test
    void guestSeesOnlyReadingAndLogin() {
        continueAsGuest();

        assertFalse(userSession.isLoggedIn());
        assertTrue(button("#readReviewsButton").isVisible());
        assertTrue(button("#loginButton").isVisible());
        assertFalse(button("#writeReviewButton").isVisible());
        assertFalse(button("#myReviewsButton").isVisible());
        assertFalse(button("#profileButton").isVisible());
    }

    /**
     * Verifiserer at en gjest kan velge et emne og lese anmeldelsene av det, og at profilknappen er
     * skjult på begge sidene.
     *
     * @throws Exception hvis emnene eller anmeldelsene ikke lastes innen tidsgrensen
     */
    @Test
    void guestCanReadReviews() throws Exception {
        continueAsGuest();

        interact(() -> button("#readReviewsButton").fire());
        awaitCoursesLoaded();
        assertFalse(button("#profileButton").isVisible());
        interact(() -> {
            courseComboBox().getSelectionModel().select(4);
            button("#nextButton").fire();
        });

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> WaitForAsyncUtils.asyncFx(() -> "1".equals(label("#countLabel").getText()))
                        .get(5, TimeUnit.SECONDS));
        assertEquals("IT1901 - Informatikk prosjektarbeid I", label("#courseTitleLabel").getText());
        assertEquals("4.00", label("#averageLabel").getText());
        assertEquals(1, ((ListView<?>) find("#reviewList")).getItems().size());
        assertFalse(button("#profileButton").isVisible());
    }

    /**
     * Verifiserer at en gjest kan logge inn fra hjemmesiden, og at alle valgene vises etterpå.
     *
     * @throws Exception hvis hjemmesiden ikke vises innen tidsgrensen
     */
    @Test
    void guestCanLogInWithoutRestart() throws Exception {
        continueAsGuest();

        interact(() -> button("#loginButton").fire());
        interact(() -> {
            ((TextField) find("#emailField")).setText("ola@ntnu.no");
            ((TextField) find("#passwordField")).setText("Passord123");
            button("#loginButton").fire();
        });

        awaitLoggedInHome();
        assertEquals("ola@ntnu.no", userSession.getEmail());
    }

    /**
     * Verifiserer at en gjest kan registrere seg fra hjemmesiden og blir logget inn med en gang.
     *
     * @throws Exception hvis hjemmesiden ikke vises innen tidsgrensen
     */
    @Test
    void guestCanRegisterWithoutRestart() throws Exception {
        continueAsGuest();

        interact(() -> button("#loginButton").fire());
        interact(() -> button("#registerButton").fire());
        interact(() -> {
            ((TextField) find("#nameField")).setText("Ny Bruker");
            ((TextField) find("#emailField")).setText("ny@ntnu.no");
            ((TextField) find("#repeatEmailField")).setText("ny@ntnu.no");
            ((TextField) find("#passwordField")).setText("Passord123");
            ((TextField) find("#repeatPasswordField")).setText("Passord123");
            button("#registerButton").fire();
        });

        awaitLoggedInHome();
        assertEquals("Ny Bruker", userSession.getName());
    }

    /**
     * Trykker «Fortsett som gjest» og sjekker at hjemmesiden vises.
     */
    private void continueAsGuest() {
        interact(() -> button("#guestButton").fire());
        assertNotNull(find("#readReviewsButton"));
    }

    /**
     * Venter til hjemmesiden vises med alle valgene for en innlogget bruker.
     *
     * @throws Exception hvis hjemmesiden ikke vises innen tidsgrensen
     */
    private void awaitLoggedInHome() throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> WaitForAsyncUtils.asyncFx(() -> {
            Node writeButton = find("#writeReviewButton");
            return writeButton != null && writeButton.isVisible();
        }).get(5, TimeUnit.SECONDS));

        assertTrue(userSession.isLoggedIn());
        assertTrue(button("#myReviewsButton").isVisible());
        assertTrue(button("#profileButton").isVisible());
        assertFalse(button("#loginButton").isVisible());
    }

    /**
     * Venter til standardemnene er lastet og emnevelgeren er tilgjengelig.
     *
     * @throws Exception hvis emnene ikke lastes innen tidsgrensen
     */
    private void awaitCoursesLoaded() throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> WaitForAsyncUtils.asyncFx(() -> {
            ComboBox<Course> comboBox = courseComboBox();
            return comboBox != null && !comboBox.isDisabled()
                    && comboBox.getItems().size() == DefaultCourses.all().size();
        }).get(5, TimeUnit.SECONDS));
    }

    @SuppressWarnings("unchecked")
    private ComboBox<Course> courseComboBox() {
        return (ComboBox<Course>) find("#courseComboBox");
    }

    private Node find(String selector) {
        return stage.getScene().getRoot().lookup(selector);
    }

    private Button button(String selector) {
        return (Button) find(selector);
    }

    private Label label(String selector) {
        return (Label) find(selector);
    }

    /**
     * Godtar alle innlogginger og registreringer uten å kontakte en REST-server.
     */
    private static final class FakeAuthenticationClient implements AuthenticationClient {

        @Override
        public AuthenticationResult login(String email, String password) {
            return new AuthenticationResult("token-login", "Ola Nordmann", email);
        }

        @Override
        public AuthenticationResult register(String name, String email, String password) {
            return new AuthenticationResult("token-register", name, email);
        }
    }
}