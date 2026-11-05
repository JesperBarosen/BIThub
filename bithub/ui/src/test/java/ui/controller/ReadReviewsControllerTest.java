package ui.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.model.Course;
import core.model.Review;
import core.repository.ReviewRepository;
import core.service.ReviewService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.controlsfx.control.Rating;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import ui.Navigation;
import ui.client.InMemoryReviewRepository;

/**
 * UI-tester for {@link ReadReviewsController} ved bruk av TestFX.
 *
 * <p>Anmeldelsene lagres i en {@link InMemoryReviewRepository}, slik at testene viser at lagrede
 * anmeldelser hentes og vises i UI uten at serveren må kjøre.
 */
class ReadReviewsControllerTest extends ApplicationTest {

    private ReadReviewsController controller;
    private InMemoryReviewRepository repository;
    private Parent root;
    private Stage stage;

    /**
     * Laster ReadReviews.fxml og kobler kontrolleren til en tjeneste som lagrer i minnet.
     *
     * @param stage hovedvinduet for TestFX
     * @throws IOException hvis FXML-filen ikke kan lastes
     */
    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/ReadReviews.fxml"));
        root = loader.load();
        controller = loader.getController();

        repository = new InMemoryReviewRepository();
        ReviewService reviewService = new ReviewService(repository);
        Navigation navigation = new Navigation(reviewService, repository);

        controller.setReviewService(reviewService);
        controller.setNavigation(navigation);

        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Verifiserer at emnenavn, gjennomsnitt, antall og anmeldelser fra lagringen vises på siden.
     */
    @Test
    void showsStoredReviewsAndAverage() throws Exception {
        repository.addReview("IT1901", "Informatikk prosjektarbeid I", new Review("Ola", 5, "Bra"));
        repository.addReview("IT1901", "Informatikk prosjektarbeid I", new Review("Anonym", 2, ""));

        interact(() -> controller.setCourse(new Course("IT1901", "Informatikk prosjektarbeid I")));
        awaitReviewsLoaded();

        assertEquals("IT1901 - Informatikk prosjektarbeid I", label("#courseTitleLabel").getText());
        assertEquals("3.50", label("#averageLabel").getText());
        assertEquals(3.5, averageStars().getRating());
        assertTrue(averageStars().isMouseTransparent());
        assertEquals("2", label("#countLabel").getText());
        ListView<Review> reviews = reviewList();
        assertEquals(2, reviews.getItems().size());
        assertEquals("Ola", reviews.getItems().get(0).getReviewerName());
    }

    /**
     * Verifiserer at et langt emnenavn brytes over flere linjer og vises helt, uten «...», og at
     * overskriften er sentrert.
     */
    @Test
    void showsWholeLongCourseTitle() throws Exception {
        final String title = "TDT4100 - Objektorientert programmering med et svært langt emnenavn";

        interact(() -> controller.setCourse(new Course("TDT4100",
                "Objektorientert programmering med et svært langt emnenavn")));
        awaitReviewsLoaded();
        WaitForAsyncUtils.waitForFxEvents();

        interact(() -> {
            Label courseTitle = label("#courseTitleLabel");
            Text shownText = (Text) courseTitle.lookup(".text");
            assertEquals(title, shownText.getText());
            assertEquals(TextAlignment.CENTER, courseTitle.getTextAlignment());
        });
    }

    /**
     * Verifiserer at et emne uten anmeldelser viser 0 og en tom liste.
     */
    @Test
    void showsEmptyCourse() throws Exception {
        interact(() -> controller.setCourse(new Course("TDT4100", "Objektorientert")));
        awaitReviewsLoaded();

        assertEquals("TDT4100 - Objektorientert", label("#courseTitleLabel").getText());
        assertEquals("0.00", label("#averageLabel").getText());
        assertEquals(0.0, averageStars().getRating());
        assertEquals("0", label("#countLabel").getText());
        assertTrue(reviewList().getItems().isEmpty());
    }


    /**
     * Kontrollerer lastetilstand og at JavaFX svarer mens hentingen er treg.
     */
    @Test
    void showsLoadingStateWhileFetchingReviews() throws Exception {
        DelayedReviewRepository delayedRepository = new DelayedReviewRepository();

        try {
            interact(() -> {
                controller.setReviewService(new ReviewService(delayedRepository));
                controller.setCourse(new Course("IT1901", "Informatikk prosjektarbeid I"));
            });
            assertTrue(delayedRepository.started.await(5, TimeUnit.SECONDS));

            interact(() -> {
                assertEquals("-", label("#averageLabel").getText());
                assertEquals(0.0, averageStars().getRating());
                assertEquals("-", label("#countLabel").getText());
                assertEquals("Henter anmeldelser ...", placeholderText());
                assertTrue(reviewList().getItems().isEmpty());
                assertFalse(button("#backButton").isDisabled());
                assertFalse(button("#homeButton").isDisabled());
            });

            Course loaded = new Course("IT1901", "Informatikk prosjektarbeid I");
            loaded.addReview(new Review("Ola", 5, "Bra emne"));
            delayedRepository.response.complete(Optional.of(loaded));
            awaitReviewsLoaded();

            interact(() -> {
                assertEquals("5.00", label("#averageLabel").getText());
                assertEquals(5.0, averageStars().getRating());
                assertEquals("1", label("#countLabel").getText());
                assertEquals("Ingen anmeldelser ennå.", placeholderText());
                assertEquals(1, reviewList().getItems().size());
            });
        } finally {
            delayedRepository.response.complete(Optional.empty());
        }
    }

    /**
     * Kontrollerer at utilgjengelig server gir forståelig feilmelding.
     */
    @Test
    void showsErrorWhenServerIsUnavailable() throws Exception {
        DelayedReviewRepository delayedRepository = new DelayedReviewRepository();

        try {
            interact(() -> {
                controller.setReviewService(new ReviewService(delayedRepository));
                controller.setCourse(new Course("IT1901", "Informatikk prosjektarbeid I"));
            });
            assertTrue(delayedRepository.started.await(5, TimeUnit.SECONDS));
            delayedRepository.response.completeExceptionally(
                    new UncheckedIOException(new ConnectException("Server utilgjengelig")));

            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                    () -> WaitForAsyncUtils.asyncFx(() ->
                            "Kunne ikke lese anmeldelsene. Prøv igjen senere."
                                    .equals(placeholderText())).get(5, TimeUnit.SECONDS));

            interact(() -> {
                assertEquals("-", label("#averageLabel").getText());
                assertEquals(0.0, averageStars().getRating());
                assertEquals("-", label("#countLabel").getText());
                assertTrue(reviewList().getItems().isEmpty());
                assertFalse(button("#backButton").isDisabled());
                assertFalse(button("#homeButton").isDisabled());
            });
        } finally {
            delayedRepository.response.complete(Optional.empty());
        }
    }

    /**
     * Kontrollerer at et sent serversvar ikke oppdaterer en forlatt skjerm.
     */
    @Test
    void ignoresLateResponseAfterNavigation() throws Exception {
        DelayedReviewRepository delayedRepository = new DelayedReviewRepository();

        try {
            interact(() -> {
                controller.setReviewService(new ReviewService(delayedRepository));
                controller.setCourse(new Course("IT1901", "Informatikk prosjektarbeid I"));
            });
            assertTrue(delayedRepository.started.await(5, TimeUnit.SECONDS));

            interact(() -> {
                button("#homeButton").fire();
                assertNotSame(root, stage.getScene().getRoot());
                assertNotNull(stage.getScene().getRoot().lookup("#writeReviewButton"));
            });

            Course lateResponse = new Course("IT1901", "Informatikk prosjektarbeid I");
            lateResponse.addReview(new Review("Ola", 5, "Sent svar"));
            delayedRepository.response.complete(Optional.of(lateResponse));
            assertTrue(delayedRepository.finished.await(5, TimeUnit.SECONDS));
            WaitForAsyncUtils.waitForFxEvents();

            interact(() -> {
                assertEquals("-", label("#averageLabel").getText());
                assertEquals("-", label("#countLabel").getText());
                assertTrue(reviewList().getItems().isEmpty());
                assertEquals("Henter anmeldelser ...", placeholderText());
                assertNotNull(stage.getScene().getRoot().lookup("#writeReviewButton"));
            });
        } finally {
            delayedRepository.response.complete(Optional.empty());
        }
    }

    /**
     * Verifiserer at en anmeldelse vises med navn, stjerner og kommentar, og at kommentaren
     * utelates når den er tom.
     */
    @Test
    void reviewCardShowsNameRatingAndComment() {
        VBox withComment = ReadReviewsController.createReviewCard(new Review("Ola", 4, "Bra fag"));
        assertEquals(List.of("OLA", "KOMMENTAR:", "Bra fag"), labelTexts(withComment));
        assertEquals(4.0, ((Rating) withComment.getChildren().get(1)).getRating());

        VBox withoutComment = ReadReviewsController.createReviewCard(new Review("Anonym", 2, ""));
        assertEquals(List.of("ANONYM"), labelTexts(withoutComment));
        assertEquals(2.0, ((Rating) withoutComment.getChildren().get(1)).getRating());
    }

    /**
     * Henter teksten fra hver etikett i kortet, i samme rekkefølge som de vises. Stjernene er
     * ikke en etikett og hoppes over.
     *
     * @param card kortet som skal leses
     * @return teksten i etikettene, ovenfra og ned
     */
    private static List<String> labelTexts(VBox card) {
        return card.getChildren().stream()
                .filter(Label.class::isInstance)
                .map(node -> ((Label) node).getText())
                .toList();
    }

    /**
     * Verifiserer at emnet ikke kan vises før tjenesten er satt, og at tjenesten ikke kan være
     * null.
     */
    @Test
    void requiresReviewService() {
        ReadReviewsController withoutService = new ReadReviewsController();
        assertThrows(IllegalStateException.class,
                () -> withoutService.setCourse(new Course("IT1901", "Prosjektarbeid")));
        assertThrows(NullPointerException.class, () -> withoutService.setReviewService(null));
    }

    /**
     * Verifiserer at TILBAKE åpner fagvelgeren og HJEM åpner hjemmesiden.
     */
    @Test
    void backAndHomeButtonsNavigate() {
        interact(() -> button("#backButton").fire());
        assertNotNull(lookup("#courseComboBox").tryQuery().orElse(null));

        interact(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/ReadReviews.fxml"));
            try {
                Parent page = loader.load();
                ReadReviewsController pageController = loader.getController();
                pageController.setReviewService(new ReviewService(repository));
                pageController
                        .setNavigation(new Navigation(new ReviewService(repository), repository));

                Stage stage = (Stage) lookup("#courseComboBox").query().getScene().getWindow();
                stage.setScene(new Scene(page));
                ((Button) page.lookup("#homeButton")).fire();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
        assertNotNull(lookup("#writeReviewButton").tryQuery().orElse(null));
    }

    private Label label(String selector) {
        return (Label) root.lookup(selector);
    }

    private Rating averageStars() {
        return (Rating) root.lookup("#averageStars");
    }

    @SuppressWarnings("unchecked")
    private ListView<Review> reviewList() {
        return (ListView<Review>) root.lookup("#reviewList");
    }

    private Button button(String selector) {
        return (Button) root.lookup(selector);
    }

    /**
     * Venter til anmeldelsene er ferdig hentet og statistikken er oppdatert.
     *
     * @throws Exception hvis innlastingen ikke fullføres innen tidsgrensen
     */
    private void awaitReviewsLoaded() throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> WaitForAsyncUtils.asyncFx(() -> !"-".equals(label("#countLabel").getText()))
                        .get(5, TimeUnit.SECONDS));
    }

    private String placeholderText() {
        return ((Label) reviewList().getPlaceholder()).getText();
    }

    /**
     * Simulerer et forsinket svar fra lagringen uten en faktisk REST-server.
     */
    private static final class DelayedReviewRepository implements ReviewRepository {

        private final CompletableFuture<Optional<Course>> response = new CompletableFuture<>();
        private final CountDownLatch started = new CountDownLatch(1);
        private final CountDownLatch finished = new CountDownLatch(1);

        @Override
        public Optional<Course> findCourse(String courseCode) {
            started.countDown();
            try {
                return response.join();
            } finally {
                finished.countDown();
            }
        }

        @Override
        public List<Course> findAllCourses() {
            throw new UnsupportedOperationException("Testen støtter bare henting av anmeldelser.");
        }

        @Override
        public void addReview(String courseCode, String courseName, Review review) {
            throw new UnsupportedOperationException("Testen støtter bare henting av anmeldelser.");
        }

        @Override
        public void addCoursesIfMissing(List<Course> courses) {
            throw new UnsupportedOperationException("Testen støtter bare henting av anmeldelser.");
        }
    }

}