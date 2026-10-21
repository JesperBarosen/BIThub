package ui;

import core.ReviewService;
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
     * Kobler appen til REST-serveren og viser hjemmesiden når JavaFX starter. Dette er det eneste
     * stedet i UI som vet at dataene hentes fra serveren. Resten av UI bruker bare ReviewService og
     * ReviewRepository fra core.
     *
     * <p>Serveradressen er {@code http://localhost:8080}, men kan endres med
     * {@code -Dbithub.server-url=...} når appen startes. Standardemnene legges inn av serveren.
     *
     * @param stage hovedvinduet til applikasjonen
     * @throws IOException hvis hjemmesiden ikke kan lastes
     */
    @Override
    public void start(Stage stage) throws IOException {
        RemoteReviewRepository repository = new RemoteReviewRepository(
                System.getProperty("bithub.server-url", RemoteReviewRepository.DEFAULT_BASE_URL));
        ReviewService reviewService = new ReviewService(repository);
        Navigation navigation = new Navigation(reviewService, repository);

        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("Hjemmeside.fxml"));
        Parent parent = fxmlLoader.load();

        HjemmesideController controller = fxmlLoader.getController();
        controller.setNavigation(navigation);
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