package ui.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.exception.NotReviewOwnerException;
import core.model.Course;
import core.model.Review;
import core.repository.ReviewRepository;
import core.service.ReviewService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.controlsfx.control.Rating;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import ui.Navigation;
import ui.client.InMemoryReviewRepository;
import ui.session.UserSession;

/**
 * TestFX-tester for {@link MyReviewsController}.
 *
 * <p>Anmeldelsene lagres i minnet, og spørsmålet før sletting byttes ut, slik at testene ikke
 * trenger serveren eller en dialogboks.
 */
class MyReviewsControllerTest extends ApplicationTest {

    private static final String OWNER = "ola@example.com";

    private Stage stage;
    private MyReviewsController controller;
    private InMemoryReviewRepository repository;
    private UserSession userSession;
    private ReviewService reviewService;
    private final List<String> questions = new ArrayList<>();
    private boolean confirmDelete = true;

    /**
     * Laster MyReviews.fxml med en innlogget bruker og lagring i minnet.
     *
     * @param stage vinduet testene kjører i
     * @throws IOException hvis FXML-filen ikke kan lastes
     */
    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        repository = new InMemoryReviewRepository();
        reviewService = new ReviewService(repository);
        userSession = new UserSession();
        userSession.signIn("token-1", "Ola Nordmann", OWNER);
        final Navigation navigation = new Navigation(reviewService, repository, userSession,
                null);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/MyReviews.fxml"));
        final Parent root = loader.load();
        controller = loader.getController();
        controller.setReviewService(reviewService);
        controller.setUserSession(userSession);
        controller.setNavigation(navigation);
        controller.setDeleteConfirmation(question -> {
            questions.add(question);
            return confirmDelete;
        });

        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Verifiserer at bare brukerens egne anmeldelser vises, også de anonyme, og ikke anmeldelser
     * fra andre brukere eller gjester.
     */
    @Test
    void showsOnlyOwnReviewsIncludingAnonymous() throws Exception {
        repository.addReview("IT1901", "Informatikk prosjektarbeid I",
                new Review("id-1", OWNER, "Ola Nordmann", false, 5, "Bra"));
        repository.addReview("TDT4100", "Objektorientert",
                new Review("id-2", OWNER, "Ola Nordmann", true, 3, ""));
        repository.addReview("IT1901", "Informatikk prosjektarbeid I",
                new Review("id-3", "kari@example.com", "Kari", false, 2, "Tungt"));
        repository.addReview("IT1901", "Informatikk prosjektarbeid I",
                new Review("Gjest", 4, "Fint"));

        loadReviews();

        assertEquals("2", label("#countLabel").getText());
        List<MyReviewsController.OwnReview> items = reviewList().getItems();
        assertEquals(2, items.size());
        assertEquals("id-1", items.get(0).review().getId());
        assertEquals("IT1901", items.get(0).courseCode());
        assertEquals("id-2", items.get(1).review().getId());
        assertTrue(items.get(1).review().isAnonymous());
        assertEquals("", label("#statusLabel").getText());
    }

    /**
     * Verifiserer at en bruker uten anmeldelser får 0 og en tom liste.
     */
    @Test
    void showsEmptyListWithoutOwnReviews() throws Exception {
        repository.addReview("IT1901", "Informatikk prosjektarbeid I",
                new Review("id-3", "kari@example.com", "Kari", false, 2, "Tungt"));

        loadReviews();

        assertEquals("0", label("#countLabel").getText());
        assertTrue(reviewList().getItems().isEmpty());
    }

    /**
     * Verifiserer at siden ber om innlogging når ingen er logget inn, uten å hente noe.
     */
    @Test
    void asksForLoginWhenNotLoggedIn() {
        interact(() -> {
            userSession.clear();
            controller.loadReviews();
        });

        Label status = label("#statusLabel");
        assertEquals("Du må logge inn for å se dine anmeldelser.", status.getText());
        assertEquals(Color.RED, status.getTextFill());
        assertEquals("-", label("#countLabel").getText());
        assertTrue(reviewList().getItems().isEmpty());
    }

    /**
     * Verifiserer at en feil fra lagringen gir en feilmelding.
     */
    @Test
    void showsErrorWhenReviewsCannotBeFetched() throws Exception {
        interact(() -> {
            controller.setReviewService(new ReviewService(new FailingReviewRepository()));
            controller.loadReviews();
        });

        awaitStatus("Kunne ikke hente anmeldelsene. Prøv igjen senere.");
        assertEquals(Color.RED, label("#statusLabel").getTextFill());
    }

    /**
     * Verifiserer at anmeldelsen slettes etter at brukeren har bekreftet, og at lista og antallet
     * oppdateres.
     */
    @Test
    void deletesReviewAfterConfirmation() throws Exception {
        repository.addReview("IT1901", "Informatikk prosjektarbeid I",
                new Review("id-1", OWNER, "Ola Nordmann", false, 5, "Bra"));
        repository.addReview("TDT4100", "Objektorientert",
                new Review("id-2", OWNER, "Ola Nordmann", true, 3, ""));
        loadReviews();

        interact(() -> firstCardButton("#deleteButton").fire());
        awaitStatus("Anmeldelsen ble slettet.");

        assertEquals(List.of("Vil du slette anmeldelsen av IT1901?"), questions);
        assertEquals("1", label("#countLabel").getText());
        assertEquals("id-2", reviewList().getItems().get(0).review().getId());
        assertTrue(repository.findReview("id-1").isEmpty());
        assertTrue(repository.findReview("id-2").isPresent());
    }

    /**
     * Verifiserer at ingenting slettes når brukeren angrer.
     */
    @Test
    void keepsReviewWhenDeleteIsCancelled() throws Exception {
        repository.addReview("IT1901", "Informatikk prosjektarbeid I",
                new Review("id-1", OWNER, "Ola Nordmann", false, 5, "Bra"));
        loadReviews();
        confirmDelete = false;

        interact(() -> firstCardButton("#deleteButton").fire());

        assertEquals(1, questions.size());
        assertEquals("1", label("#countLabel").getText());
        assertEquals(1, reviewList().getItems().size());
        assertTrue(repository.findReview("id-1").isPresent());
    }

    /**
     * Sjekker at en mislykket sletting beholder anmeldelsen og lar brukeren prøve igjen.
     *
     * @throws Exception hvis statusmeldingen ikke vises innen tidsgrensen
     */
    @Test
    void networkFailureKeepsReviewAndAllowsDeleteRetry() throws Exception {
        repository.addReview("IT1901", "Informatikk prosjektarbeid I",
                new Review("id-1", OWNER, "Ola Nordmann", false, 5, "Bra"));
        loadReviews();
        interact(() -> controller.setReviewService(
                new ReviewService(new FailingReviewRepository())));

        interact(() -> firstCardButton("#deleteButton").fire());
        awaitStatus("Kunne ikke slette anmeldelsen. Prøv igjen senere.");

        assertEquals(Color.RED, label("#statusLabel").getTextFill());
        assertEquals("1", label("#countLabel").getText());
        assertEquals(1, reviewList().getItems().size());
        assertEquals("id-1", reviewList().getItems().get(0).review().getId());
        assertTrue(repository.findReview("id-1").isPresent());

        interact(() -> {
            controller.setReviewService(reviewService);
            firstCardButton("#deleteButton").fire();
        });
        awaitStatus("Anmeldelsen ble slettet.");

        assertEquals(2, questions.size());
        assertEquals("0", label("#countLabel").getText());
        assertTrue(reviewList().getItems().isEmpty());
        assertTrue(repository.findReview("id-1").isEmpty());
    }

    /**
     * Sjekker at avvist eierskap viser serverens melding uten å fjerne anmeldelsen fra lista.
     *
     * @throws Exception hvis statusmeldingen ikke vises innen tidsgrensen
     */
    @Test
    void rejectedOwnershipKeepsReviewAndShowsMessage() throws Exception {
        repository.addReview("IT1901", "Informatikk prosjektarbeid I",
                new Review("id-1", OWNER, "Ola Nordmann", false, 5, "Bra"));
        loadReviews();
        interact(() -> controller.setReviewService(new ReviewService(
                new FailingReviewRepository(new NotReviewOwnerException()))));

        interact(() -> firstCardButton("#deleteButton").fire());
        awaitStatus("Du har ikke tilgang til å gjøre denne endringen.");

        assertEquals(Color.RED, label("#statusLabel").getTextFill());
        assertEquals(1, questions.size());
        assertEquals("1", label("#countLabel").getText());
        assertEquals(1, reviewList().getItems().size());
        assertEquals("id-1", reviewList().getItems().get(0).review().getId());
        assertTrue(repository.findReview("id-1").isPresent());
        interact(() -> assertFalse(firstCardButton("#deleteButton").isDisabled()));
    }

    /**
     * Verifiserer at en anmeldelse som allerede er borte, fjernes fra lista med en melding.
     */
    @Test
    void removesReviewThatIsAlreadyGone() throws Exception {
        repository.addReview("IT1901", "Informatikk prosjektarbeid I",
                new Review("id-1", OWNER, "Ola Nordmann", false, 5, "Bra"));
        loadReviews();
        repository.deleteReview("id-1");

        interact(() -> firstCardButton("#deleteButton").fire());
        awaitStatus("Anmeldelsen finnes ikke lenger.");

        assertEquals("0", label("#countLabel").getText());
        assertTrue(reviewList().getItems().isEmpty());
    }

    /**
     * Verifiserer at «Endre» åpner skjemaet utfylt med anmeldelsen.
     */
    @Test
    void editButtonOpensFilledForm() throws Exception {
        repository.addReview("IT1901", "Informatikk prosjektarbeid I",
                new Review("id-1", OWNER, "Ola Nordmann", true, 4, "Bra"));
        loadReviews();

        interact(() -> firstCardButton("#editButton").fire());

        Parent root = stage.getScene().getRoot();
        assertEquals("IT1901 - Informatikk prosjektarbeid I",
                ((Label) root.lookup("#courseTitleLabel")).getText());
        assertEquals(4.0, ((Rating) root.lookup("#ratingStars")).getRating());
        assertTrue(((CheckBox) root.lookup("#anonymousCheckBox")).isSelected());
    }

    /**
     * Verifiserer at «Tilbake» og «Hjem» går til hjemmesiden.
     */
    @Test
    void backAndHomeOpenHomePage() {
        interact(() -> ((Button) stage.getScene().getRoot().lookup("#backButton")).fire());
        assertNotNull(stage.getScene().getRoot().lookup("#myReviewsButton"));

        interact(() -> ((Button) stage.getScene().getRoot().lookup("#myReviewsButton")).fire());
        assertNotNull(stage.getScene().getRoot().lookup("#reviewList"));

        interact(() -> ((Button) stage.getScene().getRoot().lookup("#homeButton")).fire());
        assertNotNull(stage.getScene().getRoot().lookup("#myReviewsButton"));
    }

    /**
     * Henter anmeldelsene og venter til hentingen er ferdig.
     *
     * @throws Exception hvis hentingen ikke blir ferdig innen tidsgrensen
     */
    private void loadReviews() throws Exception {
        interact(() -> controller.loadReviews());
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> WaitForAsyncUtils.asyncFx(() -> !reviewList().isDisabled()
                        && !"Henter anmeldelsene ...".equals(label("#statusLabel").getText()))
                        .get(5, TimeUnit.SECONDS));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Venter til statusmeldingen vises og kallet er ferdig.
     *
     * @param expected meldingen som forventes
     * @throws Exception hvis meldingen ikke vises innen tidsgrensen
     */
    private void awaitStatus(String expected) throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> WaitForAsyncUtils.asyncFx(() -> expected
                        .equals(label("#statusLabel").getText()) && !reviewList().isDisabled())
                        .get(5, TimeUnit.SECONDS));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Finner en knapp i kortet for den første anmeldelsen i lista.
     *
     * @param selector id-en til knappen, for eksempel "#deleteButton"
     * @return knappen
     */
    private Button firstCardButton(String selector) {
        Node button = reviewList().lookup(selector);
        assertNotNull(button, "Fant ikke " + selector + " i lista");
        assertFalse(button.isDisabled());
        return (Button) button;
    }

    @SuppressWarnings("unchecked")
    private ListView<MyReviewsController.OwnReview> reviewList() {
        return (ListView<MyReviewsController.OwnReview>) stage.getScene().getRoot()
                .lookup("#reviewList");
    }

    private Label label(String selector) {
        return (Label) stage.getScene().getRoot().lookup(selector);
    }

    /**
     * Lagring som alltid feiler, slik at testene kan sjekke feilmeldingen.
     */
    private static final class FailingReviewRepository implements ReviewRepository {
        private final RuntimeException deleteError;

        private FailingReviewRepository() {
            this(new UncheckedIOException(new IOException("Serveren svarer ikke.")));
        }

        private FailingReviewRepository(RuntimeException deleteError) {
            this.deleteError = deleteError;
        }

        @Override
        public void addReview(String courseCode, String courseName, Review review) {
        }

        @Override
        public Optional<Course> findCourse(String courseCode) {
            return Optional.empty();
        }

        @Override
        public List<Course> findAllCourses() {
            throw new UncheckedIOException(new IOException("Serveren svarer ikke."));
        }

        @Override
        public void addCoursesIfMissing(List<Course> courses) {
        }

        @Override
        public boolean deleteReview(String reviewId) {
            throw deleteError;
        }
    }
}
