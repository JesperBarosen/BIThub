package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.model.Review;
import core.service.ReviewService;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
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
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

/**
 * TestFX-tester for {@link EndreEgenAnmeldelseController}. Anmeldelsene lagres i minnet, slik at
 * testene ikke trenger serveren.
 */
class EndreEgenAnmeldelseControllerTest extends ApplicationTest {

    private static final String OWNER = "ola@example.com";

    private Stage stage;
    private EndreEgenAnmeldelseController controller;
    private InMemoryReviewRepository repository;
    private Review review;

    /**
     * Lagrer én anmeldelse og åpner skjemaet for å endre den.
     *
     * @param stage vinduet testene kjører i
     * @throws IOException hvis FXML-filen ikke kan lastes
     */
    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        repository = new InMemoryReviewRepository();
        review = new Review("id-1", OWNER, "Ola Nordmann", false, 4, "Bra fag");
        repository.addReview("IT1901", "Informatikk prosjektarbeid I", review);

        ReviewService reviewService = new ReviewService(repository);
        UserSession userSession = new UserSession();
        userSession.signIn("token-1", "Ola Nordmann", OWNER);
        final Navigation navigation = new Navigation(reviewService, repository, userSession, null);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("EndreEgenAnmeldelse.fxml"));
        final Parent root = loader.load();
        controller = loader.getController();
        controller.setReviewService(reviewService);
        controller.setNavigation(navigation);
        controller.setReview("IT1901", "Informatikk prosjektarbeid I", review);

        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Verifiserer at skjemaet er fylt ut med anmeldelsen slik den er nå.
     */
    @Test
    void formIsFilledWithReview() {
        assertEquals("IT1901 - Informatikk prosjektarbeid I", label("#emneTittelLabel").getText());
        assertEquals("4", ratingField().getText());
        assertEquals("Bra fag", commentArea().getText());
        assertFalse(anonymousCheckBox().isSelected());
        assertEquals(review, controller.getReview());
    }

    /**
     * Verifiserer at endringen lagres, og at appen går tilbake til «Mine anmeldelser» med den
     * endrede anmeldelsen.
     */
    @Test
    void savesChangeAndShowsMyReviews() throws Exception {
        interact(() -> {
            ratingField().setText("2");
            commentArea().setText("  Mye arbeid  ");
            anonymousCheckBox().setSelected(true);
            button("#saveButton").fire();
        });

        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> stage.getScene().getRoot().lookup("#reviewList") != null);

        Review saved = repository.findReview("id-1").orElseThrow();
        assertEquals(2, saved.getRating());
        assertEquals("Mye arbeid", saved.getComment());
        assertTrue(saved.isAnonymous());
        assertEquals(OWNER, saved.getOwnerEmail());
        assertEquals("Ola Nordmann", saved.getReviewerName());
    }

    /**
     * Verifiserer at en ugyldig vurdering gir en feilmelding, og at skjemaet og anmeldelsen
     * beholdes.
     */
    @Test
    void invalidRatingShowsErrorAndKeepsForm() throws Exception {
        interact(() -> {
            ratingField().setText("9");
            commentArea().setText("Ny kommentar");
            button("#saveButton").fire();
        });

        awaitStatus("Vurdering må være et tall fra 1 til 5.");
        assertEquals(Color.RED, label("#statusLabel").getTextFill());
        assertEquals("9", ratingField().getText());
        assertEquals("Ny kommentar", commentArea().getText());
        assertEquals(4, repository.findReview("id-1").orElseThrow().getRating());
    }

    /**
     * Verifiserer at en anmeldelse som er slettet i mellomtiden, gir en feilmelding.
     */
    @Test
    void reviewThatIsGoneShowsError() throws Exception {
        repository.deleteReview("id-1");

        interact(() -> button("#saveButton").fire());

        awaitStatus("Anmeldelsen finnes ikke lenger.");
        assertNull(stage.getScene().getRoot().lookup("#reviewList"));
    }

    /**
     * Verifiserer at «Tilbake» går til «Mine anmeldelser» og «Hjem» til hjemmesiden, uten å lagre.
     */
    @Test
    void backOpensMyReviewsAndHomeOpensHomePage() {
        interact(() -> {
            ratingField().setText("1");
            button("#backButton").fire();
        });
        assertNotNull(stage.getScene().getRoot().lookup("#reviewList"));
        assertEquals(4, repository.findReview("id-1").orElseThrow().getRating());

        interact(() -> button("#homeButton").fire());
        assertNotNull(stage.getScene().getRoot().lookup("#myReviewsButton"));
    }

    /**
     * Venter til statusmeldingen vises og lagringen er ferdig.
     *
     * @param expected meldingen som forventes
     * @throws Exception hvis meldingen ikke vises innen tidsgrensen
     */
    private void awaitStatus(String expected) throws Exception {
        WaitForAsyncUtils
                .waitFor(5, TimeUnit.SECONDS,
                        () -> WaitForAsyncUtils
                                .asyncFx(() -> expected.equals(label("#statusLabel").getText())
                                        && !button("#saveButton").isDisabled())
                                .get(5, TimeUnit.SECONDS));
    }

    private Label label(String selector) {
        return (Label) stage.getScene().getRoot().lookup(selector);
    }

    private Button button(String selector) {
        return (Button) stage.getScene().getRoot().lookup(selector);
    }

    private TextField ratingField() {
        return (TextField) stage.getScene().getRoot().lookup("#ratingField");
    }

    private TextArea commentArea() {
        return (TextArea) stage.getScene().getRoot().lookup("#commentArea");
    }

    private CheckBox anonymousCheckBox() {
        return (CheckBox) stage.getScene().getRoot().lookup("#anonymousCheckBox");
    }
}