package ui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Tester at et passordfelt kan vises og skjules uten at teksten forsvinner.
 */
class PasswordVisibilityTest extends ApplicationTest {

    private CheckBox showPassword;
    private PasswordField hiddenField;
    private TextField shownField;

    @Override
    public void start(Stage stage) {
        showPassword = new CheckBox("Vis passord");
        hiddenField = new PasswordField();
        shownField = new TextField();
        PasswordVisibility.connect(showPassword, hiddenField, shownField);
    }

    @Test
    void passwordIsHiddenByDefault() {
        assertFalse(showPassword.isSelected());
        assertTrue(hiddenField.isVisible());
        assertTrue(hiddenField.isManaged());
        assertFalse(shownField.isVisible());
        assertFalse(shownField.isManaged());
    }

    @Test
    void textIsKeptWhenSwitchingBetweenHiddenAndShown() {
        interact(() -> {
            hiddenField.setText("Hemmelig123");
            showPassword.setSelected(true);
        });

        assertFalse(hiddenField.isVisible());
        assertTrue(shownField.isVisible());
        assertEquals("Hemmelig123", shownField.getText());

        interact(() -> {
            shownField.setText("Hemmelig456");
            showPassword.setSelected(false);
        });

        assertTrue(hiddenField.isVisible());
        assertEquals("Hemmelig456", hiddenField.getText());
    }

    @Test
    void shownFieldFollowsDisabledStateAndEnterAction() {
        AtomicInteger actions = new AtomicInteger();

        interact(() -> {
            hiddenField.setOnAction(event -> actions.incrementAndGet());
            shownField.fireEvent(new ActionEvent());
            hiddenField.setDisable(true);
        });

        assertTrue(shownField.isDisabled());
        assertEquals(1, actions.get());
    }

    @Test
    void rejectsMissingFields() {
        assertThrows(NullPointerException.class,
                () -> PasswordVisibility.connect(null, new PasswordField(), new TextField()));
        assertThrows(NullPointerException.class,
                () -> PasswordVisibility.connect(new CheckBox(), null, new TextField()));
        assertThrows(NullPointerException.class,
                () -> PasswordVisibility.connect(new CheckBox(), new PasswordField(), null));
    }
}