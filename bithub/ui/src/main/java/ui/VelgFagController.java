package ui;

import core.Course;
import core.ReviewRepository;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.function.BiConsumer;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

/**
 * Lar brukeren velge et emne, enten for å skrive eller lese anmeldelser.
 */
public class VelgFagController {

    @FXML
    private ComboBox<Course> courseComboBox;

    @FXML
    private Label statusLabel;

    @FXML
    private Button nextButton;

    @FXML
    private Button backButton;

    @FXML
    private Button homeButton;

    private BiConsumer<Node, Course> onCourseSelected;

    private Navigation navigation;

    @FXML
    private void initialize() {
        courseComboBox.setButtonCell(courseCell());
        courseComboBox.setCellFactory(listView -> courseCell());

        courseComboBox.disableProperty().bind(taskRunner.runningProperty());
        nextButton.disableProperty()
                .bind(taskRunner.runningProperty().or(courseComboBox.valueProperty().isNull()));
    }

    private final BackgroundTaskRunner taskRunner = new BackgroundTaskRunner();

    /**
     * Setter lagringen og henter emnene i bakgrunnen, også de uten anmeldelser.
     *
     * <p>En eventuell tidligere henting avbrytes før den nye starter.
     *
     * @param reviewRepository lagringen for emner og anmeldelser
     * @throws NullPointerException hvis reviewRepository er null
     */
    public void setReviewRepository(ReviewRepository reviewRepository) {
        Objects.requireNonNull(reviewRepository);
        taskRunner.cancel();

        courseComboBox.getSelectionModel().clearSelection();
        courseComboBox.getItems().clear();
        statusLabel.setText("Henter emner …");

        taskRunner.execute(() -> reviewRepository.findAllCourses(), courses -> {
            courseComboBox.setItems(FXCollections.observableArrayList(courses));
            statusLabel.setText(courses.isEmpty() ? "Ingen emner er tilgjengelige." : "");
        }, error -> statusLabel.setText("Kunne ikke hente emnene. Prøv igjen senere."));
    }

    /**
     * Bestemmer hva som skal skje når brukeren har valgt et emne og trykker «Gå videre». Slik kan
     * fagvelgeren brukes både for å skrive og lese anmeldelser. Handlingen settes av
     * {@link Navigation}.
     *
     * @param onCourseSelected handlingen som får knappen som ble trykket og det valgte emnet
     * @throws NullPointerException hvis onCourseSelected er null
     */
    public void setOnCourseSelected(BiConsumer<Node, Course> onCourseSelected) {
        this.onCourseSelected = Objects.requireNonNull(onCourseSelected);
    }

    /**
     * Setter navigasjonen som kontrolleren skal bruke.
     *
     * @param navigation navigasjonsobjektet
     */
    public void setNavigation(Navigation navigation) {
        this.navigation = Objects.requireNonNull(navigation);
    }

    @FXML
    private void handleNext() {
        if (taskRunner.runningProperty().get()) {
            return;
        }

        Course selectedCourse = courseComboBox.getValue();
        if (selectedCourse == null) {
            statusLabel.setText("Velg et fag først.");
            return;
        }

        taskRunner.cancel();
        onCourseSelected.accept(nextButton, selectedCourse);
    }

    @FXML
    private void handleBack() {
        taskRunner.cancel();
        navigation.showHome(backButton);
    }

    @FXML
    private void handleHome() {
        taskRunner.cancel();
        navigation.showHome(homeButton);
    }

    private ListCell<Course> courseCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                setText(empty || course == null ? null
                        : course.getCourseCode() + " - " + course.getCourseName());
            }
        };
    }
}