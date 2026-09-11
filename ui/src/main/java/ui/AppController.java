package ui;

import java.util.List;

import core.Course;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

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

  @FXML
  void handleCourseClick() {
    /* Lagrer markert fag*/
    String selectedCourse =
        courseListView.getSelectionModel().getSelectedItem();

    /*Hvis markert, så hvises markert fag */
    if (selectedCourse != null) {
      selectedCourseLabel.setText("Valgt fag: " + selectedCourse);
    }
  }
}