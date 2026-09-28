package ui;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Starter BIThub-applikasjonen. */
public class App extends Application {

  /**
   * Viser hjemmesiden når JavaFX starter.
   *
   * @param stage hovedvinduet til applikasjonen
   * @throws IOException hvis hjemmesiden ikke kan lastes
   */
  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("Hjemmeside.fxml"));
    Parent parent = fxmlLoader.load();
    stage.setScene(new Scene(parent));
    stage.show();
  }

  /**
   * Starter JavaFX-applikasjonen.
   *
   * @param args argumenter fra kommandolinjen
   */
  public static void main(String[] args) {
    launch(args);
  }
}
