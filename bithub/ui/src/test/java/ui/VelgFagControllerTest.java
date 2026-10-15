package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.Course;
import core.DefaultCourses;
import core.Review;
import file.ReviewHandler;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * UI-tester for {@link VelgFagController}.
 *
 * <p>Emnene lagres i en midlertidig fil, slik at testene viser at nedtrekkslisten
 * fylles fra lagringen og ikke fra en hardkodet liste.
 */
class VelgFagControllerTest extends ApplicationTest {

    @TempDir
    Path tempDir;

    private VelgFagController controller;
    private ReviewHandler handler;
    private Parent root;

    /**
     * Laster VelgFag.fxml uten å gi kontrolleren en lagring. Hver test bestemmer
     * selv hva som ligger i lagringen før den settes.
     *
     * @param stage hovedvinduet for TestFX
     * @throws IOException hvis FXML-filen ikke kan lastes
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("VelgFag.fxml"));
        root = loader.load();
        controller = loader.getController();
        handler = new ReviewHandler(tempDir.resolve("courses.json"));
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Verifiserer at alle standardemnene vises etter første oppstart, når
     * datafilen ikke fantes fra før.
     */
    @Test
    void showsAllDefaultCoursesOnFirstStartup() {
        handler.addCoursesIfMissing(DefaultCourses.all());

        interact(() -> controller.setReviewRepository(handler));

        assertEquals(
                DefaultCourses.all().stream().map(Course::getCourseCode).toList(),
                courseComboBox().getItems().stream().map(Course::getCourseCode).toList());
    }

    /**
     * Verifiserer at emnene kommer fra lagringen, også emner som ikke er
     * standardemner og emner uten anmeldelser.
     */
    @Test
    void showsCoursesFromStorage() {
        handler.addReview("TDT4100", "Objektorientert programmering", new Review("Ola", 4, "Bra"));
        handler.addCoursesIfMissing(List.of(new Course("IT1901", "Informatikk prosjektarbeid I")));

        interact(() -> controller.setReviewRepository(handler));

        List<Course> items = courseComboBox().getItems();
        assertEquals(2, items.size());
        assertEquals("TDT4100", items.get(0).getCourseCode());
        assertEquals("IT1901", items.get(1).getCourseCode());
        assertTrue(items.get(1).getReviews().isEmpty());
    }

    /**
     * Verifiserer at kontrolleren ikke godtar null som lagring.
     */
    @Test
    void rejectsNullService() {
        assertThrows(NullPointerException.class, () -> controller.setReviewRepository(null));
    }

    @SuppressWarnings("unchecked")
    private ComboBox<Course> courseComboBox() {
        return (ComboBox<Course>) root.lookup("#courseComboBox");
    }
}