package ui;

import core.Course;
import core.NotReviewOwnerException;
import core.Review;
import core.ReviewNotFoundException;
import core.ReviewService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Kontroller for «Mine anmeldelser», der den innloggede brukeren ser, endrer og sletter sine egne
 * anmeldelser.
 *
 * <p>Anmeldelsene hentes og slettes i bakgrunnen med {@link BackgroundTaskRunner}, slik at skjermen
 * ikke fryser mens appen venter på serveren.
 */
public class MineAnmeldelserController {

    @FXML
    private Label countLabel;

    @FXML
    private ListView<OwnReview> reviewList;

    @FXML
    private Label statusLabel;

    @FXML
    private Button profileButton;

    @FXML
    private Button backButton;

    @FXML
    private Button homeButton;

    private ReviewService reviewService;
    private Navigation navigation;
    private UserSession userSession = new UserSession();
    private Predicate<String> deleteConfirmation = MineAnmeldelserController::confirmWithDialog;

    private final BackgroundTaskRunner taskRunner = new BackgroundTaskRunner();

    /**
     * En av brukerens anmeldelser sammen med emnet den hører til.
     *
     * @param courseCode emnekoden
     * @param courseName navnet på emnet
     * @param review     anmeldelsen
     */
    record OwnReview(String courseCode, String courseName, Review review) {
    }

    /**
     * Setter opp lista slik at hver anmeldelse vises som et kort med knapper for å endre og slette,
     * og hindrer nye handlinger mens et kall pågår.
     */
    @FXML
    private void initialize() {
        reviewList.setPlaceholder(new Label("Du har ikke skrevet noen anmeldelser ennå."));
        reviewList.disableProperty().bind(taskRunner.runningProperty());
        reviewList.setCellFactory(listView -> new ListCell<>() {
            /**
             * Viser kortet for anmeldelsen i cellen, eller ingenting hvis cellen er tom.
             *
             * @param ownReview anmeldelsen cellen skal vise
             * @param empty     {@code true} hvis cellen ikke har innhold
             */
            @Override
            protected void updateItem(OwnReview ownReview, boolean empty) {
                super.updateItem(ownReview, empty);
                setText(null);
                if (empty || ownReview == null) {
                    setGraphic(null);
                } else {
                    VBox card = createReviewCard(ownReview);
                    card.maxWidthProperty().bind(listView.widthProperty().subtract(40));
                    setGraphic(card);
                }
            }
        });
    }

    /**
     * Setter tjenesten kontrolleren henter, endrer og sletter anmeldelser med.
     *
     * @param reviewService tjenesten for anmeldelser
     * @throws NullPointerException hvis reviewService er null
     */
    public void setReviewService(ReviewService reviewService) {
        this.reviewService = Objects.requireNonNull(reviewService);
    }

    /**
     * Setter sesjonen med den innloggede brukeren.
     *
     * @param userSession sesjonen som husker den innloggede brukeren
     * @throws NullPointerException hvis userSession er null
     */
    public void setUserSession(UserSession userSession) {
        this.userSession = Objects.requireNonNull(userSession);
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
     * Bytter ut spørsmålet som vises før en anmeldelse slettes. Brukes av testene, slik at de ikke
     * må lukke en dialogboks.
     *
     * @param deleteConfirmation får spørsmålet og svarer true hvis brukeren vil slette
     * @throws NullPointerException hvis deleteConfirmation er null
     */
    void setDeleteConfirmation(Predicate<String> deleteConfirmation) {
        this.deleteConfirmation = Objects.requireNonNull(deleteConfirmation);
    }

    /**
     * Henter og viser anmeldelsene til den innloggede brukeren. Tjenesten og sesjonen må være satt
     * først.
     *
     * @throws IllegalStateException hvis tjenesten ikke er satt
     */
    public void loadReviews() {
        if (reviewService == null) {
            throw new IllegalStateException("ReviewService må settes før anmeldelsene hentes.");
        }
        taskRunner.cancel();
        reviewList.getItems().clear();
        countLabel.setText("-");

        if (!userSession.isLoggedIn()) {
            showErrorMessage("Du må logge inn for å se dine anmeldelser.");
            return;
        }

        String ownerEmail = userSession.getEmail();
        showInfoMessage("Henter anmeldelsene ...");

        taskRunner.execute(() -> reviewService.findReviewsByOwner(ownerEmail), courses -> {
            List<OwnReview> ownReviews = new ArrayList<>();
            for (Course course : courses) {
                for (Review review : course.getReviews()) {
                    ownReviews.add(
                            new OwnReview(course.getCourseCode(), course.getCourseName(), review));
                }
            }
            reviewList.getItems().setAll(ownReviews);
            countLabel.setText(String.valueOf(ownReviews.size()));
            statusLabel.setText("");
        }, error -> showErrorMessage("Kunne ikke hente anmeldelsene. Prøv igjen senere."));
    }

    /**
     * Lager visningen av én anmeldelse: emnet, vurderingen, kommentaren, om den er anonym, og
     * knapper for å endre og slette.
     *
     * @param ownReview anmeldelsen som skal vises
     * @return en boks med anmeldelsen og knappene
     */
    private VBox createReviewCard(OwnReview ownReview) {
        Review review = ownReview.review();
        Label course = new Label(ownReview.courseCode() + " - " + ownReview.courseName());
        course.getStyleClass().add("anmeldelse-navn");
        course.setWrapText(true);

        Label rating = new Label("VURDERING: " + review.getRating() + "/5");
        Label shownAs = new Label(
                review.isAnonymous() ? "Vises som: Anonym" : "Vises som: " + userSession.getName());

        VBox card = new VBox(4, course, rating, shownAs);
        card.getStyleClass().add("anmeldelse-kort");

        String comment = review.getComment();
        if (comment != null && !comment.isBlank()) {
            Label commentText = new Label(comment);
            commentText.setWrapText(true);
            card.getChildren().addAll(new Label("KOMMENTAR:"), commentText);
        }

        Button editButton = new Button("ENDRE ✎");
        editButton.setId("editButton");
        editButton.setOnAction(event -> {
            taskRunner.cancel();
            navigation.showEditReview(editButton, ownReview.courseCode(), ownReview.courseName(),
                    review);
        });

        Button deleteButton = new Button("SLETT");
        deleteButton.setId("deleteButton");
        deleteButton.setOnAction(event -> deleteReview(ownReview));

        card.getChildren().add(new HBox(10, editButton, deleteButton));
        return card;
    }

    /**
     * Spør brukeren om anmeldelsen skal slettes, og sletter den i bakgrunnen hvis svaret er ja.
     * Anmeldelsen fjernes fra lista først når serveren har bekreftet slettingen.
     *
     * @param ownReview anmeldelsen som skal slettes
     */
    private void deleteReview(OwnReview ownReview) {
        if (taskRunner.runningProperty().get()) {
            return;
        }
        String question = "Vil du slette anmeldelsen av " + ownReview.courseCode() + "?";
        if (!deleteConfirmation.test(question)) {
            return;
        }

        Review review = ownReview.review();
        showInfoMessage("Sletter anmeldelsen ...");

        taskRunner.execute(() -> {
            reviewService.deleteReview(review);
            return review;
        }, deleted -> {
            reviewList.getItems().remove(ownReview);
            countLabel.setText(String.valueOf(reviewList.getItems().size()));
            showSuccessMessage("Anmeldelsen ble slettet.");
        }, error -> {
            if (error instanceof ReviewNotFoundException) {
                reviewList.getItems().remove(ownReview);
                countLabel.setText(String.valueOf(reviewList.getItems().size()));
                showErrorMessage("Anmeldelsen finnes ikke lenger.");
            } else if (error instanceof NotReviewOwnerException) {
                showErrorMessage(error.getMessage());
            } else {
                showErrorMessage("Kunne ikke slette anmeldelsen. Prøv igjen senere.");
            }
        });
    }

    /**
     * Viser en dialogboks som spør om brukeren vil slette anmeldelsen.
     *
     * @param question spørsmålet som vises
     * @return true hvis brukeren trykket OK
     */
    private static boolean confirmWithDialog(String question) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, question, ButtonType.OK,
                ButtonType.CANCEL);
        alert.setTitle("Slett anmeldelse");
        alert.setHeaderText(null);
        Optional<ButtonType> answer = alert.showAndWait();
        return answer.isPresent() && answer.get() == ButtonType.OK;
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
     * Avbryter ventingen og går tilbake til hjemmesiden, der «Mine anmeldelser» ble åpnet fra.
     */
    @FXML
    private void handleBack() {
        taskRunner.cancel();
        navigation.showHome(backButton);
    }

    /**
     * Avbryter ventingen og går til hjemmesiden.
     */
    @FXML
    private void handleHome() {
        taskRunner.cancel();
        navigation.showHome(homeButton);
    }

    /**
     * Viser en informasjonsmelding i svart.
     *
     * @param message meldingen som skal vises
     */
    private void showInfoMessage(String message) {
        statusLabel.setTextFill(Color.BLACK);
        statusLabel.setText(message);
    }

    /**
     * Viser en bekreftelse i grønt.
     *
     * @param message meldingen som skal vises
     */
    private void showSuccessMessage(String message) {
        statusLabel.setTextFill(Color.GREEN);
        statusLabel.setText(message);
    }

    /**
     * Viser en feilmelding i rødt.
     *
     * @param message feilmeldingen som skal vises
     */
    private void showErrorMessage(String message) {
        statusLabel.setTextFill(Color.RED);
        statusLabel.setText(message);
    }
}