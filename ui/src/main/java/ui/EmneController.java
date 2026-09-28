package ui;

import core.Course;
import core.Review;
import file.ReviewHandler;
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
  private ReviewHandler reviewHandler;

  @FXML
  void initialize() {
    this.reviewHandler = new ReviewHandler();
  }

  /**
   * Viser et emne angitt som tekst ("KODE - NAVN").
   *
   * @param emneData emnekode og emnenavn skilt med " - "
   */
  public void setEmneData(String emneData) {
    if (emneData == null || emneData.isBlank()) return;

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
    String ratingInput = ratingField.getText() != null ? ratingField.getText().trim() : "";
    String commentInput = commentArea.getText() != null ? commentArea.getText().trim() : "";

    int rating;
    try {
      rating = Integer.parseInt(ratingInput);
      if (rating < 1 || rating > 5) {
        showErrorMessage("Vurdering må være et tall fra 1 til 5.");
        return;
      }
    } catch (NumberFormatException e) {
      showErrorMessage("Vurdering må være et gyldig tall.");
      return;
    }

    String reviewerName = anonymousCheckBox.isSelected() ? "Anonym" : "Student";

    try {
      Review newReview = new Review(reviewerName, rating, commentInput);
      reviewHandler.addReview(this.courseCode, this.courseName, newReview);

      showSuccessMessage("Vurderingen ble sendt inn!");
      clearFields();
    } catch (Exception e) {
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

  public String getCourseCode() {
    return courseCode;
  }

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