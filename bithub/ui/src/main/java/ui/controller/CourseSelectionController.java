package ui.controller;

import core.model.Course;
import core.repository.ReviewRepository;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiConsumer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import ui.Navigation;
import ui.util.BackgroundTaskRunner;

/**
 * Lar brukeren velge et emne, enten for å skrive eller lese anmeldelser.
 */
public class CourseSelectionController {

    @FXML
    private ComboBox<Course> courseComboBox;

    @FXML
    private TextField searchField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button nextButton;

    @FXML
    private Button backButton;

    @FXML
    private Button homeButton;

    private BiConsumer<Node, Course> onCourseSelected;

    @FXML
    private Button profileButton;

    private Navigation navigation;

    private final ObservableList<Course> allCourses = FXCollections.observableArrayList();

    private final FilteredList<Course> filteredCourses = new FilteredList<>(allCourses);

    private boolean coursesLoaded;

    /**
     * Setter opp emnelisten og søkefeltet, og deaktiverer feltene og knappen mens emnene hentes.
     */
    @FXML
    private void initialize() {
        courseComboBox.setButtonCell(courseCell());
        // Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
        courseComboBox.setCellFactory(listView -> {
            ListCell<Course> cell = courseCell();
            // Lange emnenavn brytes, så listen ikke blir bredere enn feltet.
            cell.setWrapText(true);
            cell.setPrefWidth(0);
            return cell;
        });
        courseComboBox.setItems(filteredCourses);
        courseComboBox.setVisibleRowCount(6);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCourses(newValue);

            if (searchField.isFocused() && !taskRunner.runningProperty().get()) {
                if (filteredCourses.isEmpty()) {
                    courseComboBox.hide();
                } else {
                    courseComboBox.hide();
                    courseComboBox.show();
                }
            }
        });
        searchField.disableProperty().bind(taskRunner.runningProperty());

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
        coursesLoaded = false;

        courseComboBox.getSelectionModel().clearSelection();
        allCourses.clear();
        statusLabel.setText("Henter emner …");

        taskRunner.execute(() -> reviewRepository.findAllCourses(), courses -> {
            allCourses.setAll(courses);
            coursesLoaded = true;
            updateSearchStatus();
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
     * Setter navigasjonen som kontrolleren skal bruke. Profilknappen kobles samtidig.
     *
     * @param navigation navigasjonsobjektet
     */
    public void setNavigation(Navigation navigation) {
        this.navigation = Objects.requireNonNull(navigation);
        navigation.connectProfileButton(profileButton, taskRunner::cancel);
    }

    /**
     * Åpner det valgte emnet, eller ber brukeren velge et emne først.
     */
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

    /**
     * Avbryter eventuell henting og går tilbake til hjemmesiden.
     */
    @FXML
    private void handleBack() {
        taskRunner.cancel();
        navigation.showHome(backButton);
    }

    /**
     * Avbryter eventuell henting og går til hjemmesiden.
     */
    @FXML
    private void handleHome() {
        taskRunner.cancel();
        navigation.showHome(homeButton);
    }

    /**
     * Filtrerer de innlastede emnene på kode og navn uten nye serverkall.
     *
     * @param searchText teksten fra søkefeltet
     */
    private void filterCourses(String searchText) {
        String query = searchText == null ? "" : searchText.strip().toLowerCase(Locale.ROOT);
        final Course selectedCourse = courseComboBox.getValue();

        // Fjern valget før listen endres, så en ny indeks ikke gir feil emne.
        courseComboBox.getSelectionModel().clearSelection();
        courseComboBox.setValue(null);

        filteredCourses.setPredicate(
                course -> course.getCourseCode().toLowerCase(Locale.ROOT).contains(query)
                        || course.getCourseName().toLowerCase(Locale.ROOT).contains(query));

        if (selectedCourse != null && filteredCourses.contains(selectedCourse)) {
            courseComboBox.getSelectionModel().select(selectedCourse);
        }

        updateSearchStatus();
    }

    /**
     * Oppdaterer søkeresultatet uten å overskrive laste- eller feilmeldinger.
     */
    private void updateSearchStatus() {
        if (!coursesLoaded) {
            return;
        }

        if (allCourses.isEmpty()) {
            statusLabel.setText("Ingen emner er tilgjengelige.");
        } else if (filteredCourses.isEmpty()) {
            statusLabel.setText("Ingen emner samsvarer med søket.");
        } else {
            statusLabel.setText("");
        }
    }

    /**
     * Lager en celle som viser emnet som «KODE - navn».
     *
     * @return cellen som brukes i emnelisten
     */
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