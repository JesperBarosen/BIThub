package ui;

import core.FileHandler;
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
  private Label statusLabel; // Viser tilbakemelding til brukeren

  private String courseCode = "";
  private String courseName = "";
  private FileHandler fileHandler;
  private static final String FILENAME = "data/reviews.csv";

  @FXML
  void initialize() {
    fileHandler = new FileHandler(FILENAME);

    // Oppretter filen med overskrifter dersom den ikke finnes fra før
    if (fileHandler.isEmpty()) {
      fileHandler.addToFile(new String[]{"CourseCode", "CourseName", "ReviewerName", "Rating", "Comment"});
    }
  }

  public void setEmneData(String emneData) {
    // Avbryter resten av metoden hvis argumentet er tomt
    if (emneData == null || emneData.isBlank()) return;

    // Henter ut emnekode og navnet på emnet
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
    /* 
    Sjekker om feltene er tomme og setter "" i tilfelle.
    Ellers henter tekst og trimmer.
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
    Legger bare til "Student" i stedet for brukernavn 
    siden innlogging ikke er implementert ennå.
    */
    String reviewerName = anonymousCheckBox.isSelected() ? "Anonym" : "Student";
    
    /*
    - Fjerner linjeskift for å unngå korrupt CSV
    - Erstatter doble hermetegn " med doble hermetegn "" for å unngå feil ved lesing
    - Pakker alt inn i "" før det sendes inn i CSV-filen
    */
    String formattedComment = 
      "\"" + 
      commentInput
        .replace("\"", "\"\"")
        .replace("\r\n", " ")
        .replace("\n", " ") 
      + "\"";

    // Matrisen som skal sendes til FileHandler
    String[] reviewLine = new String[] {
      courseCode,
      courseName,
      reviewerName,
      ratingInput,
      formattedComment
    };

    try {
      fileHandler.addToFile(reviewLine);
      showSuccessMessage("Vurderingen ble sendt inn!");
      clearFields();
    } catch (RuntimeException e) {
      showErrorMessage("Feil ved lagring: Kunne ikke skrive til fil.");
    }
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