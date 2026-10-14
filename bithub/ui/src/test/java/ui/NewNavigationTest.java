package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import core.Course;
import core.DefaultCourses;
import core.ReviewService;
import file.ReviewHandler;
import java.nio.file.Path;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Tester navigasjonen i de nye FXML-skjermbildene.
 */
class NewNavigationTest extends ApplicationTest {

    @TempDir
    Path tempDir;

    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        // Setter opp appen slik App gjør, men med en midlertidig fil i stedet for ~/.bithub.
        // Filen finnes ikke fra før, så dette er det samme som første oppstart.
        ReviewHandler reviewHandler = new ReviewHandler(tempDir.resolve("courses.json"));
        reviewHandler.addCoursesIfMissing(DefaultCourses.all());
        Navigation.setReviewRepository(reviewHandler);
        Navigation.setReviewService(new ReviewService(reviewHandler));

        Parent root = new FXMLLoader(App.class.getResource("Hjemmeside.fxml")).load();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void canChooseCourseAndReturnHome() {
        interact(() -> button("#writeReviewButton").fire());

        @SuppressWarnings("unchecked")
        ComboBox<Course> courseComboBox
                = (ComboBox<Course>) stage.getScene().getRoot().lookup("#courseComboBox");
        assertNotNull(courseComboBox);
        assertEquals(5, courseComboBox.getItems().size());

        interact(() -> {
            courseComboBox.getSelectionModel().select(4);
            button("#nextButton").fire();
        });

        Label title = (Label) stage.getScene().getRoot().lookup("#emneTittelLabel");
        assertNotNull(title);
        assertEquals("IT1901 - Informatikk prosjektarbeid I", title.getText());

        interact(() -> button("#backButton").fire());
        assertNotNull(stage.getScene().getRoot().lookup("#courseComboBox"));

        interact(() -> button("#homeButton").fire());
        assertNotNull(stage.getScene().getRoot().lookup("#writeReviewButton"));
    }

    @Test
    void unimplementedPageShowsPlaceholder() {
        interact(() -> button("#myReviewsButton").fire());
        Label heading = (Label) stage.getScene().getRoot().lookup("#placeholderHeading");
        assertEquals("Mine anmeldelser", heading.getText());
    }

    /**
     * Går gjennom alle tre lag: skriver en anmeldelse i UI, som lagres til fil,
     * og leser den tilbake på siden for å lese anmeldelser.
     */
    @Test
    void writtenReviewIsShownWhenReading() {
        interact(() -> button("#writeReviewButton").fire());
        interact(() -> {
            courseComboBox().getSelectionModel().select(4);
            button("#nextButton").fire();
        });
        interact(() -> {
            ((TextField) stage.getScene().getRoot().lookup("#ratingField")).setText("4");
            button("#sendInnButton").fire();
            button("#homeButton").fire();
        });

        interact(() -> button("#readReviewsButton").fire());
        interact(() -> {
            courseComboBox().getSelectionModel().select(4);
            button("#nextButton").fire();
        });

        assertEquals("IT1901 - Informatikk prosjektarbeid I", label("#courseTitleLabel").getText());
        assertEquals("4.00", label("#averageLabel").getText());
        assertEquals("1", label("#countLabel").getText());
        assertEquals(1, ((ListView<?>) stage.getScene().getRoot().lookup("#reviewList"))
                .getItems().size());

        interact(() -> button("#backButton").fire());
        assertNotNull(courseComboBox());
    }

    @SuppressWarnings("unchecked")
    private ComboBox<Course> courseComboBox() {
        return (ComboBox<Course>) stage.getScene().getRoot().lookup("#courseComboBox");
    }

    private Label label(String selector) {
        return (Label) stage.getScene().getRoot().lookup(selector);
    }

    private Button button(String selector) {
        return (Button) stage.getScene().getRoot().lookup(selector);
    }
}