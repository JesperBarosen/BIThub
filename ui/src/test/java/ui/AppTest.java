package ui;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.scene.input.MouseEvent;
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
    private AppController controller;

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
        controller = fxmlLoader.getController();
        stage.setScene(new Scene(root));
        stage.show();
    }

    /*
    Verifiserer at applikasjonen starter og laster inn rotnoden korrekt.
    */
    @Test
    void testAppStarts() {
        Assertions.assertNotNull(root);
    }

    /*
    Verifiserer at faglisten finnes og inneholder minst ett fag
    når applikasjonen starter.
    */
    @Test
    void testCourseListIsNotEmpty() {
        @SuppressWarnings("unchecked")
        ListView<String> courseListView
                = (ListView<String>) root.lookup("#courseListView");

        Assertions.assertNotNull(courseListView);
        Assertions.assertFalse(courseListView.getItems().isEmpty());
    }

    /*
    Verifiserer at valg av et fag i faglisten oppdaterer etiketten
    som viser hvilket fag som er valgt.
    */
    @Test
    void testSelectingCourseUpdatesLabel() {
        @SuppressWarnings("unchecked")
        ListView<String> courseListView
                = (ListView<String>) root.lookup("#courseListView");

        Assertions.assertNotNull(courseListView);

        interact(() -> {
            courseListView.getSelectionModel().selectFirst();
            controller.handleCourseClick();
        });

        Label selectedCourseLabel
                = (Label) root.lookup("#selectedCourseLabel");

        Assertions.assertNotNull(selectedCourseLabel);
        Assertions.assertEquals(
                "Valgt fag: IT1901 - Informatikk prosjektarbeid",
                selectedCourseLabel.getText()
        );
    }

    /*
    Tester at dobbeltklikk utløser navigeringslogikken til EmneView.fxml. 
    */
    @Test
    void testDoubleClickSwitchesScene() {
        // Dobbeltklikker på et element i listen
        doubleClickOn("IT1901 - Informatikk prosjektarbeid");

        // Finner elementet #emneTittelLabel i den nylig lastede scenen (EmneView.fxml)
        Label emneTittelLabel = (Label) window(0).getScene().getRoot().lookup("#emneTittelLabel");
        
        //Sjekker om overskriften med ID #emneTittelLabel finnes
        Assertions.assertNotNull(emneTittelLabel, "Navigering feilet: emneTittelLabel ble ikke funnet");
        //Sjekker om info om faget ble tatt med videre
        Assertions.assertTrue(emneTittelLabel.getText().contains("IT1901"));
    }

    /*
    Tester at AppController håndterer spesialtilfeller 
    og feilsituasjoner uten å krasje
    */
    @Test
    void testHandleCourseClickEdgeCases() {
        
        //Finner listen med fag og markerer øverste fag
        @SuppressWarnings("unchecked")
        ListView<String> courseListView = (ListView<String>) root.lookup("#courseListView");
        interact(() -> courseListView.getSelectionModel().selectFirst());

        //Metoden må håndtere null-event uten å kaste NullPointerException
        Assertions.assertDoesNotThrow(() -> {
            interact(() -> controller.handleCourseClick((MouseEvent) null));
        });

        // Høyreklikk skal velge/marker emnet, men ikke bytte scene
        rightClickOn("IT1901 - Informatikk prosjektarbeid");

        //Sjekker forventet streng i selectedCourseLabel
        Label selectedCourseLabel = (Label) root.lookup("#selectedCourseLabel");
        Assertions.assertEquals("Valgt fag: IT1901 - Informatikk prosjektarbeid", selectedCourseLabel.getText());

        // Sjekker at vi fortsatt er på hovedskjermen
        Label emneTittelLabel = (Label) root.lookup("#emneTittelLabel");
        Assertions.assertNull(emneTittelLabel);
    }
}
