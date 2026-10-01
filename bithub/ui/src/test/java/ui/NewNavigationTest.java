package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import core.Course;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/** Tester navigasjonen i de nye FXML-skjermbildene. */
class NewNavigationTest extends ApplicationTest {

  private Stage stage;

  @Override
  public void start(Stage stage) throws Exception {
    this.stage = stage;
    new App().start(stage);
  }

  @Test
  void canChooseCourseAndReturnHome() {
    interact(() -> button("#writeReviewButton").fire());

    @SuppressWarnings("unchecked")
    ComboBox<Course> courseComboBox =
        (ComboBox<Course>) stage.getScene().getRoot().lookup("#courseComboBox");
    assertNotNull(courseComboBox);
    assertEquals(5, courseComboBox.getItems().size());

    interact(() -> {
      courseComboBox.getSelectionModel().select(4);
      button("#nextButton").fire();
    });

    Label title = (Label) stage.getScene().getRoot().lookup("#emneTittelLabel");
    assertNotNull(title);
    assertEquals("IT1901 - Informatikk prosjektarbeid I", title.getText());

    interact(() -> button("#backButton").fire());
    assertNotNull(stage.getScene().getRoot().lookup("#courseComboBox"));

    interact(() -> button("#homeButton").fire());
    assertNotNull(stage.getScene().getRoot().lookup("#writeReviewButton"));
  }

  @Test
  void unimplementedPageShowsPlaceholder() {
    interact(() -> button("#readReviewsButton").fire());
    Label heading = (Label) stage.getScene().getRoot().lookup("#placeholderHeading");
    assertEquals("Les anmeldelser", heading.getText());
  }

  private Button button(String selector) {
    return (Button) stage.getScene().getRoot().lookup(selector);
  }
}
