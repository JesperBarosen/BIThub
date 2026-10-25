package ui;

import core.NotReviewOwnerException;
import core.Review;
import core.ReviewNotFoundException;
import core.ReviewService;
import java.util.Objects;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

/**
 * Kontroller for skjemaet der brukeren endrer en av sine egne anmeldelser.
 *
 * <p>Skjemaet fylles ut med anmeldelsen slik den er nå. Endringen sendes i bakgrunnen med
 * {@link BackgroundTaskRunner}, og skjemainnholdet beholdes hvis endringen feiler.
 */
public class EndreEgenAnmeldelseController {

    @FXML
    private Label emneTittelLabel;

    @FXML
    private TextField ratingField;

    @FXML
    private TextArea commentArea;

    @FXML
    private CheckBox anonymousCheckBox;

    @FXML
    private Label statusLabel;

    @FXML
    private Button saveButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button backButton;

    @FXML
    private Button homeButton;

    private ReviewService reviewService;
    private Navigation navigation;
    private Review review;

    private final BackgroundTaskRunner taskRunner = new BackgroundTaskRunner();

    /**
     * Deaktiverer lagring og redigering mens endringen sendes.
     */
    @FXML
    private void initialize() {
        saveButton.disableProperty().bind(taskRunner.runningProperty());
        ratingField.disableProperty().bind(taskRunner.runningProperty());
        commentArea.disableProperty().bind(taskRunner.runningProperty());
        anonymousCheckBox.disableProperty().bind(taskRunner.runningProperty());
    }

    /**
     * Setter tjenesten kontrolleren sender endringen med.
     *
     * @param reviewService tjenesten for anmeldelser
     * @throws NullPointerException hvis reviewService er null
     */
    public void setReviewService(ReviewService reviewService) {
        this.reviewService = Objects.requireNonNull(reviewService);
    }

    /**
     * Setter navigasjonen som kontrolleren skal bruke.
     *
     * @param navigation navigasjonsobjektet
     * @throws NullPointerException hvis navigation er null
     */
    public void setNavigation(Navigation navigation) {
        this.navigation = Objects.requireNonNull(navigation);
    }

    /**
     * Viser anmeldelsen som skal endres, og fyller ut skjemaet med vurderingen, kommentaren og om
     * den er anonym.
     *
     * @param courseCode emnekoden til emnet anmeldelsen hører til
     * @param courseName navnet på emnet
     * @param review     anmeldelsen som skal endres
     * @throws NullPointerException hvis review er null
     */
    public void setReview(String courseCode, String courseName, Review review) {
        this.review = Objects.requireNonNull(review);
        emneTittelLabel.setText(courseCode + " - " + courseName);
        ratingField.setText(String.valueOf(review.getRating()));
        commentArea.setText(review.getComment());
        anonymousCheckBox.setSelected(review.isAnonymous());
        statusLabel.setText("");
    }

    /**
     * Sender endringen i bakgrunnen. Ved suksess går appen tilbake til «Mine anmeldelser», som
     * viser den endrede anmeldelsen. Ved feil vises en melding, og skjemaet beholdes.
     */
    @FXML
    private void handleSave() {
        if (taskRunner.runningProperty().get()) {
            return;
        }
        if (review == null) {
            showErrorMessage("Ingen anmeldelse er valgt.");
            return;
        }

        Review original = review;
        String rating = ratingField.getText();
        String comment = commentArea.getText();
        boolean anonymous = anonymousCheckBox.isSelected();

        statusLabel.setTextFill(Color.BLACK);
        statusLabel.setText("Lagrer endringen ...");

        taskRunner.execute(() -> reviewService.updateReview(original, rating, comment, anonymous),
                updated -> navigation.showMyReviews(saveButton), error -> {
                    if (error instanceof ReviewNotFoundException) {
                        showErrorMessage("Anmeldelsen finnes ikke lenger.");
                    } else if (error instanceof IllegalArgumentException
                            || error instanceof NotReviewOwnerException) {
                        showErrorMessage(error.getMessage());
                    } else {
                        showErrorMessage("Kunne ikke lagre endringen. Prøv igjen senere.");
                    }
                });
    }

    /**
     * Henter anmeldelsen som endres.
     *
     * @return anmeldelsen, eller null hvis ingen er valgt
     */
    public Review getReview() {
        return review;
    }

    /**
     * Avbryter ventingen og åpner profilen.
     */
    @FXML
    private void handleProfile() {
        taskRunner.cancel();
        navigation.showComingSoon(profileButton, "Profil");
    }

    /**
     * Avbryter ventingen og går tilbake til «Mine anmeldelser» uten å lagre.
     */
    @FXML
    private void handleBack() {
        taskRunner.cancel();
        navigation.showMyReviews(backButton);
    }

    /**
     * Avbryter ventingen og går til hjemmesiden uten å lagre.
     */
    @FXML
    private void handleHome() {
        taskRunner.cancel();
        navigation.showHome(homeButton);
    }

    private void showErrorMessage(String message) {
        statusLabel.setTextFill(Color.RED);
        statusLabel.setText(message);
    }
}