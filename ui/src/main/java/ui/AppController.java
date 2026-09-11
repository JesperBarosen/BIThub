package ui;

import core.Course;
import java.util.List;
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
    courses = List.of(
        new Course("IT1901", "Informatikk prosjektarbeid"),
        new Course("TDT4100", "Objektorientert programmering"),
        new Course("TDT4160", "Datamaskiner og digitalteknikk")
    );

    for (Course course : courses) {
      String courseText =
          course.getCourseCode() + " – " + course.getCourseName();

      courseListView.getItems().add(courseText);
    }
  }

  @FXML
  void handleCourseClick() {
    String selectedCourse =
        courseListView.getSelectionModel().getSelectedItem();

    if (selectedCourse != null) {
      selectedCourseLabel.setText("Valgt fag: " + selectedCourse);
    }
  }
}