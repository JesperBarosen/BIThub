package ui;

import core.Course;
import core.Review;
import core.ReviewService;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Enhetstester og UI-tester for {@link SkrivAnmeldelseController} ved bruk av TestFX.
 *
 * <p>Sjekker at data blir satt og oppdelt korrekt, at validering av innsending gir riktige feil- og
 * suksessmeldinger, og at skjemaet nullstilles ved suksess.
 */
public class SkrivAnmeldelseControllerTest extends ApplicationTest {

    private SkrivAnmeldelseController controller;
    private Parent root;
    private InMemoryReviewRepository repository;

    /**
     * Klargjør og viser grensesnittet SkrivAnmeldelse.fxml før hver test. Anmeldelsene lagres i
     * minnet, slik at testene ikke trenger serveren.
     *
     * @param stage Hovedscenen for TestFX
     * @throws IOException dersom FXML-filen ikke kan lastes
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SkrivAnmeldelse.fxml"));
        root = loader.load();
        controller = loader.getController();

        repository = new InMemoryReviewRepository();
        controller.setReviewService(new ReviewService(repository));

        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Tester at {@link SkrivAnmeldelseController#setEmneData(String)} håndterer null og blanke
     * strenger uten feil.
     */
    @Test
    @DisplayName("Skal ignorere null og blank emneData")
    void testSetEmneDataWithNullOrBlank() {
        interact(() -> controller.setEmneData(null));
        Assertions.assertEquals("", controller.getCourseCode());
        Assertions.assertEquals("", controller.getCourseName());

        interact(() -> controller.setEmneData("   "));
        Assertions.assertEquals("", controller.getCourseCode());
        Assertions.assertEquals("", controller.getCourseName());
    }

    /**
     * Tester at {@link SkrivAnmeldelseController#setEmneData(String)} deler opp emnekode og
     * emnenavn korrekt ved formatet "KODE - NAVN".
     */
    @Test
    @DisplayName("Skal splitte emnekode og emnenavn når formatet inneholder bindestrek")
    void testSetEmneDataWithCodeAndName() {
        String testData = "TDT4100 - Objektorientert programmering";

        interact(() -> controller.setEmneData(testData));

        Label emneTittelLabel = (Label) root.lookup("#emneTittelLabel");
        Assertions.assertNotNull(emneTittelLabel,
                "emneTittelLabel ble ikke funnet i SkrivAnmeldelse.fxml");
        Assertions.assertEquals(testData, emneTittelLabel.getText());
        Assertions.assertEquals("TDT4100", controller.getCourseCode());
        Assertions.assertEquals("Objektorientert programmering", controller.getCourseName());
    }

    /**
     * Tester at {@link SkrivAnmeldelseController#setEmneData(String)} setter emnenavn når formatet
     * ikke har bindestrek.
     */
    @Test
    @DisplayName("Skal sette kun emnenavn dersom formatet mangler bindestrek")
    void testSetEmneDataWithoutDash() {
        String testData = "Programmering";

        interact(() -> controller.setEmneData(testData));

        Assertions.assertEquals("", controller.getCourseCode());
        Assertions.assertEquals("Programmering", controller.getCourseName());
    }

    /**
     * Tester at innsending av ugyldig rating (ikke-tall) viser feilmelding i status-labelen.
     */
    @Test
    @DisplayName("Skal vise feilmelding dersom rating ikke er et gyldig tall")
    void testHandleSendInnInvalidNumber() {
        TextField ratingField = (TextField) root.lookup("#ratingField");
        interact(() -> ratingField.setText("abc"));

        sendInn();

        Label statusLabel = (Label) root.lookup("#statusLabel");
        Assertions.assertEquals("Vurdering må være et gyldig tall.", statusLabel.getText());
        Assertions.assertEquals(Color.RED, statusLabel.getTextFill());
    }

    /**
     * Tester at rating utenfor intervallet 1-5 viser feilmelding.
     */
    @Test
    @DisplayName("Skal vise feilmelding dersom rating er utenfor intervallet 1-5")
    void testHandleSendInnOutOfRangeNumber() {
        TextField ratingField = (TextField) root.lookup("#ratingField");

        // Test rating for lav (0)
        interact(() -> ratingField.setText("0"));
        sendInn();

        Label statusLabel = (Label) root.lookup("#statusLabel");
        Assertions.assertEquals("Vurdering må være et tall fra 1 til 5.", statusLabel.getText());
        Assertions.assertEquals(Color.RED, statusLabel.getTextFill());

        // Test rating for høy (6)
        interact(() -> ratingField.setText("6"));
        sendInn();
        Assertions.assertEquals("Vurdering må være et tall fra 1 til 5.", statusLabel.getText());
        Assertions.assertEquals(Color.RED, statusLabel.getTextFill());
    }

    /**
     * Tester en vellykket innsending med anonym vurdering, og at feltene tømmes.
     */
    @Test
    @DisplayName("Skal lagre vurdering vellykket med anonym avhuking og nullstille felter")
    void testHandleSendInnSuccessAnonymous() {
        interact(() -> controller.setEmneData("TDT4120 - Algoritmer og datastrukturer"));

        TextField ratingField = (TextField) root.lookup("#ratingField");
        TextArea commentArea = (TextArea) root.lookup("#commentArea");
        CheckBox anonymousCheckBox = (CheckBox) root.lookup("#anonymousCheckBox");

        interact(() -> {
            ratingField.setText("5");
            commentArea.setText("Veldig bra emne!");
            anonymousCheckBox.setSelected(true);
        });

        sendInn();

        Label statusLabel = (Label) root.lookup("#statusLabel");
        Assertions.assertEquals("Vurderingen ble sendt inn!", statusLabel.getText());
        Assertions.assertEquals(Color.GREEN, statusLabel.getTextFill());

        // Verifiser at feltene er tømt etter at vurderingen er sendt inn
        Assertions.assertEquals("", ratingField.getText());
        Assertions.assertEquals("", commentArea.getText());
        Assertions.assertFalse(anonymousCheckBox.isSelected());

        // Les fra lagringen for å kontrollere hva som faktisk ble lagret.
        Assertions.assertEquals(1, repository.findAllCourses().size());
        Course course = repository.findCourse("TDT4120").orElseThrow();
        Assertions.assertEquals("Algoritmer og datastrukturer", course.getCourseName());
        Assertions.assertEquals(1, course.getReviews().size());
        Review review = course.getReviews().get(0);
        Assertions.assertEquals("Anonym", review.getReviewerName());
        Assertions.assertEquals(5, review.getRating());
        Assertions.assertEquals("Veldig bra emne!", review.getComment());
    }

    /**
     * Tester vellykket innsending som ikke-anonym student.
     */
    @Test
    @DisplayName("Skal lagre vurdering vellykket som ikke-anonym student")
    void testHandleSendInnSuccessNonAnonymous() {
        interact(() -> controller.setEmneData("TDT4100 - Objektorientert programmering"));

        TextField ratingField = (TextField) root.lookup("#ratingField");
        TextArea commentArea = (TextArea) root.lookup("#commentArea");
        CheckBox anonymousCheckBox = (CheckBox) root.lookup("#anonymousCheckBox");

        interact(() -> {
            ratingField.setText("4");
            commentArea.setText("Moro prosjekt.");
            anonymousCheckBox.setSelected(false);
        });

        sendInn();

        Label statusLabel = (Label) root.lookup("#statusLabel");
        Assertions.assertEquals("Vurderingen ble sendt inn!", statusLabel.getText());
        Assertions.assertEquals(Color.GREEN, statusLabel.getTextFill());

        // Les fra lagringen for å kontrollere hva som faktisk ble lagret.
        Assertions.assertEquals(1, repository.findAllCourses().size());
        Course course = repository.findCourse("TDT4100").orElseThrow();
        Assertions.assertEquals("Objektorientert programmering", course.getCourseName());
        Assertions.assertEquals(1, course.getReviews().size());
        Review review = course.getReviews().get(0);
        Assertions.assertEquals("Student", review.getReviewerName());
        Assertions.assertEquals(4, review.getRating());
        Assertions.assertEquals("Moro prosjekt.", review.getComment());
    }

    private void sendInn() {
        // Tester FXML-handlingen uten å være avhengig av museklikk eller vindusfokus.
        interact(() -> {
            Button button = (Button) root.lookup("#sendInnButton");
            Assertions.assertNotNull(button);
            Assertions.assertFalse(button.isDisabled());
            button.fire();
        });
    }
}