package ui;

import core.Course;
import core.FileHandler;
import core.Review;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

/** Kontroller for skjemaet der brukeren skriver en anmeldelse. */
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
  private Label statusLabel; // Viser tilbakemelding til brukeren

  @FXML
  private Button backButton;

  @FXML
  private Button homeButton;

  private String courseCode = "";
  private String courseName = "";
  private FileHandler fileHandler;
  private static final String FILENAME = "data/reviews.csv";

  @FXML
  void initialize() {
    fileHandler = new FileHandler(FILENAME);

    // Oppretter filen med overskrifter dersom den ikke finnes fra før
    if (fileHandler.isEmpty()) {
      fileHandler.addToFile(new String[] { "CourseCode", "CourseName", "ReviewerName", "Rating", "Comment" });
    }
  }

  /**
   * Viser et emne angitt som tekst. Brukes fortsatt av den gamle visningen.
   *
   * @param emneData emnekode og emnenavn skilt med " - "
   */
  public void setEmneData(String emneData) {
    // Avbryter resten av metoden hvis argumentet er tomt
    if (emneData == null || emneData.isBlank()) {
        return;
    }

    // Henter ut emnekode og navnet på emnet
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
   * @param course emnet som ble valgt i fagvelgeren
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
    /*
     * Sjekker om feltene er tomme og setter "" i tilfelle. Ellers henter tekst og
     * trimmer.
     */
    String ratingInput = ratingField.getText() != null ? ratingField.getText().trim() : "";
    String commentInput = commentArea.getText() != null ? commentArea.getText().trim() : "";

    // Validerer vurdering (1-5)
    try {
      int rating = Integer.parseInt(ratingInput);
      if (rating < 1 || rating > 5) {
        showErrorMessage("Vurdering må være et tall fra 1 til 5.");
        return;
      }
    } catch (NumberFormatException e) {
      showErrorMessage("Vurdering må være et gyldig tall.");
      return;
    }

    /*
     * Legger bare til "Student" i stedet for brukernavn siden innlogging ikke er
     * implementert ennå.
     */
    String reviewerName = anonymousCheckBox.isSelected() ? "Anonym" : "Student";
    Review review = new Review(reviewerName, Integer.parseInt(ratingInput), commentInput);

    /*
     * - Fjerner linjeskift for å unngå korrupt CSV - Erstatter doble hermetegn
     * " med doble hermetegn "" for å unngå feil ved lesing - Pakker alt inn i ""
     * før det sendes inn i CSV-filen
     */
    String formattedComment = "\"" + review.getComment().replace("\"", "\"\"").replace("\r\n", " ").replace("\n", " ")
        + "\"";

    // Matrisen som skal sendes til FileHandler
    String[] reviewLine = new String[] { courseCode, courseName, review.getReviewerName(),
        Integer.toString(review.getRating()), formattedComment };

    try {
      fileHandler.addToFile(reviewLine);
      showSuccessMessage("Vurderingen ble sendt inn!");
      clearFields();
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
