package ui;

import core.ReviewService;
import file.ReviewHandler;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Starter BIThub-applikasjonen.
 */
public class App extends Application {

    /**
     * Setter opp lagringen og viser hjemmesiden når JavaFX starter. Dette er
     * det eneste stedet i UI som vet at anmeldelser lagres med ReviewHandler.
     * Resten av UI bruker bare ReviewService fra core.
     *
     * @param stage hovedvinduet til applikasjonen
     * @throws IOException hvis hjemmesiden ikke kan lastes
     */
    @Override
    public void start(Stage stage) throws IOException {
        Navigation.setReviewService(new ReviewService(new ReviewHandler()));

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
