package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.Course;
import core.Review;
import core.ReviewService;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

/**
 * UI-tester for {@link LeseAnmeldelserController} ved bruk av TestFX.
 *
 * <p>Anmeldelsene lagres i en {@link InMemoryReviewRepository}, slik at testene viser at lagrede
 * anmeldelser hentes og vises i UI uten at serveren må kjøre.
 */
class LeseAnmeldelserControllerTest extends ApplicationTest {

    private LeseAnmeldelserController controller;
    private InMemoryReviewRepository repository;
    private Parent root;

    /**
     * Laster LeseAnmeldelser.fxml og kobler kontrolleren til en tjeneste som lagrer i minnet.
     *
     * @param stage hovedvinduet for TestFX
     * @throws IOException hvis FXML-filen ikke kan lastes
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LeseAnmeldelser.fxml"));
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
     * Verifiserer at en anmeldelse vises med navn, vurdering og kommentar, og at kommentaren
     * utelates når den er tom.
     */
    @Test
    void reviewCardShowsNameRatingAndComment() {
        assertEquals(List.of("OLA", "VURDERING: 4/5", "KOMMENTAR:", "Bra fag"), labelTexts(
                LeseAnmeldelserController.createReviewCard(new Review("Ola", 4, "Bra fag"))));
        assertEquals(List.of("ANONYM", "VURDERING: 2/5"), labelTexts(
                LeseAnmeldelserController.createReviewCard(new Review("Anonym", 2, ""))));
    }

    /**
     * Henter teksten fra hver etikett i kortet, i samme rekkefølge som de vises.
     *
     * @param card kortet som skal leses
     * @return teksten i etikettene, ovenfra og ned
     */
    private static List<String> labelTexts(VBox card) {
        return card.getChildren().stream().map(node -> ((Label) node).getText()).toList();
    }

    /**
     * Verifiserer at emnet ikke kan vises før tjenesten er satt, og at tjenesten ikke kan være
     * null.
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
                LeseAnmeldelserController pageController = loader.getController();
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
}