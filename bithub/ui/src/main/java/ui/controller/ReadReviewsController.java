package ui.controller;

import core.model.Course;
import core.model.Review;
import core.service.ReviewService;
import java.util.Locale;
import java.util.Objects;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import org.controlsfx.control.Rating;
import ui.Navigation;
import ui.util.BackgroundTaskRunner;

/**
 * Kontroller for siden som viser anmeldelsene og gjennomsnittlig vurdering for et valgt emne.
 *
 * <p>Kontrolleren henter emnet gjennom {@link ReviewService}, slik at den ikke vet hvordan
 * anmeldelsene er lagret.
 */
public class ReadReviewsController {

    @FXML
    private Label courseTitleLabel;

    @FXML
    private Label averageLabel;

    @FXML
    private Rating averageStars;

    @FXML
    private Label countLabel;

    @FXML
    private ListView<Review> reviewList;

    @FXML
    private Button backButton;

    @FXML
    private Button homeButton;

    private ReviewService reviewService;
    @FXML
    private Button profileButton;

    private Navigation navigation;

    private final BackgroundTaskRunner taskRunner = new BackgroundTaskRunner();

    /**
     * Setter opp lista slik at hver anmeldelse vises som et kort, og viser en melding når emnet
     * ikke har anmeldelser.
     */
    @FXML
    private void initialize() {
        reviewList.setPlaceholder(new Label("Ingen anmeldelser ennå."));
        reviewList.setCellFactory(listView -> new ListCell<>() {
            /**
             * Viser kortet for anmeldelsen i cellen, eller ingenting hvis cellen er tom.
             *
             * @param review anmeldelsen cellen skal vise
             * @param empty  {@code true} hvis cellen ikke har innhold
             */
            @Override
            protected void updateItem(Review review, boolean empty) {
                super.updateItem(review, empty);
                setText(null);
                if (empty || review == null) {
                    setGraphic(null);
                } else {
                    VBox card = createReviewCard(review);
                    // Trekker fra plass til rullefeltet, slik at kommentaren brytes riktig.
                    card.maxWidthProperty().bind(listView.widthProperty().subtract(40));
                    setGraphic(card);
                }
            }
        });
    }

    /**
     * Setter tjenesten kontrolleren skal hente anmeldelser fra.
     *
     * @param reviewService tjenesten som henter lagrede anmeldelser
     * @throws NullPointerException hvis reviewService er null
     */
    public void setReviewService(ReviewService reviewService) {
        this.reviewService = Objects.requireNonNull(reviewService);
    }

    /**
     * Setter navigasjonen som kontrolleren skal bruke. Profilknappen kobles samtidig.
     *
     * @param navigation navigasjonsobjektet
     */
    public void setNavigation(Navigation navigation) {
        this.navigation = Objects.requireNonNull(navigation);
        navigation.connectProfileButton(profileButton, taskRunner::cancel);
    }

    /**
     * Henter og viser anmeldelsene og gjennomsnittet for emnet, både som tall og stjerner.
     *
     * @param course emnet som skal vises
     * @throws IllegalArgumentException hvis emnet er null
     * @throws IllegalStateException    hvis tjenesten ikke er satt
     */
    public void setCourse(Course course) {
        if (reviewService == null) {
            throw new IllegalStateException("ReviewService må settes før emnet.");
        }
        if (course == null) {
            throw new IllegalArgumentException("Emnet kan ikke være null.");
        }

        taskRunner.cancel();

        courseTitleLabel.setText(course.getCourseCode() + " - " + course.getCourseName());
        averageLabel.setText("-");
        averageStars.setRating(0);
        countLabel.setText("-");
        reviewList.getItems().clear();
        reviewList.setPlaceholder(new Label("Henter anmeldelser ..."));

        taskRunner.execute(() -> reviewService.getCourseWithReviews(course), courseWithReviews -> {
            courseTitleLabel.setText(
                    courseWithReviews.getCourseCode() + " - " + courseWithReviews.getCourseName());
            averageLabel.setText(
                    String.format(Locale.ROOT, "%.2f", courseWithReviews.getAverageRating()));
            averageStars.setRating(courseWithReviews.getAverageRating());
            countLabel.setText(String.valueOf(courseWithReviews.getReviews().size()));
            reviewList.getItems().setAll(courseWithReviews.getReviews());
            reviewList.setPlaceholder(new Label("Ingen anmeldelser ennå."));
        }, error -> {
            reviewList.getItems().clear();
            reviewList
                    .setPlaceholder(new Label("Kunne ikke lese anmeldelsene. Prøv igjen senere."));
        });
    }

    /**
     * Lager visningen av én anmeldelse i listen: navnet, vurderingen og kommentaren under
     * hverandre.
     *
     * @param review anmeldelsen som skal vises
     * @return en boks med navn, vurdering og eventuelt kommentar
     */
    static VBox createReviewCard(Review review) {
        Label name = new Label(review.getReviewerName().toUpperCase(Locale.ROOT));
        name.getStyleClass().add("anmeldelse-navn");

        Label rating = new Label("VURDERING: " + review.getRating() + "/5");

        VBox card = new VBox(4, name, rating);
        card.getStyleClass().add("anmeldelse-kort");

        String comment = review.getComment();
        if (comment != null && !comment.isBlank()) {
            Label commentText = new Label(comment);
            commentText.setWrapText(true);
            card.getChildren().addAll(new Label("KOMMENTAR:"), commentText);
        }
        return card;
    }

    /**
     * Avbryter ventingen og går tilbake til emnevelgeren.
     */
    @FXML
    private void handleBack() {
        taskRunner.cancel();
        navigation.showCourseSelectionForReading(backButton);
    }

    /**
     * Avbryter ventingen og går til hjemmesiden.
     */
    @FXML
    private void handleHome() {
        taskRunner.cancel();
        navigation.showHome(homeButton);
    }
}