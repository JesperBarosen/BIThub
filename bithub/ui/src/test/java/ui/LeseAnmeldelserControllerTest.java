package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.Course;
import core.Review;
import core.ReviewService;
import file.ReviewHandler;
import java.io.IOException;
import java.nio.file.Path;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * UI-tester for {@link LeseAnmeldelserController} ved bruk av TestFX.
 *
 * <p>Anmeldelsene lagres i en midlertidig fil med {@link ReviewHandler}, slik at
 * testene viser at data som er lagret til fil, leses tilbake og vises i UI.
 */
class LeseAnmeldelserControllerTest extends ApplicationTest {

    @TempDir
    Path tempDir;

    private LeseAnmeldelserController controller;
    private ReviewHandler handler;
    private Parent root;

    /**
     * Laster LeseAnmeldelser.fxml og kobler kontrolleren til en tjeneste som
     * lagrer i en midlertidig fil.
     *
     * @param stage hovedvinduet for TestFX
     * @throws IOException hvis FXML-filen ikke kan lastes
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LeseAnmeldelser.fxml"));
        root = loader.load();
        controller = loader.getController();

        handler = new ReviewHandler(tempDir.resolve("courses.json"));
        Navigation.setReviewService(new ReviewService(handler));
        controller.setReviewService(new ReviewService(handler));

        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Verifiserer at emnenavn, gjennomsnitt, antall og anmeldelser fra filen
     * vises på siden.
     */
    @Test
    void showsStoredReviewsAndAverage() {
        handler.addReview("IT1901", "Informatikk prosjektarbeid I", new Review("Ola", 5, "Bra"));
        handler.addReview("IT1901", "Informatikk prosjektarbeid I", new Review("Anonym", 2, ""));

        interact(() -> controller.setCourse(new Course("IT1901", "Informatikk prosjektarbeid I")));

        assertEquals("IT1901 - Informatikk prosjektarbeid I", label("#courseTitleLabel").getText());
        assertEquals("3.50", label("#averageLabel").getText());
        assertEquals("2", label("#countLabel").getText());
        ListView<Review> reviews = reviewList();
        assertEquals(2, reviews.getItems().size());
        assertEquals("Ola", reviews.getItems().get(0).getReviewerName());
    }

    /**
     * Verifiserer at et emne uten anmeldelser viser 0 og en tom liste.
     */
    @Test
    void showsEmptyCourse() {
        interact(() -> controller.setCourse(new Course("TDT4100", "Objektorientert")));

        assertEquals("TDT4100 - Objektorientert", label("#courseTitleLabel").getText());
        assertEquals("0.00", label("#averageLabel").getText());
        assertEquals("0", label("#countLabel").getText());
        assertTrue(reviewList().getItems().isEmpty());
    }

    /**
     * Verifiserer teksten som vises for en anmeldelse, med og uten kommentar.
     */
    @Test
    void formatReviewShowsRatingNameAndComment() {
        assertEquals("4/5 - Ola\nBra fag",
                LeseAnmeldelserController.formatReview(new Review("Ola", 4, "Bra fag")));
        assertEquals("2/5 - Anonym",
                LeseAnmeldelserController.formatReview(new Review("Anonym", 2, "")));
    }

    /**
     * Verifiserer at emnet ikke kan vises før tjenesten er satt, og at
     * tjenesten ikke kan være null.
     */
    @Test
    void requiresReviewService() {
        LeseAnmeldelserController withoutService = new LeseAnmeldelserController();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LeseAnmeldelser.fxml"));
            try {
                Parent page = loader.load();
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

    @SuppressWarnings("unchecked")
    private ListView<Review> reviewList() {
        return (ListView<Review>) root.lookup("#reviewList");
    }

    private Button button(String selector) {
        return (Button) root.lookup(selector);
    }
}