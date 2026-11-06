package ui.controller;

import core.model.Course;
import core.service.ReviewService;
import java.util.Objects;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import org.controlsfx.control.Rating;
import ui.Navigation;
import ui.session.UserSession;
import ui.util.BackgroundTaskRunner;

/**
 * Kontroller for skjemaet der brukeren skriver og sender inn en anmeldelse.
 */
public class WriteReviewController {

    @FXML
    private Label courseTitleLabel;

    @FXML
    private Rating ratingStars;

    @FXML
    private TextArea commentArea;

    @FXML
    private CheckBox anonymousCheckBox;

    @FXML
    private Label statusLabel;

    @FXML
    private Button submitButton;

    @FXML
    private Button backButton;

    @FXML
    private Button homeButton;

    private String courseCode = "";
    private String courseName = "";
    private ReviewService reviewService;
    @FXML
    private Button profileButton;

    private Navigation navigation;
    private UserSession userSession = new UserSession();

    private final BackgroundTaskRunner taskRunner = new BackgroundTaskRunner();

    /**
     * Deaktiverer innsending og redigering mens en anmeldelse sendes.
     */
    @FXML
    private void initialize() {
        submitButton.disableProperty().bind(taskRunner.runningProperty());
        ratingStars.disableProperty().bind(taskRunner.runningProperty());
        commentArea.disableProperty().bind(taskRunner.runningProperty());
        anonymousCheckBox.disableProperty().bind(taskRunner.runningProperty());
    }

    /**
     * Setter tjenesten kontrolleren skal bruke når brukeren sender inn en anmeldelse. Kontrolleren
     * oppretter ikke lagring selv, den får den utenfra.
     *
     * @param reviewService tjenesten som validerer og lagrer anmeldelser
     * @throws NullPointerException hvis reviewService er null
     */
    public void setReviewService(ReviewService reviewService) {
        this.reviewService = Objects.requireNonNull(reviewService);
    }

    /**
     * Setter sesjonen med den innloggede brukeren. Uten innlogget bruker sendes ingen anmeldelse,
     * siden gjester bare kan lese anmeldelser.
     *
     * @param userSession sesjonen som husker den innloggede brukeren
     * @throws NullPointerException hvis userSession er null
     */
    public void setUserSession(UserSession userSession) {
        this.userSession = Objects.requireNonNull(userSession);
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
     * Viser et emne angitt som tekst ("KODE - NAVN").
     *
     * @param courseData emnekode og emnenavn skilt med " - "
     */
    public void setCourseData(String courseData) {
        if (courseData == null || courseData.isBlank()) {
            return;
        }

        String[] parts = courseData.split(" - ", 2);
        if (parts.length == 2) {
            setCourse(new Course(parts[0].trim(), parts[1].trim()));
        } else {
            this.courseName = courseData.trim();
            courseTitleLabel.setText(courseData);
        }
    }

    /**
     * Viser det valgte emnet på anmeldelsessiden.
     *
     * @param course emnet som ble valgt
     * @throws IllegalArgumentException hvis emnet er null
     */
    public void setCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Emnet kan ikke være null.");
        }
        this.courseCode = course.getCourseCode();
        this.courseName = course.getCourseName();
        courseTitleLabel.setText(courseCode + " - " + courseName);
    }

    /**
     * Sender anmeldelsen i bakgrunnen uten å blokkere JavaFX-tråden.
     *
     * <p>Avkrysningen «Anonym?» sendes med, og serveren kobler anmeldelsen til brukeren ut fra
     * tokenet. Uten innlogging vises en melding om å logge inn, og uten valgt stjerne vises en
     * melding om å velge en vurdering. I begge tilfellene sendes ingenting. Skjemainnholdet
     * beholdes ved feil og tømmes først etter bekreftet innsending.
     */
    @FXML
    private void handleSubmit() {
        if (taskRunner.runningProperty().get()) {
            return;
        }

        if (!userSession.isLoggedIn()) {
            showErrorMessage("Du må logge inn for å skrive en anmeldelse.");
            return;
        }

        // Rating gir et desimaltall, men uten halve stjerner er det alltid et helt tall.
        int selectedStars = (int) Math.round(ratingStars.getRating());
        if (selectedStars < 1) {
            showErrorMessage("Velg en vurdering ved å trykke på stjernene.");
            return;
        }

        String ownerEmail = userSession.getEmail();
        String reviewerName = userSession.getName();
        boolean anonymous = anonymousCheckBox.isSelected();
        String selectedCourseCode = courseCode;
        String selectedCourseName = courseName;
        String rating = String.valueOf(selectedStars);
        String comment = commentArea.getText();

        statusLabel.setTextFill(Color.BLACK);
        statusLabel.setText("Sender anmeldelsen ...");

        taskRunner.execute(() -> reviewService.submitReview(selectedCourseCode, selectedCourseName,
                ownerEmail, reviewerName, anonymous, rating, comment), review -> {
                    showSuccessMessage("Vurderingen ble sendt inn!");
                    clearFields();
                }, error -> {
                    if (error instanceof IllegalArgumentException) {
                        showErrorMessage(error.getMessage());
                    } else {
                        showErrorMessage("Kunne ikke bekrefte at anmeldelsen ble lagret. "
                                + "Prøv igjen senere.");
                    }
                });
    }

    /**
     * Avbryter ventingen og går tilbake til emnevalget.
     */
    @FXML
    private void handleBack() {
        taskRunner.cancel();
        navigation.showCourseSelection(backButton);
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
     * Henter emnekoden som vises på siden.
     *
     * @return emnekoden, eller en tom streng hvis ingen emne er valgt
     */
    public String getCourseCode() {
        return courseCode;
    }

    /**
     * Henter emnenavnet som vises på siden.
     *
     * @return emnenavnet, eller en tom streng hvis ingen emne er valgt
     */
    public String getCourseName() {
        return courseName;
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

    /**
     * Tømmer skjemaet slik at en ny anmeldelse kan skrives.
     */
    private void clearFields() {
        ratingStars.setRating(0);
        commentArea.clear();
        anonymousCheckBox.setSelected(false);
    }
}