package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.Course;
import core.DefaultCourses;
import core.Review;
import core.ReviewRepository;
import core.ReviewService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Tester emnevalg ved vellykket henting, venting, feil og navigering.
 */
class VelgFagControllerTest extends ApplicationTest {

    private VelgFagController controller;
    private InMemoryReviewRepository repository;
    private Parent root;
    private Stage stage;

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("VelgFag.fxml"));
        root = loader.load();
        controller = loader.getController();
        repository = new InMemoryReviewRepository();

        controller.setNavigation(
                new Navigation(new ReviewService(repository), repository));

        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void showsAllDefaultCoursesOnFirstStartup() throws Exception {
        repository.addCoursesIfMissing(DefaultCourses.all());

        interact(() -> controller.setReviewRepository(repository));
        awaitLoadingFinished();

        interact(() -> {
            assertEquals(DefaultCourses.all().stream().map(Course::getCourseCode).toList(),
                    courseComboBox().getItems().stream().map(Course::getCourseCode).toList());
            assertEquals("", statusLabel().getText());
            assertTrue(button("#nextButton").isDisabled());
        });
    }

    @Test
    void showsCoursesFromStorage() throws Exception {
        repository.addReview("TDT4100", "Objektorientert programmering",
                new Review("Ola", 4, "Bra"));
        repository.addCoursesIfMissing(
                List.of(new Course("IT1901", "Informatikk prosjektarbeid I")));

        interact(() -> controller.setReviewRepository(repository));
        awaitLoadingFinished();

        interact(() -> {
            List<Course> items = courseComboBox().getItems();
            assertEquals(2, items.size());
            assertEquals("TDT4100", items.get(0).getCourseCode());
            assertEquals("IT1901", items.get(1).getCourseCode());
            assertTrue(items.get(1).getReviews().isEmpty());
        });
    }

    @Test
    void rejectsNullService() {
        interact(() -> assertThrows(NullPointerException.class,
                () -> controller.setReviewRepository(null)));
    }

    @Test
    void showsMessageWhenNoCoursesAreAvailable() throws Exception {
        interact(() -> controller.setReviewRepository(repository));
        awaitLoadingFinished();

        interact(() -> {
            assertTrue(courseComboBox().getItems().isEmpty());
            assertEquals("Ingen emner er tilgjengelige.", statusLabel().getText());
            assertTrue(button("#nextButton").isDisabled());
        });
    }

    @Test
    void disablesCourseSelectionWhileLoading() throws Exception {
        CompletableFuture<List<Course>> response = new CompletableFuture<>();

        try {
            interact(() -> controller.setReviewRepository(
                    new CourseLoadingRepository(() -> response.join())));

            // JavaFX-tråden må kunne behandle dette mens hentingen fortsatt venter.
            interact(() -> {
                assertEquals("Henter emner …", statusLabel().getText());
                assertTrue(courseComboBox().isDisabled());
                assertTrue(button("#nextButton").isDisabled());
                assertFalse(button("#backButton").isDisabled());
                assertFalse(button("#homeButton").isDisabled());
            });

            response.complete(DefaultCourses.all());
            awaitLoadingFinished();

            interact(() -> {
                assertEquals(DefaultCourses.all().size(), courseComboBox().getItems().size());
                assertEquals("", statusLabel().getText());
                assertFalse(courseComboBox().isDisabled());
                assertTrue(button("#nextButton").isDisabled());

                courseComboBox().getSelectionModel().selectFirst();
                assertFalse(button("#nextButton").isDisabled());
            });
        } finally {
            response.complete(List.of());
        }
    }

    @Test
    void showsErrorWhenServerIsUnavailable() throws Exception {
        ReviewRepository unavailableRepository = new CourseLoadingRepository(() -> {
            throw new UncheckedIOException(
                    new ConnectException("Serveren er utilgjengelig."));
        });

        interact(() -> controller.setReviewRepository(unavailableRepository));
        awaitLoadingFinished();

        interact(() -> {
            assertEquals("Kunne ikke hente emnene. Prøv igjen senere.",
                    statusLabel().getText());
            assertTrue(courseComboBox().getItems().isEmpty());
            assertFalse(courseComboBox().isDisabled());
            assertTrue(button("#nextButton").isDisabled());
            assertFalse(button("#backButton").isDisabled());
            assertFalse(button("#homeButton").isDisabled());
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"#homeButton", "#backButton"})
    void ignoresLateResponseAfterNavigation(String buttonId) throws Exception {
        CompletableFuture<List<Course>> response = new CompletableFuture<>();
        CountDownLatch started = new CountDownLatch(1);
        AtomicReference<Thread> worker = new AtomicReference<>();

        ReviewRepository delayedRepository = new CourseLoadingRepository(() -> {
            worker.set(Thread.currentThread());
            started.countDown();

            // Svaret kommer selv om navigeringen forsøker å avbryte hentingen.
            return response.join();
        });

        try {
            interact(() -> controller.setReviewRepository(delayedRepository));
            assertTrue(started.await(5, TimeUnit.SECONDS));

            interact(() -> {
                button(buttonId).fire();
                assertNotSame(root, stage.getScene().getRoot());
                assertTrue(stage.getScene().getRoot()
                        .lookup("#writeReviewButton") instanceof Button);
            });

            response.complete(DefaultCourses.all());

            Thread backgroundThread = worker.get();
            backgroundThread.join(5000);
            assertFalse(backgroundThread.isAlive());

            interact(() -> {
                // Også den forlatte kontrolleren skal ignorere svaret.
                assertTrue(courseComboBox().getItems().isEmpty());
                assertEquals("Henter emner …", statusLabel().getText());
                assertTrue(stage.getScene().getRoot()
                        .lookup("#writeReviewButton") instanceof Button);
            });
        } finally {
            response.complete(List.of());
        }
    }

    /**
     * Venter uten å blokkere JavaFX-tråden, og leser UI-tilstanden på den tråden.
     */
    private void awaitLoadingFinished() throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> WaitForAsyncUtils.asyncFx(
                        () -> !courseComboBox().isDisabled()).get(5, TimeUnit.SECONDS));
    }

    @SuppressWarnings("unchecked")
    private ComboBox<Course> courseComboBox() {
        return (ComboBox<Course>) root.lookup("#courseComboBox");
    }

    private Label statusLabel() {
        return (Label) root.lookup("#statusLabel");
    }

    private Button button(String selector) {
        return (Button) root.lookup(selector);
    }

    /**
     * Lar hver test styre emnehentingen uten å endre den felles testlagringen.
     */
    private static final class CourseLoadingRepository implements ReviewRepository {

        private final Supplier<List<Course>> loadCourses;

        private CourseLoadingRepository(Supplier<List<Course>> loadCourses) {
            this.loadCourses = loadCourses;
        }

        @Override
        public List<Course> findAllCourses() {
            return loadCourses.get();
        }

        @Override
        public void addReview(String courseCode, String courseName, Review review) {
            throw new UnsupportedOperationException("Testen støtter bare emnehenting.");
        }

        @Override
        public Optional<Course> findCourse(String courseCode) {
            throw new UnsupportedOperationException("Testen støtter bare emnehenting.");
        }

        @Override
        public void addCoursesIfMissing(List<Course> courses) {
            throw new UnsupportedOperationException("Testen støtter bare emnehenting.");
        }
    }
}