package ui;

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

    @FXML
    private void handleReadReviews() {
        Navigation.showCourseSelectionForReading(readReviewsButton);
    }

    @FXML
    private void handleWriteReview() {
        Navigation.showCourseSelection(writeReviewButton);
    }

    @FXML
    private void handleMyReviews() {
        Navigation.showComingSoon(myReviewsButton, "Mine anmeldelser");
    }

    @FXML
    private void handleProfile() {
        Navigation.showComingSoon(profileButton, "Profil");
    }
}