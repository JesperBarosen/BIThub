package ui;

import core.Review;
import file.ReviewHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

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

  private String courseCode = "";
  private String courseName = "";
  private ReviewHandler reviewHandler;

  @FXML
  void initialize() {
    // Initialiserer ReviewHandler som automatisk peker på user.home/.bithub/reviews.json
    this.reviewHandler = new ReviewHandler();
  }

  public void setEmneData(String emneData) {
    if (emneData == null || emneData.isBlank()) return;

    String[] parts = emneData.split(" - ", 2);
    if (parts.length == 2) {
      this.courseCode = parts[0].trim();
      this.courseName = parts[1].trim();
    } else {
      this.courseName = emneData.trim();
    }

    emneTittelLabel.setText(emneData);
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
    // 1. Oppretter et Review-objekt
    Review newReview = new Review(reviewerName, rating, commentInput);

    // 2. Sender inn emnekode, emnenavn og anmeldelsen til handleren
    reviewHandler.addReview(this.courseCode, this.courseName, newReview);

    showSuccessMessage("Vurderingen ble sendt inn!");
    clearFields();
  } catch (Exception e) {
    showErrorMessage("Feil ved lagring: Kunne ikke skrive til fil.");
  }
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