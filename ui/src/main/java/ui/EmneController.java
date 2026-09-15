package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class EmneController {

  @FXML
  private Label emneTittelLabel;

  @FXML
  void initialize() {
    // Printer til console når controller initialiseres
    System.out.println("EmneController ble initialisert!");
  }

  // Metode som tar med emneNavn basert på hvilket emne klikkes på i AppController
  public void setEmneData(String emneNavn) {
    emneTittelLabel.setText("Detaljer for: " + emneNavn);
  }


}