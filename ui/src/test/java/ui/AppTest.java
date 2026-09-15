package ui;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

/**
 * Testklasse for applikasjonens JavaFX-brukergrensesnitt.
 * Verifiserer at applikasjonen starter korrekt, at faglisten fylles med fag,
 * og at valg av fag oppdaterer brukergrensesnittet.
 */
class AppTest extends ApplicationTest {

    private Parent root;

    /**
     * Laster inn applikasjonens FXML-fil og viser JavaFX-vinduet
     * før hver test kjøres.
     *
     * @param stage JavaFX-vinduet som brukes av TestFX
     * @throws IOException hvis FXML-filen ikke kan lastes inn
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("App.fxml"));
        root = fxmlLoader.load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Verifiserer at applikasjonen starter og laster inn rotnoden korrekt.
     */
    @Test
    void testAppStarts() {
        Assertions.assertNotNull(root);
    }

    /**
     * Verifiserer at faglisten finnes og inneholder minst ett fag
     * når applikasjonen starter.
     */
    @Test
    void testCourseListIsNotEmpty() {
        @SuppressWarnings("unchecked")
        ListView<String> courseListView
                = (ListView<String>) root.lookup("#courseListView");

        Assertions.assertNotNull(courseListView);
        Assertions.assertFalse(courseListView.getItems().isEmpty());
    }

    /**
     * Verifiserer at valg av et fag i faglisten oppdaterer etiketten
     * som viser hvilket fag som er valgt.
     */
    @Test
    void testSelectingCourseUpdatesLabel() {
        @SuppressWarnings("unchecked")
        ListView<String> courseListView
                = (ListView<String>) root.lookup("#courseListView");

        Assertions.assertNotNull(courseListView);

        String firstCourse = courseListView.getItems().get(0);
        clickOn(firstCourse);

        Label selectedCourseLabel
                = (Label) root.lookup("#selectedCourseLabel");

        Assertions.assertNotNull(selectedCourseLabel);
        Assertions.assertEquals(
                "Valgt fag: " + firstCourse,
                selectedCourseLabel.getText()
        );
    }
}