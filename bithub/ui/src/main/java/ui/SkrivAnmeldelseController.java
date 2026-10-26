package ui;

import core.Course;
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
 * Kontroller for skjemaet der brukeren skriver og sender inn en anmeldelse.
 */
public class SkrivAnmeldelseController {

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
    private Button sendInnButton;

    @FXML
    private Button backButton;

    @FXML
    private Button homeButton;

    private String courseCode = "";
    private String courseName = "";
    private ReviewService reviewService;
    private Navigation navigation;
    private UserSession userSession = new UserSession();

    private final BackgroundTaskRunner taskRunner = new BackgroundTaskRunner();

    /**
     * Deaktiverer innsending og redigering mens en anmeldelse sendes.
     */
    @FXML
    private void initialize() {
        sendInnButton.disableProperty().bind(taskRunner.runningProperty());
        ratingField.disableProperty().bind(taskRunner.runningProperty());
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
     * Setter navigasjonen som kontrolleren skal bruke.
     *
     * @param navigation navigasjonsobjektet
     */
    public void setNavigation(Navigation navigation) {
        this.navigation = Objects.requireNonNull(navigation);
    }

    /**
     * Viser et emne angitt som tekst ("KODE - NAVN").
     *
     * @param emneData emnekode og emnenavn skilt med " - "
     */
    public void setEmneData(String emneData) {
        if (emneData == null || emneData.isBlank()) {
            return;
        }

        String[] parts = emneData.split(" - ", 2);
        if (parts.length == 2) {
            setCourse(new Course(parts[0].trim(), parts[1].trim()));
        } else {
            this.courseName = emneData.trim();
            emneTittelLabel.setText(emneData);
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
            throw new IllegalArgumentException("Course cannot be null.");
        }
        this.courseCode = course.getCourseCode();
        this.courseName = course.getCourseName();
        emneTittelLabel.setText(courseCode + " - " + courseName);
    }

    /**
     * Sender anmeldelsen i bakgrunnen uten å blokkere JavaFX-tråden.
     *
     * <p>Avkrysningen «Anonym?» sendes med, og serveren kobler anmeldelsen til brukeren ut fra
     * tokenet. Uten innlogging vises en melding om å logge inn, og ingenting sendes.
     * Skjemainnholdet beholdes ved feil og tømmes først etter bekreftet innsending.
     */
    @FXML
    private void handleSendInn() {
        if (taskRunner.runningProperty().get()) {
            return;
        }

        if (!userSession.isLoggedIn()) {
            showErrorMessage("Du må logge inn for å skrive en anmeldelse.");
            return;
        }

        String ownerEmail = userSession.getEmail();
        String reviewerName = userSession.getName();
        boolean anonymous = anonymousCheckBox.isSelected();
        String selectedCourseCode = courseCode;
        String selectedCourseName = courseName;
        String rating = ratingField.getText();
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

    private void showSuccessMessage(String message) {
        statusLabel.setTextFill(Color.GREEN);
        statusLabel.setText(message);
    }

    private void showErrorMessage(String message) {
        statusLabel.setTextFill(Color.RED);
        statusLabel.setText(message);
    }

    private void clearFields() {
        ratingField.clear();
        commentArea.clear();
        anonymousCheckBox.setSelected(false);
    }
}