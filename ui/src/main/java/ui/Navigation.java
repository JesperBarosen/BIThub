package ui;

import core.Course;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** Samler navigasjon mellom skjermbildene som finnes i appen. */
public final class Navigation {

  private Navigation() {
  }

  /**
   * Åpner hjemmesiden i det samme vinduet.
   *
   * @param source et element i skjermbildet som er åpent nå
   * @throws IllegalStateException hvis FXML-filen ikke kan lastes
   */
  public static void showHome(Node source) {
    showFxml(source, "Hjemmeside.fxml");
  }

  /**
   * Åpner fagvelgeren i det samme vinduet.
   *
   * @param source et element i skjermbildet som er åpent nå
   * @throws IllegalStateException hvis FXML-filen ikke kan lastes
   */
  public static void showCourseSelection(Node source) {
    showFxml(source, "VelgFag.fxml");
  }

  /**
   * Åpner anmeldelsessiden og gir det valgte emnet til kontrolleren.
   *
   * @param source et element i skjermbildet som er åpent nå
   * @param course emnet brukeren har valgt
   * @throws IllegalStateException hvis FXML-filen ikke kan lastes
   */
  public static void showReviewForm(Node source, Course course) {
    FXMLLoader loader = new FXMLLoader(Navigation.class.getResource("EmneView.fxml"));
    try {
      Parent root = loader.load();
      EmneController controller = loader.getController();
      controller.setCourse(course);
      stageOf(source).setScene(new Scene(root));
    } catch (IOException e) {
      throw new IllegalStateException("Kunne ikke åpne anmeldelsessiden", e);
    }
  }

  /**
   * Viser en midlertidig side for funksjoner som ikke er implementert.
   *
   * @param source et element i skjermbildet som er åpent nå
   * @param title overskriften som skal vises
   */
  public static void showComingSoon(Node source, String title) {
    Label heading = new Label(title);
    heading.setId("placeholderHeading");
    Label message = new Label("Denne siden er ikke laget ennå.");
    Button homeButton = new Button("HJEM");
    VBox root = new VBox(20, heading, message, homeButton);
    root.setStyle("-fx-alignment: center; -fx-padding: 30;");
    homeButton.setOnAction(event -> showHome(homeButton));
    stageOf(source).setScene(new Scene(root, 400, 600));
  }

  private static void showFxml(Node source, String fileName) {
    try {
      Parent root = FXMLLoader.load(Navigation.class.getResource(fileName));
      stageOf(source).setScene(new Scene(root));
    } catch (IOException e) {
      throw new IllegalStateException("Kunne ikke åpne " + fileName, e);
    }
  }

  private static Stage stageOf(Node source) {
    return (Stage) source.getScene().getWindow();
  }
}
