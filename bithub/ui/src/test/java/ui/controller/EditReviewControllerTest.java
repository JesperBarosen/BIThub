package ui.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.model.Course;
import core.model.Review;
import core.repository.ReviewRepository;
import core.service.ReviewService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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
 * TestFX-tester for {@link EditReviewController}. Anmeldelsene lagres i minnet, slik at
 * testene ikke trenger serveren.
 */
class EditReviewControllerTest extends ApplicationTest {

    private static final String OWNER = "ola@example.com";

    private Stage stage;
    private EditReviewController controller;
    private InMemoryReviewRepository repository;
    private Review review;

    /**
     * Lagrer én anmeldelse og åpner skjemaet for å endre den.
     *
     * @param stage vinduet testene kjører i
     * @throws IOException hvis FXML-filen ikke kan lastes
     */
    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        repository = new InMemoryReviewRepository();
        review = new Review("id-1", OWNER, "Ola Nordmann", false, 4, "Bra fag");
        repository.addReview("IT1901", "Informatikk prosjektarbeid I", review);

        ReviewService reviewService = new ReviewService(repository);
        UserSession userSession = new UserSession();
        userSession.signIn("token-1", "Ola Nordmann", OWNER);
        final Navigation navigation = new Navigation(reviewService, repository, userSession, null);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/EditReview.fxml"));
        final Parent root = loader.load();
        controller = loader.getController();
        controller.setReviewService(reviewService);
        controller.setNavigation(navigation);
        controller.setReview("IT1901", "Informatikk prosjektarbeid I", review);

        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Verifiserer at skjemaet er fylt ut med anmeldelsen slik den er nå.
     */
    @Test
    void formIsFilledWithReview() {
        assertEquals("IT1901 - Informatikk prosjektarbeid I", label("#courseTitleLabel").getText());
        assertEquals(4.0, ratingStars().getRating());
        assertEquals("Bra fag", commentArea().getText());
        assertFalse(anonymousCheckBox().isSelected());
        assertEquals(review, controller.getReview());
    }

    /**
     * Sjekker at lagring uten valgt anmeldelse viser en feil og beholder skjemaet og lagringen.
     *
     * @throws Exception hvis statusmeldingen ikke vises innen tidsgrensen
     */
    @Test
    void missingReviewShowsErrorWithoutChangingStoredReview() throws Exception {
        interact(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/EditReview.fxml"));
            final Parent root;
            try {
                root = loader.load();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            controller = loader.getController();
            ReviewService service = new ReviewService(repository);
            controller.setReviewService(service);
            controller.setNavigation(new Navigation(service, repository));
            stage.setScene(new Scene(root));
            ratingField().setText("2");
            commentArea().setText("Ny kommentar");
            anonymousCheckBox().setSelected(true);
        });
        final Scene editScene = stage.getScene();

        interact(() -> button("#saveButton").fire());
        awaitStatus("Ingen anmeldelse er valgt.");

        assertNull(controller.getReview());
        assertSame(editScene, stage.getScene());
        assertEquals(Color.RED, label("#statusLabel").getTextFill());
        assertEquals("2", ratingField().getText());
        assertEquals("Ny kommentar", commentArea().getText());
        assertTrue(anonymousCheckBox().isSelected());
        assertSame(review, repository.findReview("id-1").orElseThrow());
        assertEquals(4, review.getRating());
        assertEquals("Bra fag", review.getComment());
        assertFalse(review.isAnonymous());
    }

    /**
     * Verifiserer at endringen lagres, og at appen går tilbake til «Mine anmeldelser» med den
     * endrede anmeldelsen.
     */
    @Test
    void savesChangeAndShowsMyReviews() throws Exception {
        interact(() -> {
            ratingStars().setRating(2);
            commentArea().setText("  Mye arbeid  ");
            anonymousCheckBox().setSelected(true);
            button("#saveButton").fire();
        });

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> stage.getScene().getRoot().lookup("#reviewList") != null);

        Review saved = repository.findReview("id-1").orElseThrow();
        assertEquals(2, saved.getRating());
        assertEquals("Mye arbeid", saved.getComment());
        assertTrue(saved.isAnonymous());
        assertEquals(OWNER, saved.getOwnerEmail());
        assertEquals("Ola Nordmann", saved.getReviewerName());
    }

    /**
     * Verifiserer at lagring uten valgt stjerne gir en feilmelding, og at skjemaet og
     * anmeldelsen beholdes.
     */
    @Test
    void missingRatingShowsErrorAndKeepsForm() throws Exception {
        interact(() -> {
            ratingStars().setRating(0);
            commentArea().setText("Ny kommentar");
            button("#saveButton").fire();
        });

        awaitStatus("Velg en vurdering ved å trykke på stjernene.");
        assertEquals(Color.RED, label("#statusLabel").getTextFill());
        assertEquals("Ny kommentar", commentArea().getText());
        assertEquals(4, repository.findReview("id-1").orElseThrow().getRating());
    }

    /**
     * Sjekker at en nettverksfeil beholder skjemaet og ikke endrer den lagrede anmeldelsen.
     *
     * @throws Exception hvis feilmeldingen ikke vises innen tidsgrensen
     */
    @Test
    void networkFailureKeepsFormAndStoredReview() throws Exception {
        ReviewRepository unavailableRepository = new ReviewRepository() {
            @Override
            public void addReview(String courseCode, String courseName, Review review) {
                repository.addReview(courseCode, courseName, review);
            }

            @Override
            public Optional<Course> findCourse(String courseCode) {
                return repository.findCourse(courseCode);
            }

            @Override
            public List<Course> findAllCourses() {
                return repository.findAllCourses();
            }

            @Override
            public void addCoursesIfMissing(List<Course> courses) {
                repository.addCoursesIfMissing(courses);
            }

            @Override
            public boolean updateReview(String reviewId, int rating, String comment,
                    boolean anonymous) {
                throw new UncheckedIOException(new IOException("Ingen kontakt med serveren."));
            }
        };
        final Scene editScene = stage.getScene();

        interact(() -> {
            controller.setReviewService(new ReviewService(unavailableRepository));
            ratingField().setText("2");
            commentArea().setText("Ny kommentar");
            anonymousCheckBox().setSelected(true);
            button("#saveButton").fire();
        });

        awaitStatus("Kunne ikke lagre endringen. Prøv igjen senere.");
        assertSame(editScene, stage.getScene());
        assertEquals("2", ratingField().getText());
        assertEquals("Ny kommentar", commentArea().getText());
        assertTrue(anonymousCheckBox().isSelected());
        Review stored = repository.findReview("id-1").orElseThrow();
        assertEquals(4, stored.getRating());
        assertEquals("Bra fag", stored.getComment());
        assertFalse(stored.isAnonymous());
    }

    /**
     * Verifiserer at en anmeldelse som er slettet i mellomtiden, gir en feilmelding.
     */
    @Test
    void reviewThatIsGoneShowsError() throws Exception {
        repository.deleteReview("id-1");

        interact(() -> button("#saveButton").fire());

        awaitStatus("Anmeldelsen finnes ikke lenger.");
        assertNull(stage.getScene().getRoot().lookup("#reviewList"));
    }

    /**
     * Verifiserer at «Tilbake» går fra redigering til «Mine anmeldelser» uten å lagre.
     */
    @Test
    void backOpensMyReviewsWithoutSaving() {
        interact(() -> {
            ratingStars().setRating(1);
            button("#backButton").fire();
        });
        assertNotNull(stage.getScene().getRoot().lookup("#reviewList"));
        assertEquals(4, repository.findReview("id-1").orElseThrow().getRating());
    }

    /**
     * Verifiserer at «Hjem» går direkte fra redigering til hjemmesiden uten å lagre.
     */
    @Test
    void homeOpensHomePageWithoutSaving() {
        interact(() -> {
            ratingField().setText("1");
            commentArea().setText("Ulagret kommentar");
            anonymousCheckBox().setSelected(true);
            button("#homeButton").fire();
        });

        assertNotNull(stage.getScene().getRoot().lookup("#myReviewsButton"));
        Review stored = repository.findReview("id-1").orElseThrow();
        assertEquals(4, stored.getRating());
        assertEquals("Bra fag", stored.getComment());
        assertFalse(stored.isAnonymous());
    }

    /**
     * Venter til statusmeldingen vises og lagringen er ferdig.
     *
     * @param expected meldingen som forventes
     * @throws Exception hvis meldingen ikke vises innen tidsgrensen
     */
    private void awaitStatus(String expected) throws Exception {
        WaitForAsyncUtils
                .waitFor(5, TimeUnit.SECONDS,
                        () -> WaitForAsyncUtils
                                .asyncFx(() -> expected.equals(label("#statusLabel").getText())
                                        && !button("#saveButton").isDisabled())
                                .get(5, TimeUnit.SECONDS));
    }

    private Label label(String selector) {
        return (Label) stage.getScene().getRoot().lookup(selector);
    }

    private Button button(String selector) {
        return (Button) stage.getScene().getRoot().lookup(selector);
    }

    private Rating ratingStars() {
        return (Rating) stage.getScene().getRoot().lookup("#ratingStars");
    }

    private TextArea commentArea() {
        return (TextArea) stage.getScene().getRoot().lookup("#commentArea");
    }

    private CheckBox anonymousCheckBox() {
        return (CheckBox) stage.getScene().getRoot().lookup("#anonymousCheckBox");
    }
}
