package ui;

import java.util.Objects;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Handlingene på startsiden.
 */
public class HjemmesideController {

    @FXML
    private Button readReviewsButton;

    @FXML
    private Button writeReviewButton;

    @FXML
    private Button myReviewsButton;

    @FXML
    private Button profileButton;

    private Navigation navigation;

    /**
     * Setter navigasjonen som kontrolleren skal bruke.
     *
     * @param navigation navigasjonsobjektet
     */
    public void setNavigation(Navigation navigation) {
        this.navigation = Objects.requireNonNull(navigation);
    }

    @FXML
    private void handleReadReviews() {
        navigation.showCourseSelectionForReading(readReviewsButton);
    }

    @FXML
    private void handleWriteReview() {
        navigation.showCourseSelection(writeReviewButton);
    }

    @FXML
    private void handleMyReviews() {
        navigation.showMyReviews(myReviewsButton);
    }

    @FXML
    private void handleProfile() {
        navigation.showComingSoon(profileButton, "Profil");
    }
}