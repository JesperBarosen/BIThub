package ui;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

/**
 * TestFX test suite for the application's JavaFX user interface.
 *
 * <p>Verifies that the application starts successfully, that
 * the course list is populated, and that selecting a course
 * updates the user interface correctly.
 */
class AppTest extends ApplicationTest {

  private Parent root;

  /**
   * Loads the application's FXML file and displays the JavaFX scene
   * before each test is executed.
   *
   * @param stage the JavaFX stage used by TestFX
   * @throws IOException if the FXML file cannot be loaded
   */
  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("App.fxml"));
    root = fxmlLoader.load();
    stage.setScene(new Scene(root));
    stage.show();
  }

  /**
   * Tests that the application starts and successfully loads
   * the root JavaFX node.
   */
  @Test
  void testAppStarts() {
    Assertions.assertNotNull(root);
  }

  /**
   * Tests that the course list exists and contains at least one course
   * when the application is started.
   */
  @Test
  void testCourseListIsNotEmpty() {
    @SuppressWarnings("unchecked")
    ListView<String> courseListView =
        (ListView<String>) root.lookup("#courseListView");

    Assertions.assertNotNull(courseListView);
    Assertions.assertFalse(courseListView.getItems().isEmpty());
  }

  /**
   * Tests that selecting a course in the course list updates
   * the label showing the currently selected course.
   */
  @Test
  void testSelectingCourseUpdatesLabel() {
    @SuppressWarnings("unchecked")
    ListView<String> courseListView =
        (ListView<String>) root.lookup("#courseListView");

    Assertions.assertNotNull(courseListView);

    String firstCourse = courseListView.getItems().get(0);
    clickOn(firstCourse);

    Label selectedCourseLabel =
        (Label) root.lookup("#selectedCourseLabel");

    Assertions.assertNotNull(selectedCourseLabel);
    Assertions.assertEquals(
        "Valgt fag: " + firstCourse,
        selectedCourseLabel.getText()
    );
  }
}