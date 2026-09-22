package ui;

import core.Course;
import java.io.IOException;
import java.util.List;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/** Kontroller for den opprinnelige faglisten i App.fxml. */
public class AppController {

  private List<Course> courses;

  @FXML
  private ListView<String> courseListView;

  @FXML
  private Label selectedCourseLabel;

  @FXML
  void initialize() {
    /*Oppretter en liste med gyldig fag*/
    courses = List.of(
        new Course("IT1901", "Informatikk prosjektarbeid"),
        new Course("TDT4100", "Objektorientert programmering"),
        new Course("TDT4160", "Datamaskiner og digitalteknikk")
    );

    for (Course course : courses) {
      /*Lager en string med fagkode og navn for vært gyldig fag*/
      String courseText =
          course.getCourseCode() + " - " + course.getCourseName();

      /*Legger til stringen i ListView, som er det brukeren ser */
      courseListView.getItems().add(courseText);
    }
  }

  /**
   * Oppdaterer valgt fag i grensesnittet. 
   * Kan kalles direkte fra tester uten å sende et MouseEvent.
   */
  public void handleCourseClick() {
    String selectedCourse = courseListView.getSelectionModel().getSelectedItem();
    if (selectedCourse != null) {
      selectedCourseLabel.setText("Valgt fag: " + selectedCourse);
    }
  }

  /*
  Trigges av museklikk i GUI-et. Oppdaterer etikett og håndterer dobbeltklikk for scenebytte.
  */
  @FXML
  public void handleCourseClick(MouseEvent event) {
    // Kaller hjelpemetoden for å oppdatere selectedCourseLabel
    handleCourseClick();

    String selectedCourse = courseListView.getSelectionModel().getSelectedItem();

    /* Hvis dobbeltklikk med primærknapp */
    if (selectedCourse != null && event != null 
        && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
      try {
        // Finner plassering til EmneView.fxml og bygger grensesnittet
        FXMLLoader loader = new FXMLLoader(getClass().getResource("EmneView.fxml"));
        Parent root = loader.load();

        // Loader controller og setter Labelen emneTittelLabel til valgt emne 
        EmneController controller = loader.getController();
        controller.setEmneData(selectedCourse);

        // Bytter ut det nåværende skjermbildet med det nye grensesnittet
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();

      } catch (IOException e) {
        System.err.println("Kunne ikke laste EmneView.fxml: " + e.getMessage());
      }
    }
  }
}
