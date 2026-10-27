
package ui;

import core.model.Course;
import core.model.Review;
import core.repository.ReviewRepository;
import core.service.ReviewService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ConnectException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.testfx.util.WaitForAsyncUtils;

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
    void testHandleSendInnInvalidNumber() throws Exception {
        signIn();
        TextField ratingField = (TextField) root.lookup("#ratingField");
        interact(() -> ratingField.setText("abc"));

        sendInn();
        awaitStatusMessage("Vurdering må være et gyldig tall.");

        interact(() -> {
            Label statusLabel = (Label) root.lookup("#statusLabel");
            Assertions.assertEquals("Vurdering må være et gyldig tall.", statusLabel.getText());
            Assertions.assertEquals(Color.RED, statusLabel.getTextFill());
        });
    }

    /**
     * Tester at rating utenfor intervallet 1-5 viser feilmelding.
     */
    @Test
    @DisplayName("Skal vise feilmelding dersom rating er utenfor intervallet 1-5")
    void testHandleSendInnOutOfRangeNumber() throws Exception {
        signIn();
        TextField ratingField = (TextField) root.lookup("#ratingField");

        // Test rating for lav (0)
        interact(() -> ratingField.setText("0"));
        sendInn();
        awaitStatusMessage("Vurdering må være et tall fra 1 til 5.");

        interact(() -> {
            Label statusLabel = (Label) root.lookup("#statusLabel");
            Assertions.assertEquals("Vurdering må være et tall fra 1 til 5.",
                    statusLabel.getText());
            Assertions.assertEquals(Color.RED, statusLabel.getTextFill());
        });

        // Test rating for høy (6)
        interact(() -> ratingField.setText("6"));
        sendInn();
        awaitStatusMessage("Vurdering må være et tall fra 1 til 5.");

        interact(() -> {
            Label statusLabel = (Label) root.lookup("#statusLabel");
            Assertions.assertEquals("Vurdering må være et tall fra 1 til 5.",
                    statusLabel.getText());
            Assertions.assertEquals(Color.RED, statusLabel.getTextFill());
        });
    }

    /**
     * Tester en vellykket innsending med anonym vurdering, og at feltene tømmes.
     */
    @Test
    @DisplayName("Skal lagre vurdering vellykket med anonym avhuking og nullstille felter")
    void testHandleSendInnSuccessAnonymous() throws Exception {
        signIn();
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
        awaitStatusMessage("Vurderingen ble sendt inn!");

        interact(() -> {
            Label statusLabel = (Label) root.lookup("#statusLabel");
            Assertions.assertEquals("Vurderingen ble sendt inn!", statusLabel.getText());
            Assertions.assertEquals(Color.GREEN, statusLabel.getTextFill());

            // Verifiser at feltene er tømt etter at vurderingen er sendt inn.
            Assertions.assertEquals("", ratingField.getText());
            Assertions.assertEquals("", commentArea.getText());
            Assertions.assertFalse(anonymousCheckBox.isSelected());
        });

        // Les fra lagringen for å kontrollere hva som faktisk ble lagret.
        Assertions.assertEquals(1, repository.findAllCourses().size());
        Course course = repository.findCourse("TDT4120").orElseThrow();
        Assertions.assertEquals("Algoritmer og datastrukturer", course.getCourseName());
        Assertions.assertEquals(1, course.getReviews().size());
        Review review = course.getReviews().get(0);
        Assertions.assertEquals("kari@ntnu.no", review.getOwnerEmail());
        Assertions.assertTrue(review.isAnonymous());
        Assertions.assertEquals("Anonym", review.getDisplayName());
        Assertions.assertEquals(5, review.getRating());
        Assertions.assertEquals("Veldig bra emne!", review.getComment());
    }

    /**
     * Tester vellykket innsending som innlogget bruker som ikke er anonym.
     */
    @Test
    @DisplayName("Skal lagre vurdering vellykket med navnet til innlogget bruker")
    void testHandleSendInnSuccessNonAnonymous() throws Exception {
        signIn();
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
        awaitStatusMessage("Vurderingen ble sendt inn!");

        interact(() -> {
            Label statusLabel = (Label) root.lookup("#statusLabel");
            Assertions.assertEquals("Vurderingen ble sendt inn!", statusLabel.getText());
            Assertions.assertEquals(Color.GREEN, statusLabel.getTextFill());
        });

        // Les fra lagringen for å kontrollere hva som faktisk ble lagret.
        Assertions.assertEquals(1, repository.findAllCourses().size());
        Course course = repository.findCourse("TDT4100").orElseThrow();
        Assertions.assertEquals("Objektorientert programmering", course.getCourseName());
        Assertions.assertEquals(1, course.getReviews().size());
        Review review = course.getReviews().get(0);
        Assertions.assertEquals("Kari Nordmann", review.getReviewerName());
        Assertions.assertEquals("kari@ntnu.no", review.getOwnerEmail());
        Assertions.assertFalse(review.isAnonymous());
        Assertions.assertEquals(4, review.getRating());
        Assertions.assertEquals("Moro prosjekt.", review.getComment());
    }

    /**
     * Tester at en gjest ikke kan sende inn en anmeldelse, og at skjemaet beholdes.
     */
    @Test
    @DisplayName("Skal be gjester logge inn og ikke lagre noe")
    void testHandleSendInnWithoutLoginAsksForLogin() throws Exception {
        interact(() -> controller.setEmneData("TDT4100 - Objektorientert programmering"));

        TextField ratingField = (TextField) root.lookup("#ratingField");
        interact(() -> ratingField.setText("3"));

        sendInn();
        awaitStatusMessage("Du må logge inn for å skrive en anmeldelse.");

        Assertions.assertEquals(Color.RED, ((Label) root.lookup("#statusLabel")).getTextFill());
        Assertions.assertEquals("3", ratingField.getText());
        Assertions.assertTrue(repository.findAllCourses().isEmpty());
    }

    /**
     * Logger inn en testbruker i sesjonen som kontrolleren bruker.
     */
    private void signIn() {
        UserSession session = new UserSession();
        session.signIn("test-token", "Kari Nordmann", "kari@ntnu.no");
        interact(() -> controller.setUserSession(session));
    }

    /**
     * Utfører innsending gjennom FXML-handlingen.
     */
    private void sendInn() {
        // Tester FXML-handlingen uten å være avhengig av museklikk eller vindusfokus.
        interact(() -> {
            Button button = (Button) root.lookup("#sendInnButton");
            Assertions.assertNotNull(button);
            Assertions.assertFalse(button.isDisabled());
            button.fire();
        });
    }

    /**
     * Venter til den forventede statusmeldingen vises og innsendingen er ferdig.
     *
     * @param expectedMessage meldingen som forventes etter innsending
     * @throws Exception hvis innsendingen ikke avsluttes innen tidsgrensen
     */
    private void awaitStatusMessage(String expectedMessage) throws Exception {
        Label statusLabel = (Label) root.lookup("#statusLabel");
        Button sendButton = (Button) root.lookup("#sendInnButton");

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> WaitForAsyncUtils.asyncFx(
                () -> expectedMessage.equals(statusLabel.getText()) && !sendButton.isDisabled())
                .get(5, TimeUnit.SECONDS));
    }

    /**
     * Tester at treg lagring ikke blokkerer JavaFX eller tillater dobbel innsending.
     */
    @Test
    @DisplayName("Skal deaktivere innsending mens lagringen pågår")
    void disablesSubmissionWhileSaving() throws Exception {
        signIn();
        DelayedReviewRepository delayedRepository = new DelayedReviewRepository();

        interact(() -> {
            controller.setReviewService(new ReviewService(delayedRepository));
            controller.setEmneData("IT1901 - Informatikk prosjektarbeid I");
            ((TextField) root.lookup("#ratingField")).setText("5");
            ((TextArea) root.lookup("#commentArea")).setText("Bra emne");
        });

        try {
            sendInn();
            Assertions.assertTrue(delayedRepository.started.await(5, TimeUnit.SECONDS));

            interact(() -> {
                Button sendButton = (Button) root.lookup("#sendInnButton");

                Assertions.assertTrue(sendButton.isDisabled());
                Assertions.assertTrue(root.lookup("#ratingField").isDisabled());
                Assertions.assertTrue(root.lookup("#commentArea").isDisabled());
                Assertions.assertTrue(root.lookup("#anonymousCheckBox").isDisabled());

                Assertions.assertFalse(root.lookup("#backButton").isDisabled());
                Assertions.assertFalse(root.lookup("#homeButton").isDisabled());

                Assertions.assertEquals("Sender anmeldelsen ...",
                        ((Label) root.lookup("#statusLabel")).getText());

                sendButton.fire();
            });

            Assertions.assertEquals(1, delayedRepository.submissions.get());

            delayedRepository.release.complete(null);
            awaitStatusMessage("Vurderingen ble sendt inn!");

            Assertions.assertEquals(1, delayedRepository.submissions.get());
            Assertions.assertEquals(1,
                    delayedRepository.findCourse("IT1901").orElseThrow().getReviews().size());
        } finally {
            delayedRepository.release.complete(null);
        }
    }

    /**
     * Kontrollerer at nettverksfeil ikke fjerner brukerens skjemainnhold.
     */
    @Test
    @DisplayName("Skal beholde skjemainnhold og aktivere innsending ved feil")
    void preservesInputWhenSubmissionFails() throws Exception {
        signIn();
        DelayedReviewRepository delayedRepository = new DelayedReviewRepository();

        interact(() -> {
            controller.setReviewService(new ReviewService(delayedRepository));
            controller.setEmneData("IT1901 - Informatikk prosjektarbeid I");
            ((TextField) root.lookup("#ratingField")).setText("4");
            ((TextArea) root.lookup("#commentArea")).setText("Bra emne");
            ((CheckBox) root.lookup("#anonymousCheckBox")).setSelected(true);
        });

        try {
            sendInn();
            Assertions.assertTrue(delayedRepository.started.await(5, TimeUnit.SECONDS));
            delayedRepository.release.completeExceptionally(
                    new UncheckedIOException(new ConnectException("Serveren svarer ikke.")));

            awaitStatusMessage(
                    "Kunne ikke bekrefte at anmeldelsen ble lagret. " + "Prøv igjen senere.");

            interact(() -> {
                Assertions.assertEquals("4", ((TextField) root.lookup("#ratingField")).getText());
                Assertions.assertEquals("Bra emne",
                        ((TextArea) root.lookup("#commentArea")).getText());
                Assertions.assertTrue(((CheckBox) root.lookup("#anonymousCheckBox")).isSelected());
                Assertions.assertFalse(root.lookup("#sendInnButton").isDisabled());
                Assertions.assertFalse(root.lookup("#ratingField").isDisabled());
                Assertions.assertFalse(root.lookup("#commentArea").isDisabled());
                Assertions.assertEquals(Color.RED,
                        ((Label) root.lookup("#statusLabel")).getTextFill());
            });

            Assertions.assertTrue(delayedRepository.findAllCourses().isEmpty());
        } finally {
            delayedRepository.release.complete(null);
        }
    }

    /**
     * Kontrollerer at en gammel respons ikke oppdaterer en forlatt side.
     */
    @Test
    @DisplayName("Skal ignorere forsinket svar etter navigering til hjem")
    void ignoresLateResponseAfterNavigation() throws Exception {
        signIn();
        DelayedReviewRepository delayedRepository = new DelayedReviewRepository();

        interact(() -> {
            controller.setReviewService(new ReviewService(delayedRepository));
            controller.setNavigation(new Navigation(new ReviewService(repository), repository));
            controller.setEmneData("IT1901 - Informatikk prosjektarbeid I");
            ((TextField) root.lookup("#ratingField")).setText("5");
            ((TextArea) root.lookup("#commentArea")).setText("Bra emne");
        });

        try {
            sendInn();
            Assertions.assertTrue(delayedRepository.started.await(5, TimeUnit.SECONDS));

            interact(() -> {
                Button homeButton = (Button) root.lookup("#homeButton");
                Stage stage = (Stage) homeButton.getScene().getWindow();
                homeButton.fire();
                Assertions.assertNotSame(root, stage.getScene().getRoot());
                Assertions.assertNotNull(stage.getScene().getRoot().lookup("#writeReviewButton"));
            });

            // Serveren kan fullføre lagringen selv om klienten har navigert bort.
            delayedRepository.release.complete(null);
            Assertions.assertTrue(delayedRepository.finished.await(5, TimeUnit.SECONDS));
            WaitForAsyncUtils.waitForFxEvents();

            interact(() -> {
                Assertions.assertEquals("Sender anmeldelsen ...",
                        ((Label) root.lookup("#statusLabel")).getText());
                Assertions.assertEquals("5", ((TextField) root.lookup("#ratingField")).getText());
                Assertions.assertEquals("Bra emne",
                        ((TextArea) root.lookup("#commentArea")).getText());
            });
        } finally {
            delayedRepository.release.complete(null);
        }
    }

    /**
     * Simulerer treg lagring uten å bruke en faktisk REST-server.
     */
    private static final class DelayedReviewRepository implements ReviewRepository {

        private final InMemoryReviewRepository delegate = new InMemoryReviewRepository();
        private final CountDownLatch started = new CountDownLatch(1);
        private final CompletableFuture<Void> release = new CompletableFuture<>();
        private final AtomicInteger submissions = new AtomicInteger();
        private final CountDownLatch finished = new CountDownLatch(1);

        @Override
        public void addReview(String courseCode, String courseName, Review review) {
            submissions.incrementAndGet();
            started.countDown();
            try {
                release.join();
                delegate.addReview(courseCode, courseName, review);
            } finally {
                finished.countDown();
            }
        }

        @Override
        public Optional<Course> findCourse(String courseCode) {
            return delegate.findCourse(courseCode);
        }

        @Override
        public List<Course> findAllCourses() {
            return delegate.findAllCourses();
        }

        @Override
        public void addCoursesIfMissing(List<Course> courses) {
            delegate.addCoursesIfMissing(courses);
        }
    }
}