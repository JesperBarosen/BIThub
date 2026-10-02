package ui;

import core.Course;
import core.ReviewService;
import java.io.IOException;
import java.util.Objects;
import java.util.function.BiConsumer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Samler navigasjon mellom skjermbildene som finnes i appen.
 */
public final class Navigation {

    /**
     * Tjenesten for anmeldelser, satt én gang av App ved oppstart.
     */
    private static ReviewService reviewService;

    private Navigation() {
    }

    /**
     * Setter tjenesten som skal gis videre til skjermbildene som trenger den.
     * Kalles fra App, slik at lagringen bestemmes ett sted.
     *
     * @param service tjenesten fra core som validerer og lagrer anmeldelser
     * @throws NullPointerException hvis service er null
     */
    public static void setReviewService(ReviewService service) {
        reviewService = Objects.requireNonNull(service);
    }

    /**
     * Åpner hjemmesiden i det samme vinduet.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    public static void showHome(Node source) {
        showFxml(source, "Hjemmeside.fxml");
    }

    /**
     * Åpner fagvelgeren for å skrive en anmeldelse. Når et emne er valgt, åpnes
     * anmeldelsesskjemaet.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    public static void showCourseSelection(Node source) {
        openCourseSelection(source, Navigation::showReviewForm);
    }

    /**
     * Åpner fagvelgeren for å lese anmeldelser. Når et emne er valgt, vises
     * anmeldelsene for emnet.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    public static void showCourseSelectionForReading(Node source) {
        openCourseSelection(source, Navigation::showReviews);
    }

    /**
     * Åpner siden som viser anmeldelsene og gjennomsnittet for et emne.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @param course emnet brukeren har valgt
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    public static void showReviews(Node source, Course course) {
        FXMLLoader loader = new FXMLLoader(Navigation.class.getResource("LeseAnmeldelser.fxml"));
        try {
            Parent root = loader.load();
            LeseAnmeldelserController controller = loader.getController();
            controller.setReviewService(reviewService);
            controller.setCourse(course);
            stageOf(source).setScene(new Scene(root));
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke åpne siden med anmeldelser", e);
        }
    }

    /**
     * Åpner anmeldelsessiden og gir det valgte emnet til kontrolleren.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @param course emnet brukeren har valgt
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    public static void showReviewForm(Node source, Course course) {
        FXMLLoader loader = new FXMLLoader(Navigation.class.getResource("EmneView.fxml"));
        try {
            Parent root = loader.load();
            EmneController controller = loader.getController();
            controller.setReviewService(reviewService);
            controller.setCourse(course);
            stageOf(source).setScene(new Scene(root));
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke åpne anmeldelsessiden", e);
        }
    }

    /**
     * Viser en midlertidig side for funksjoner som ikke er implementert.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @param title overskriften som skal vises
     */
    public static void showComingSoon(Node source, String title) {
        Label heading = new Label(title);
        heading.setId("placeholderHeading");
        Label message = new Label("Denne siden er ikke laget ennå.");
        Button homeButton = new Button("HJEM");
        VBox root = new VBox(20, heading, message, homeButton);
        root.setStyle("-fx-alignment: center; -fx-padding: 30;");
        homeButton.setOnAction(event -> showHome(homeButton));
        stageOf(source).setScene(new Scene(root, 400, 600));
    }

    /**
     * Åpner fagvelgeren og bestemmer hva som skjer når et emne er valgt.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @param onCourseSelected handlingen som får knappen og det valgte emnet
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    private static void openCourseSelection(
            Node source, BiConsumer<Node, Course> onCourseSelected) {
        FXMLLoader loader = new FXMLLoader(Navigation.class.getResource("VelgFag.fxml"));
        try {
            Parent root = loader.load();
            VelgFagController controller = loader.getController();
            controller.setOnCourseSelected(onCourseSelected);
            stageOf(source).setScene(new Scene(root));
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke åpne VelgFag.fxml", e);
        }
    }

    private static void showFxml(Node source, String fileName) {
        try {
            Parent root = FXMLLoader.load(Navigation.class.getResource(fileName));
            stageOf(source).setScene(new Scene(root));
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke åpne " + fileName, e);
        }
    }

    private static Stage stageOf(Node source) {
        return (Stage) source.getScene().getWindow();
    }
}