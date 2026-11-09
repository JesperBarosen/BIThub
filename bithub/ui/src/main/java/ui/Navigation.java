package ui;

import core.model.Course;
import core.model.Review;
import core.repository.ReviewRepository;
import core.service.ReviewService;
import java.io.IOException;
import java.util.Objects;
import java.util.function.BiConsumer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import ui.client.AuthenticationClient;
import ui.client.AuthenticationResult;
import ui.controller.AboutController;
import ui.controller.ChangePasswordController;
import ui.controller.CourseSelectionController;
import ui.controller.EditReviewController;
import ui.controller.HomeController;
import ui.controller.LoginController;
import ui.controller.MyReviewsController;
import ui.controller.ProfileController;
import ui.controller.ReadReviewsController;
import ui.controller.RegisterUserController;
import ui.controller.WriteReviewController;
import ui.session.UserSession;

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
     * Siden profilen ble åpnet fra med profilknappen, eller null hvis «TILBAKE» på profilen skal gå
     * til hjemmesiden.
     */
    private Scene sceneBeforeProfile;

    /**
     * Siden infosiden ble åpnet fra, eller null hvis «TILBAKE» på infosiden skal gå til
     * hjemmesiden.
     */
    private Scene sceneBeforeAbout;

    // Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
    /**
     * Oppretter navigasjonen uten innlogging. Brukes av tester for skjermer som ikke trenger
     * innlogging, og appen oppfører seg da som for en gjest.
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
        sceneBeforeProfile = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        try {
            final Parent root = loader.load();
            LoginController controller = loader.getController();
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("RegisterUser.fxml"));
        try {
            final Parent root = loader.load();
            RegisterUserController controller = loader.getController();
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
     * Åpner hjemmesiden i det samme vinduet. Hvilke knapper som vises, avhenger av om noen er
     * logget inn.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    public void showHome(Node source) {
        sceneBeforeProfile = null;
        showFxml(source, "Home.fxml");
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ReadReviews.fxml"));
        try {
            final Parent root = loader.load();
            ReadReviewsController controller = loader.getController();
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("WriteReview.fxml"));
        try {
            final Parent root = loader.load();
            WriteReviewController controller = loader.getController();
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Profile.fxml"));
        try {
            final Parent root = loader.load();
            ProfileController controller = loader.getController();
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
     * Går tilbake fra profilen til siden profilen ble åpnet fra med profilknappen. Siden vises
     * slik den var, med det samme emnet og den samme teksten i skjemaene. Hvis profilen ble åpnet
     * fra hjemmesiden, åpnes hjemmesiden.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @throws IllegalStateException hvis hjemmesiden skal vises og FXML-filen ikke kan lastes
     */
    public void returnFromProfile(Node source) {
        Scene previous = sceneBeforeProfile;
        if (previous == null) {
            showHome(source);
        } else {
            sceneBeforeProfile = null;
            stageOf(source).setScene(previous);
        }
    }

    /**
     * Åpner infosiden om BIThub i det samme vinduet. «TILBAKE» på infosiden går til siden den
     * ble åpnet fra.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    public void showAbout(Node source) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("About.fxml"));
        try {
            final Parent root = loader.load();
            AboutController controller = loader.getController();
            controller.setNavigation(this);
            Stage stage = stageOf(source);
            sceneBeforeAbout = stage.getScene();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke åpne infosiden", e);
        }
    }

    /**
     * Går tilbake fra infosiden til siden den ble åpnet fra, slik den var. Hvis siden ikke er
     * kjent, åpnes hjemmesiden.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @throws IllegalStateException hvis hjemmesiden skal vises og FXML-filen ikke kan lastes
     */
    public void returnFromAbout(Node source) {
        Scene previous = sceneBeforeAbout;
        sceneBeforeAbout = null;
        if (previous == null) {
            showHome(source);
        } else {
            stageOf(source).setScene(previous);
        }
    }

    /**
     * Åpner «Mine anmeldelser» og henter anmeldelsene til den innloggede brukeren.
     *
     * @param source et element i skjermbildet som er åpent nå
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    public void showMyReviews(Node source) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MyReviews.fxml"));
        try {
            final Parent root = loader.load();
            MyReviewsController controller = loader.getController();
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChangePassword.fxml"));
        try {
            final Parent root = loader.load();
            ChangePasswordController controller = loader.getController();
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("EditReview.fxml"));
        try {
            final Parent root = loader.load();
            EditReviewController controller = loader.getController();
            controller.setReviewService(reviewService);
            controller.setNavigation(this);
            controller.setReview(courseCode, courseName, review);
            stageOf(source).setScene(new Scene(root));
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke åpne endring av anmeldelsen", e);
        }
    }

    /**
     * Kobler profilknappen på en side. Knappen er skjult for gjester og åpner profilen for en
     * innlogget bruker. Den skjules uten å ta bort plassen, så overskriften står på samme sted.
     * «TILBAKE» på profilen går til siden knappen ble trykket på.
     *
     * @param profileButton profilknappen på siden
     * @param beforeLeave   det som skal gjøres før siden byttes, for eksempel å avbryte et kall
     * @throws NullPointerException hvis profileButton eller beforeLeave er null
     */
    public void connectProfileButton(Button profileButton, Runnable beforeLeave) {
        Objects.requireNonNull(profileButton);
        Objects.requireNonNull(beforeLeave);
        profileButton.setVisible(userSession.isLoggedIn());
        profileButton.setOnAction(event -> {
            beforeLeave.run();
            sceneBeforeProfile = profileButton.getScene();
            showProfile(profileButton);
        });
    }

    /**
     * Åpner fagvelgeren og bestemmer hva som skjer når et emne er valgt.
     *
     * @param source           et element i skjermbildet som er åpent nå
     * @param onCourseSelected handlingen som får knappen og det valgte emnet
     * @throws IllegalStateException hvis FXML-filen ikke kan lastes
     */
    private void openCourseSelection(Node source, BiConsumer<Node, Course> onCourseSelected) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("CourseSelection.fxml"));
        try {
            final Parent root = loader.load();
            CourseSelectionController controller = loader.getController();
            controller.setNavigation(this);
            controller.setOnCourseSelected(onCourseSelected);
            controller.setReviewRepository(reviewRepository);
            stageOf(source).setScene(new Scene(root));
        } catch (IOException e) {
            throw new IllegalStateException("Kunne ikke åpne CourseSelection.fxml", e);
        }
    }

    private void showFxml(Node source, String fileName) {
        // Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fileName));
        try {
            final Parent root = loader.load();

            if (loader.getController() instanceof HomeController controller) {
                controller.setNavigation(this);
                controller.setUserSession(userSession);
            }

            stageOf(source).setScene(new Scene(root));
            // Slutt på kode laget med hjelp av KI.
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