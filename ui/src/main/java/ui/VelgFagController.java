package ui;

import core.Course;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

/** Lar brukeren velge hvilket emne som skal anmeldes. */
public class VelgFagController {

  @FXML
  private ComboBox<Course> courseComboBox;

  @FXML
  private Label statusLabel;

  @FXML
  private Button nextButton;

  @FXML
  private Button backButton;

  @FXML
  private Button homeButton;

  @FXML
  private void initialize() {
    courseComboBox.setItems(FXCollections.observableArrayList(CourseOptions.availableCourses()));
    courseComboBox.setButtonCell(courseCell());
    courseComboBox.setCellFactory(listView -> courseCell());
  }

  @FXML
  private void handleNext() {
    Course selectedCourse = courseComboBox.getValue();
    if (selectedCourse == null) {
      statusLabel.setText("Velg et fag først.");
      return;
    }
    Navigation.showReviewForm(nextButton, selectedCourse);
  }

  @FXML
  private void handleBack() {
    Navigation.showHome(backButton);
  }

  @FXML
  private void handleHome() {
    Navigation.showHome(homeButton);
  }

  private ListCell<Course> courseCell() {
    return new ListCell<>() {
      @Override
      protected void updateItem(Course course, boolean empty) {
        super.updateItem(course, empty);
        setText(empty || course == null ? null
            : course.getCourseCode() + " - " + course.getCourseName());
      }
    };
  }
}
