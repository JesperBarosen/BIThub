package ui;

import core.Course;
import core.Review;
import core.ReviewService;
import java.util.Locale;
import java.util.Objects;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

/**
 * Kontroller for siden som viser anmeldelsene og gjennomsnittlig vurdering for
 * et valgt emne.
 *
 * <p>Kontrolleren henter emnet gjennom {@link ReviewService}, slik at den ikke
 * vet hvordan anmeldelsene er lagret.
 */
public class LeseAnmeldelserController {

    @FXML
    private Label courseTitleLabel;

    @FXML
    private Label averageLabel;

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
    private void initialize() {
        reviewList.setPlaceholder(new Label("Ingen anmeldelser ennå."));
        reviewList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Review review, boolean empty) {
                super.updateItem(review, empty);
                setWrapText(true);
                setText(empty || review == null ? null : formatReview(review));
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
     * Henter og viser anmeldelsene for emnet. Tjenesten må være satt først.
     *
     * @param course emnet som skal vises
     * @throws IllegalArgumentException hvis emnet er null
     * @throws IllegalStateException hvis tjenesten ikke er satt
     */
    public void setCourse(Course course) {
        if (reviewService == null) {
            throw new IllegalStateException("ReviewService must be set before the course.");
        }
        Course courseWithReviews = reviewService.getCourseWithReviews(course);

        courseTitleLabel.setText(
                courseWithReviews.getCourseCode() + " - " + courseWithReviews.getCourseName());
        averageLabel.setText(
                String.format(Locale.ROOT, "%.2f", courseWithReviews.getAverageRating()));
        countLabel.setText(String.valueOf(courseWithReviews.getReviews().size()));
        reviewList.getItems().setAll(courseWithReviews.getReviews());
    }

    /**
     * Lager teksten som vises for én anmeldelse i listen.
     *
     * @param review anmeldelsen som skal vises
     * @return vurdering og navn på første linje, kommentaren på neste
     */
    static String formatReview(Review review) {
        String heading = review.getRating() + "/5 - " + review.getReviewerName();
        String comment = review.getComment();
        return comment == null || comment.isBlank() ? heading : heading + "\n" + comment;
    }

    @FXML
    private void handleBack() {
        Navigation.showCourseSelectionForReading(backButton);
    }

    @FXML
    private void handleHome() {
        Navigation.showHome(homeButton);
    }
}