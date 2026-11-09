package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Tester hvilken skjerm BIThub viser ved oppstart.
 */
class AppStartupTest extends ApplicationTest {

    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        new App().start(stage);
    }

    @Test
    void startsOnLoginScreen() {
        assertNotNull(lookup("#emailField").queryAs(TextField.class));
        assertNotNull(lookup("#loginButton").queryAs(Button.class));
    }

    @Test
    void windowTitleIsBitHub() {
        assertEquals("BIThub", stage.getTitle());
    }
}
