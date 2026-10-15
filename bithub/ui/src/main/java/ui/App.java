package ui;

import core.DefaultCourses;
import core.ReviewService;
import file.ReviewHandler;
import java.io.IOException;
import java.io.UncheckedIOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Starter BIThub-applikasjonen.
 */
public class App extends Application {

    /**
     * Setter opp lagringen og viser hjemmesiden når JavaFX starter. Dette er det eneste stedet i UI
     * som vet at anmeldelser lagres med ReviewHandler. Resten av UI bruker bare ReviewService fra
     * core.
     *
     * @param stage hovedvinduet til applikasjonen
     * @throws IOException hvis hjemmesiden ikke kan lastes
     */
    @Override
    public void start(Stage stage) throws IOException {
        ReviewHandler reviewHandler = new ReviewHandler();
        // Standardemnene legges inn ved første oppstart, uten å endre lagrede anmeldelser.
        try {
            reviewHandler.addCoursesIfMissing(DefaultCourses.all());
        } catch (UncheckedIOException e) {
            new Alert(Alert.AlertType.ERROR,
                    "Kunne ikke lese lagrede anmeldelser. Prøv å starte appen på nytt.").show();
        }
        Navigation.setReviewRepository(reviewHandler);
        Navigation.setReviewService(new ReviewService(reviewHandler));

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