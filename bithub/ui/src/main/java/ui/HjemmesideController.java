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

    private Navigation navigation;

    public void setNavigation(Navigation navigation) {
        this.navigation = navigation;
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
        navigation.showComingSoon(myReviewsButton, "Mine anmeldelser");
    }

    @FXML
    private void handleProfile() {
        navigation.showComingSoon(profileButton, "Profil");
    }

}