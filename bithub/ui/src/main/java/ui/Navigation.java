package ui;

import core.Course;
import core.Review;
import core.ReviewRepository;
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
    private final ReviewService reviewService;

    /**
     * Lagringen som fagvelgeren henter emnene fra, satt én gang av App ved oppstart.
     */
    private final ReviewRepository reviewRepository;

    /**
     * Den innloggede brukeren, felles for alle skjermene mens appen er åpen.
     */
    private final UserSession userSession;

    /**
     * Tjenesten for innlogging og registrering, eller null hvis den ikke er satt.
     */
    private final AuthenticationClient authenticationClient;

    /**
     * Oppretter navigasjonen uten innlogging. Brukes av tester for skjermer som ikke trenger
     * innlogging, og anmeldelser sendes da som gjest.
     *
     * @param reviewService    tjenesten for anmeldelser
     * @param reviewRepository lagringen fagvelgeren henter emnene fra
     * @throws NullPointerException hvis en av parameterne er null
     */
    public Navigation(ReviewService reviewService, ReviewRepository reviewRepository) {
        this(reviewService, reviewRepository, new UserSession(), null);
    }

    /**
     * Oppretter navigasjonen med tjenestene skjermbildene trenger.
     *
     * @param reviewService        tjenesten for anmeldelser
     * @param reviewRepository     lagringen fagvelgeren henter emnene fra
     * @param userSession          sesjonen som husker den innloggede brukeren
     * @param authenticationClient tjenesten for innlogging og registrering, eller null
     * @throws NullPointerException hvis reviewService, reviewRepository eller userSession er null
     */
    public Navigation(ReviewService reviewService, ReviewRepository reviewRepository,
            UserSession userSession, AuthenticationClient authenticationClient) {
        this.reviewService = Objects.requireNonNull(reviewService);
        this.reviewRepository = Objects.requireNonNull(reviewRepository);
        this.userSession = Objects.requireNonNull(userSession);
        this.authenticationClient = authenticationClient;
    }

    /**
     * Viser innloggingsskjermen i vinduet. Brukes av App når appen starter.
     *
     * @param stage vinduet skjermen skal vises i
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    public void showLogin(Stage stage) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoggInn.fxml"));
        try {
            final Parent root = loader.load();
            LoggInnController controller = loader.getController();
            if (authenticationClient != null) {
                controller.setAuthenticationClient(authenticationClient);
            }
            controller.setNavigation(this);
            controller.setLoginSuccessHandler(this::signIn);
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke åpne innloggingen", e);
        }
    }

    /**
     * Viser innloggingsskjermen i det samme vinduet.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    public void showLogin(Node source) {
        showLogin(stageOf(source));
    }

    /**
     * Åpner registreringsskjermen i det samme vinduet.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    public void showRegistration(Node source) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("RegistrerBruker.fxml"));
        try {
            final Parent root = loader.load();
            RegistrerBrukerController controller = loader.getController();
            if (authenticationClient != null) {
                controller.setAuthenticationClient(authenticationClient);
            }
            controller.setNavigation(this);
            controller.setRegistrationSuccessHandler(this::signIn);
            stageOf(source).setScene(new Scene(root));
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke åpne registreringen", e);
        }
    }

    /**
     * Åpner hjemmesiden i det samme vinduet.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    public void showHome(Node source) {
        showFxml(source, "Hjemmeside.fxml");
    }

    /**
     * Åpner fagvelgeren for å skrive en anmeldelse. Når et emne er valgt, åpnes
     * anmeldelsesskjemaet.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    public void showCourseSelection(Node source) {
        openCourseSelection(source, this::showReviewForm);
    }

    /**
     * Åpner fagvelgeren for å lese anmeldelser. Når et emne er valgt, vises anmeldelsene for emnet.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    public void showCourseSelectionForReading(Node source) {
        openCourseSelection(source, this::showReviews);
    }

    /**
     * Åpner siden som viser anmeldelsene og gjennomsnittet for et emne.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @param course emnet brukeren har valgt
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    public void showReviews(Node source, Course course) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LeseAnmeldelser.fxml"));
        try {
            final Parent root = loader.load();
            LeseAnmeldelserController controller = loader.getController();
            controller.setReviewService(reviewService);
            controller.setCourse(course);
            controller.setNavigation(this);
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
    public void showReviewForm(Node source, Course course) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SkrivAnmeldelse.fxml"));
        try {
            final Parent root = loader.load();
            SkrivAnmeldelseController controller = loader.getController();
            controller.setReviewService(reviewService);
            controller.setUserSession(userSession);
            controller.setNavigation(this);
            controller.setCourse(course);
            stageOf(source).setScene(new Scene(root));
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke åpne anmeldelsessiden", e);
        }
    }

    /**
     * Åpner profilsiden med navnet og e- posten til den innloggede brukeren.
     * 
     * @param source et element i skjermbildet som er åpent nå
     * @throws IllegalStateException kastes hvis FXML- filen ikke kan lastes
     */
    public void showProfile(Node source) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Profil.fxml"));
        try {
            final Parent root = loader.load();
            ProfilController controller = loader.getController();
            if (authenticationClient != null) {
                controller.setAuthenticationClient(authenticationClient);
            }
            controller.setNavigation(this);
            controller.setUserSession(userSession);
            stageOf(source).setScene(new Scene(root));
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke åpne profilen", e);
        }
    }

    /**
     * Åpner «Mine anmeldelser» og henter anmeldelsene til den innloggede brukeren.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    public void showMyReviews(Node source) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MineAnmeldelser.fxml"));
        try {
            final Parent root = loader.load();
            MineAnmeldelserController controller = loader.getController();
            controller.setReviewService(reviewService);
            controller.setUserSession(userSession);
            controller.setNavigation(this);
            stageOf(source).setScene(new Scene(root));
            controller.loadReviews();
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke åpne Mine anmeldelser", e);
        }
    }

    /**
     * Åpner siden for å bytte passord.
     * 
     * @param source et element i skjermbildet som er åpent nå
     * @throws IllegalStateException kastes hvis FXML- filen ikke kan lastes
     */
    public void showChangePassword(Node source) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("EndrePassord.fxml"));
        try {
            final Parent root = loader.load();
            EndrePassordController controller = loader.getController();
            if (authenticationClient != null) {
                controller.setAuthenticationClient(authenticationClient);
            }
            controller.setNavigation(this);
            controller.setUserSession(userSession);
            stageOf(source).setScene(new Scene(root));
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke åpne siden for passordbytte", e);
        }
    }

    /**
     * Åpner skjemaet for å endre en av brukerens egne anmeldelser.
     *
     * @param source     et element i skjermbildet som er åpent nå
     * @param courseCode emnekoden til emnet anmeldelsen hører til
     * @param courseName navnet på emnet
     * @param review     anmeldelsen som skal endres
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    public void showEditReview(Node source, String courseCode, String courseName, Review review) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("EndreEgenAnmeldelse.fxml"));
        try {
            final Parent root = loader.load();
            EndreEgenAnmeldelseController controller = loader.getController();
            controller.setReviewService(reviewService);
            controller.setNavigation(this);
            controller.setReview(courseCode, courseName, review);
            stageOf(source).setScene(new Scene(root));
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke åpne endring av anmeldelsen", e);
        }
    }

    /**
     * Viser en midlertidig side for funksjoner som ikke er implementert.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @param title  overskriften som skal vises
     */
    public void showComingSoon(Node source, String title) {
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
     * @param source           et element i skjermbildet som er åpent nå
     * @param onCourseSelected handlingen som får knappen og det valgte emnet
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    private void openCourseSelection(Node source, BiConsumer<Node, Course> onCourseSelected) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("VelgFag.fxml"));
        try {
            final Parent root = loader.load();
            VelgFagController controller = loader.getController();
            controller.setNavigation(this);
            controller.setOnCourseSelected(onCourseSelected);
            controller.setReviewRepository(reviewRepository);
            stageOf(source).setScene(new Scene(root));
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke åpne VelgFag.fxml", e);
        }
    }

    private void showFxml(Node source, String fileName) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fileName));
        try {
            final Parent root = loader.load();

            if (loader.getController() instanceof HjemmesideController controller) {
                controller.setNavigation(this);
            }

            stageOf(source).setScene(new Scene(root));
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke åpne " + fileName, e);
        }
    }

    private static Stage stageOf(Node source) {
        return (Stage) source.getScene().getWindow();
    }

    /**
     * Lagrer innloggingen fra serveren i sesjonen, slik at senere forespørsler sender tokenet.
     *
     * @param result token og brukeropplysninger fra serveren
     */
    private void signIn(AuthenticationResult result) {
        userSession.signIn(result.token(), result.name(), result.email());
    }
}