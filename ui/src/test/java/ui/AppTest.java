package ui;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;


/**
 * TestFX App test suite.
 */
class AppTest extends ApplicationTest {

  private Parent root;

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("App.fxml"));
    root = fxmlLoader.load();
    stage.setScene(new Scene(root));
    stage.show();
  }

  @Test 
  void testAppStarts() {
    Assertions.assertNotNull(root);
   }

   @Test 
   void testCourseListIsNotEmpty() { 
    @SuppressWarnings("unchecked")
    ListView<String> courseListView = (ListView<String>) root.lookup("#courseListView");
    Assertions.assertFalse(courseListView.getItems().isEmpty());
   }
}
