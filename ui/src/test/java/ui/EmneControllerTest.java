package ui;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class EmneControllerTest extends ApplicationTest {

    private EmneController controller;
    private Parent root;

    /*
    Klargjør og viser grensesnittet EmneView.fxml
    */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("EmneView.fxml"));
        root = loader.load();
        controller = loader.getController();

        stage.setScene(new Scene(root));
        stage.show();
    }

    /*
    Tester at setEmneData() mottar og viser tittel/emnedata korrekt i grensesnittet.
    */
    @Test
    void testSetEmneDataUpdatesLabelCorrectly() {
        String testData = "TDT4100 - Objektorientert programmering";

        // Oppdaterer kontrolleren på JavaFX-tråden for å unngå trådfeil under UI-endringer
        interact(() -> controller.setEmneData(testData));

        Label emneTittelLabel = (Label) root.lookup("#emneTittelLabel");
        
        // Sjekker om emneTittelLabel finnes
        Assertions.assertNotNull(emneTittelLabel, "emneTittelLabel ble ikke funnet i EmneView.fxml");
        
        // Forvent at kontrolleren har lagt til "Detaljer for: " automatisk
        Assertions.assertEquals("Detaljer for: " + testData, emneTittelLabel.getText());
    }
}