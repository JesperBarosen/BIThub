package ui;

import core.Course;
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

    private BiConsumer<Node, Course> onCourseSelected = Navigation::showReviewForm;

    @FXML
    private void initialize() {
        courseComboBox.setItems(
                FXCollections.observableArrayList(CourseOptions.availableCourses()));
        courseComboBox.setButtonCell(courseCell());
        courseComboBox.setCellFactory(listView -> courseCell());
    }

    /**
     * Bestemmer hva som skal skje når brukeren har valgt et emne og trykker
     * «Gå videre». Slik kan fagvelgeren brukes både for å skrive og lese
     * anmeldelser. Standard er å åpne anmeldelsesskjemaet.
     *
     * @param onCourseSelected handlingen som får knappen som ble trykket og det
     *     valgte emnet
     * @throws NullPointerException hvis onCourseSelected er null
     */
    public void setOnCourseSelected(BiConsumer<Node, Course> onCourseSelected) {
        this.onCourseSelected = Objects.requireNonNull(onCourseSelected);
    }

    @FXML
    private void handleNext() {
        Course selectedCourse = courseComboBox.getValue();
        if (selectedCourse == null) {
            statusLabel.setText("Velg et fag først.");
            return;
        }
        onCourseSelected.accept(nextButton, selectedCourse);
    }

    @FXML
    private void handleBack() {
        Navigation.showHome(backButton);
    }

    @FXML
    private void handleHome() {
        Navigation.showHome(homeButton);
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