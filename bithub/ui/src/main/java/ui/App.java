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

    private final UserSession userSession = new UserSession();

    /**
     * Kobler appen til REST-serveren og viser innloggingsskjermen.
     *
     * <p>Applikasjonens tjenester og sesjon opprettes og kobles sammen her. Resten av UI bruker
     * tjenestene uten å kjenne til hvordan HTTP-kommunikasjonen utføres.
     *
     * <p>Serveradressen er {@code http://localhost:8080}, men kan endres med
     * {@code -Dbithub.server-url=...} ved oppstart.
     *
     * @param stage hovedvinduet til applikasjonen
     * @throws IOException hvis innloggingsskjermen ikke kan lastes
     */
    @Override
    public void start(Stage stage) throws IOException {
        String baseUrl = System.getProperty("bithub.server-url",
                RemoteReviewRepository.DEFAULT_BASE_URL);

        RemoteReviewRepository repository = new RemoteReviewRepository(baseUrl);
        ReviewService reviewService = new ReviewService(repository);
        Navigation navigation = new Navigation(reviewService, repository);

        AuthenticationClient authenticationClient = new RemoteAuthenticationClient(baseUrl);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoggInn.fxml"));
        final Parent root = loader.load();

        LoggInnController controller = loader.getController();
        controller.setAuthenticationClient(authenticationClient);
        controller.setNavigation(navigation);

        controller.setLoginSuccessHandler(result -> {
            userSession.signIn(result.token(), result.name(), result.email());
            repository.setToken(userSession.getToken());
        });

        stage.setScene(new Scene(root));
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
