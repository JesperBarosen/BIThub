package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.model.Course;
import core.model.Review;
import core.service.DefaultCourses;
import core.service.ReviewService;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import ui.client.InMemoryReviewRepository;
import ui.controller.HomeController;
import ui.session.UserSession;

/**
 * Tester navigasjonen i de nye FXML-skjermbildene.
 */
class NewNavigationTest extends ApplicationTest {

    private Stage stage;
    private Navigation navigation;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        // Setter opp appen slik App gjør, men med lagring i minnet i stedet for serveren.
        // Lagringen har bare standardemnene, slik serveren har ved første oppstart.
        InMemoryReviewRepository repository = new InMemoryReviewRepository();
        repository.addCoursesIfMissing(DefaultCourses.all());

        ReviewService reviewService = new ReviewService(repository);
        // Skriving krever innlogging, så testene kjører med en innlogget bruker.
        UserSession userSession = new UserSession();
        userSession.signIn("token-1", "Kari Nordmann", "kari@ntnu.no");
        navigation = new Navigation(reviewService, repository, userSession, null);

        FXMLLoader loader = new FXMLLoader(App.class.getResource("Home.fxml"));
        final Parent root = loader.load();

        HomeController controller = loader.getController();
        controller.setNavigation(navigation);
        controller.setUserSession(userSession);

        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void canChooseCourseAndReturnHome() throws Exception {
        interact(() -> button("#writeReviewButton").fire());
        awaitCoursesLoaded();

        @SuppressWarnings("unchecked")
        ComboBox<Course> courseComboBox = (ComboBox<Course>) stage.getScene().getRoot()
                .lookup("#courseComboBox");
        assertNotNull(courseComboBox);
        assertEquals(DefaultCourses.all().size(), courseComboBox.getItems().size());

        interact(() -> {
            courseComboBox.getSelectionModel().select(4);
            button("#nextButton").fire();
        });

        Label title = (Label) stage.getScene().getRoot().lookup("#courseTitleLabel");
        assertNotNull(title);
        assertEquals("IT1901 - Informatikk prosjektarbeid I", title.getText());

        interact(() -> button("#backButton").fire());
        assertNotNull(stage.getScene().getRoot().lookup("#courseComboBox"));

        interact(() -> button("#homeButton").fire());
        assertNotNull(stage.getScene().getRoot().lookup("#writeReviewButton"));
    }

    /**
     * Verifiserer at profilen åpnes fra hjemmesiden og viser brukeren som er logget inn. Profilen
     * uten innlogging testes i {@code ui.controller.ProfileControllerTest}.
     */
    @Test
    void profileOpensFromHomePage() {
        interact(() -> button("#profileButton").fire());

        assertNotNull(stage.getScene().getRoot().lookup("#changePasswordButton"));
        TextField name = (TextField) stage.getScene().getRoot().lookup("#nameField");
        assertEquals("Kari Nordmann", name.getText());
    }

    /**
     * Verifiserer at profilknappen åpner profilen fra alle sidene der en innlogget bruker ser den,
     * og at knappen er deaktivert på selve profilsiden.
     */
    @Test
    void profileOpensFromEveryPage() {
        Course course = DefaultCourses.all().get(4);
        Review review = new Review("id-1", "kari@ntnu.no", "Kari Nordmann", false, 4, "Bra fag");
        List<Consumer<Node>> pages = List
                .of(navigation::showCourseSelection, navigation::showCourseSelectionForReading,
                        source -> navigation.showReviews(source, course),
                        source -> navigation.showReviewForm(source, course),
                        navigation::showMyReviews, source -> navigation.showEditReview(source,
                                course.getCourseCode(), course.getCourseName(), review),
                        navigation::showChangePassword);

        for (Consumer<Node> page : pages) {
            interact(() -> page.accept(stage.getScene().getRoot()));
            Button profileButton = button("#profileButton");
            assertTrue(profileButton.isVisible());

            interact(profileButton::fire);
            assertNotNull(stage.getScene().getRoot().lookup("#changePasswordButton"));
            assertTrue(button("#profileButton").isDisabled());
        }
    }

    /**
     * Verifiserer at «TILBAKE» på profilen går til siden profilknappen ble trykket på, slik den
     * var.
     */
    @Test
    void backFromProfileReturnsToPreviousPage() {
        Course course = DefaultCourses.all().get(4);
        Review review = new Review("id-1", "kari@ntnu.no", "Kari Nordmann", false, 4, "Bra fag");
        List<Consumer<Node>> pages = List
                .of(navigation::showCourseSelection, navigation::showCourseSelectionForReading,
                        source -> navigation.showReviews(source, course),
                        source -> navigation.showReviewForm(source, course),
                        navigation::showMyReviews, source -> navigation.showEditReview(source,
                                course.getCourseCode(), course.getCourseName(), review),
                        navigation::showChangePassword);

        for (Consumer<Node> page : pages) {
            interact(() -> page.accept(stage.getScene().getRoot()));
            Scene previous = stage.getScene();

            interact(() -> button("#profileButton").fire());
            interact(() -> button("#backButton").fire());

            assertSame(previous, stage.getScene());
        }
    }

    /**
     * Verifiserer at «TILBAKE» på profilen fortsatt går til siden man kom fra etter et besøk på
     * «Endre passord», og at den går til hjemmesiden når profilen ble åpnet derfra.
     */
    @Test
    void backFromProfileRemembersPageThroughChangePassword() {
        Course course = DefaultCourses.all().get(4);
        interact(() -> navigation.showReviews(stage.getScene().getRoot(), course));
        final Scene reviews = stage.getScene();

        interact(() -> button("#profileButton").fire());
        interact(() -> button("#changePasswordButton").fire());
        interact(() -> button("#backButton").fire());
        interact(() -> button("#backButton").fire());
        assertSame(reviews, stage.getScene());

        interact(() -> button("#homeButton").fire());
        interact(() -> button("#profileButton").fire());
        interact(() -> button("#backButton").fire());
        assertNotNull(stage.getScene().getRoot().lookup("#myReviewsButton"));
    }

    /**
     * Verifiserer at «Mine anmeldelser» åpnes fra hjemmesiden for en innlogget bruker.
     */
    @Test
    void myReviewsOpensFromHomePage() {
        interact(() -> button("#myReviewsButton").fire());

        assertNotNull(stage.getScene().getRoot().lookup("#reviewList"));
    }

    /**
     * Går gjennom ui og core: skriver en anmeldelse i UI, som lagres, og leser den tilbake på siden
     * for å lese anmeldelser.
     */
    @Test
    void writtenReviewIsShownWhenReading() throws Exception {
        interact(() -> button("#writeReviewButton").fire());
        awaitCoursesLoaded();

        interact(() -> {
            courseComboBox().getSelectionModel().select(4);
            button("#nextButton").fire();
        });

        interact(() -> {
            ((TextField) stage.getScene().getRoot().lookup("#ratingField")).setText("4");
            button("#submitButton").fire();
        });
        awaitReviewSaved();
        interact(() -> button("#homeButton").fire());

        interact(() -> button("#readReviewsButton").fire());
        awaitCoursesLoaded();

        interact(() -> {
            courseComboBox().getSelectionModel().select(4);
            button("#nextButton").fire();
        });

        awaitReviewsLoaded();

        assertEquals("IT1901 - Informatikk prosjektarbeid I", label("#courseTitleLabel").getText());
        assertEquals("4.00", label("#averageLabel").getText());
        assertEquals("1", label("#countLabel").getText());
        assertEquals(1,
                ((ListView<?>) stage.getScene().getRoot().lookup("#reviewList")).getItems().size());

        interact(() -> button("#backButton").fire());
        assertNotNull(courseComboBox());
    }

    @SuppressWarnings("unchecked")
    private ComboBox<Course> courseComboBox() {
        return (ComboBox<Course>) stage.getScene().getRoot().lookup("#courseComboBox");
    }

    private Label label(String selector) {
        return (Label) stage.getScene().getRoot().lookup(selector);
    }

    private Button button(String selector) {
        return (Button) stage.getScene().getRoot().lookup(selector);
    }

    /**
     * Venter til standardemnene er lastet og emnevelgeren er tilgjengelig.
     */
    private void awaitCoursesLoaded() throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> WaitForAsyncUtils.asyncFx(() -> {
            ComboBox<Course> comboBox = courseComboBox();
            return comboBox != null && !comboBox.isDisabled()
                    && comboBox.getItems().size() == DefaultCourses.all().size();
        }).get(5, TimeUnit.SECONDS));
    }

    /**
     * Venter på bekreftet lagring før testen navigerer bort fra skjemaet.
     */
    private void awaitReviewSaved() throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> WaitForAsyncUtils.asyncFx(() -> {
            Label status = label("#statusLabel");
            return status != null && "Vurderingen ble sendt inn!".equals(status.getText());
        }).get(5, TimeUnit.SECONDS));
    }

    /**
     * Venter til vurderingen er hentet og vises på anmeldelsessiden.
     */
    private void awaitReviewsLoaded() throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> WaitForAsyncUtils.asyncFx(() -> {
            Label average = label("#averageLabel");
            Label count = label("#countLabel");
            ListView<?> reviews = (ListView<?>) stage.getScene().getRoot().lookup("#reviewList");
            return average != null && count != null && reviews != null
                    && "4.00".equals(average.getText()) && "1".equals(count.getText())
                    && reviews.getItems().size() == 1;
        }).get(5, TimeUnit.SECONDS));
    }
}