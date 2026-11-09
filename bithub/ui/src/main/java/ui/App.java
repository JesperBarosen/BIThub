package ui;

import core.service.ReviewService;
import javafx.application.Application;
import javafx.stage.Stage;
import ui.client.AuthenticationClient;
import ui.client.RemoteAuthenticationClient;
import ui.client.RemoteReviewRepository;
import ui.session.UserSession;

/**
 * Starter BIThub-applikasjonen.
 */
public class App extends Application {

    /**
     * Kobler appen til REST-serveren og viser innloggingsskjermen.
     *
     * <p>Applikasjonens tjenester og sesjon opprettes og kobles sammen her. Resten av UI bruker
     * tjenestene uten å kjenne til hvordan HTTP-kommunikasjonen utføres. Når brukeren logger inn
     * eller registrerer seg, gir sesjonen tokenet videre til {@link RemoteReviewRepository}, slik
     * at nye anmeldelser sendes med token.
     *
     * <p>Serveradressen er {@code http://localhost:8080}, men kan endres med
     * {@code -Dbithub.server-url=...} ved oppstart.
     *
     * @param stage hovedvinduet til applikasjonen
     */
    @Override
    public void start(Stage stage) {
        String baseUrl = System.getProperty("bithub.server-url",
                RemoteReviewRepository.DEFAULT_BASE_URL);

        RemoteReviewRepository repository = new RemoteReviewRepository(baseUrl);
        ReviewService reviewService = new ReviewService(repository);
        UserSession userSession = new UserSession(repository::setToken);
        AuthenticationClient authenticationClient = new RemoteAuthenticationClient(baseUrl);
        Navigation navigation = new Navigation(reviewService, repository, userSession,
                authenticationClient);

        stage.setTitle("BIThub");
        navigation.showLogin(stage);
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