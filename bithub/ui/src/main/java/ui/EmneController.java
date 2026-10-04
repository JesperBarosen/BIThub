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
public class EmneController {

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
    private Button backButton;

    @FXML
    private Button homeButton;

    private String courseCode = "";
    private String courseName = "";
    private ReviewService reviewService;

    /**
     * Setter tjenesten kontrolleren skal bruke når brukeren sender inn en
     * anmeldelse. Kontrolleren oppretter ikke lagring selv, den får den
     * utenfra.
     *
     * @param reviewService tjenesten som validerer og lagrer anmeldelser
     * @throws NullPointerException hvis reviewService er null
     */
    public void setReviewService(ReviewService reviewService) {
        this.reviewService = Objects.requireNonNull(reviewService);
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

    @FXML
    private void handleSendInn() {
        String reviewerName = anonymousCheckBox.isSelected() ? "Anonym" : "Student";

        try {
            reviewService.submitReview(courseCode, courseName, reviewerName,
                    ratingField.getText(), commentArea.getText());

            showSuccessMessage("Vurderingen ble sendt inn!");
            clearFields();
        } catch (IllegalArgumentException e) {
            // Valideringsfeil fra core, for eksempel ugyldig vurdering
            showErrorMessage(e.getMessage());
        } catch (RuntimeException e) {
            showErrorMessage("Feil ved lagring: Kunne ikke skrive til fil.");
        }
    }

    @FXML
    private void handleBack() {
        Navigation.showCourseSelection(backButton);
    }

    @FXML
    private void handleHome() {
        Navigation.showHome(homeButton);
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
