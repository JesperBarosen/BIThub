package ui.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import core.service.ReviewService;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import ui.Navigation;
import ui.client.InMemoryReviewRepository;

/**
 * Tester at infosiden åpnes med lenken «her.» fra innloggingen og hjemmesiden, og at «TILBAKE»
 * går til siden den ble åpnet fra.
 */
class AboutControllerTest extends ApplicationTest {

    private Stage stage;

    /**
     * Viser innloggingsskjermen med lagring i minnet, uten server.
     *
     * @param stage vinduet testene kjører i
     */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        InMemoryReviewRepository repository = new InMemoryReviewRepository();
        Navigation navigation = new Navigation(new ReviewService(repository), repository);
        navigation.showLogin(stage);
        stage.show();
    }

    /**
     * Verifiserer at infosiden åpnes fra innloggingen, og at «TILBAKE» viser innloggingen slik
     * den var, med e-posten som var skrevet inn.
     */
    @Test
    void opensFromLoginAndReturnsToLogin() {
        final Scene loginScene = stage.getScene();
        TextField emailField = (TextField) find("#emailField");
        interact(() -> emailField.setText("kari@ntnu.no"));

        openAbout();
        assertAboutIsShown();

        interact(() -> ((Button) find("#backButton")).fire());
        assertSame(loginScene, stage.getScene());
        assertEquals("kari@ntnu.no", ((TextField) find("#emailField")).getText());
    }

    /**
     * Verifiserer at infosiden åpnes fra hjemmesiden, og at «TILBAKE» viser hjemmesiden igjen.
     */
    @Test
    void opensFromHomeAndReturnsToHome() {
        interact(() -> ((Button) find("#guestButton")).fire());
        final Scene homeScene = stage.getScene();
        assertNotNull(find("#readReviewsButton"));

        openAbout();
        assertAboutIsShown();

        interact(() -> ((Button) find("#backButton")).fire());
        assertSame(homeScene, stage.getScene());
        assertNotNull(find("#readReviewsButton"));
    }

    private void openAbout() {
        Hyperlink aboutLink = (Hyperlink) find("#aboutLink");
        assertNotNull(aboutLink);
        interact(aboutLink::fire);
    }

    private void assertAboutIsShown() {
        assertNotNull(find("#backButton"));
        assertNull(find("#aboutLink"));
    }

    private Node find(String query) {
        return stage.getScene().getRoot().lookup(query);
    }
}