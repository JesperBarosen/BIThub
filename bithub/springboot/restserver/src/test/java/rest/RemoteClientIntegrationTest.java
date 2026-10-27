package rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.exception.NotReviewOwnerException;
import core.model.Course;
import core.model.Review;
import core.service.ReviewService;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ui.client.AuthenticationResult;
import ui.client.RemoteAuthenticationClient;
import ui.client.RemoteReviewRepository;

/**
 * Integrasjonstester der klientene fra ui snakker med hele serveren over HTTP.
 *
 * <p>Serveren startes på en ledig port og lagrer i en midlertidig mappe, slik at testene aldri
 * endrer dataene i {@code ~/.bithub}. Klientene er de samme {@link RemoteReviewRepository} og
 * {@link RemoteAuthenticationClient} som appen bruker, så testene sjekker at klient og server er
 * enige om JSON-formatet, innloggingen og feilsvarene.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RemoteClientIntegrationTest {

    @TempDir
    static Path tempDir;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String PASSWORD = "Passord123";

    private static final String NOT_LOGGED_IN = "Du er ikke lenger logget inn. Logg inn på nytt.";

    @LocalServerPort
    private int port;

    private RemoteReviewRepository repository;
    private ReviewService reviewService;
    private RemoteAuthenticationClient authenticationClient;

    /**
     * Lar serveren bruke filer i den midlertidige mappen i stedet for standardfilene.
     *
     * @param registry innstillingene Spring bruker når serveren startes
     */
    @DynamicPropertySource
    static void useTemporaryFiles(DynamicPropertyRegistry registry) {
        registry.add("bithub.reviews-file", () -> tempDir.resolve("courses.json").toString());
        registry.add("bithub.users-file", () -> tempDir.resolve("users.json").toString());
    }

    /**
     * Kobler klientene til serveren som kjører på testporten.
     */
    @BeforeEach
    void connectClients() {
        repository = new RemoteReviewRepository(serverUrl());
        reviewService = new ReviewService(repository);
        authenticationClient = new RemoteAuthenticationClient(serverUrl());
    }

    /**
     * Verifiserer at klienten henter standardemnene som serveren legger inn ved oppstart.
     */
    @Test
    void clientReadsCoursesFromServer() {
        List<Course> courses = repository.findAllCourses();

        assertFalse(courses.isEmpty());
        assertTrue(courses.stream().anyMatch(course -> course.getCourseCode().equals("IT1901")));
    }

    /**
     * Verifiserer hele flyten for en ny bruker: registrering gir et token, anmeldelsen sendes med
     * tokenet, og den kan hentes igjen både offentlig og blant brukerens egne anmeldelser.
     */
    @Test
    void registeredUserSubmitsAndReadsReview() {
        AuthenticationResult user = signIn(authenticationClient.register("Kari Nordmann",
                "kari-klient@example.com", PASSWORD));

        reviewService.submitReview("IT1604", "Digitalt samfunn", user.email(), user.name(), false,
                "4", "Bra øvinger");

        Review review = findByComment("it1604", "Bra øvinger");
        assertEquals("Kari Nordmann", review.getDisplayName());
        assertEquals(4, review.getRating());
        assertFalse(review.getId().isBlank());

        List<Course> own = repository.findReviewsByOwner(user.email());
        assertEquals(1, own.size());
        assertEquals("IT1604", own.get(0).getCourseCode());
        assertEquals(review.getId(), own.get(0).getReviews().get(0).getId());
        assertEquals("kari-klient@example.com", own.get(0).getReviews().get(0).getOwnerEmail());
    }

    /**
     * Verifiserer at en anonym anmeldelse vises som «Anonym» for andre, men fortsatt hører til
     * brukeren under egne anmeldelser.
     */
    @Test
    void anonymousReviewIsHiddenPubliclyButOwnedByUser() {
        AuthenticationResult user = signIn(
                authenticationClient.register("Ola Nordmann", "ola-anonym@example.com", PASSWORD));

        reviewService.submitReview("EXPH0300", "Examen philosophicum", user.email(), user.name(),
                true, "3", "Anonym kommentar");

        assertEquals("Anonym", findByComment("EXPH0300", "Anonym kommentar").getDisplayName());
        Review own = repository.findReviewsByOwner(user.email()).get(0).getReviews().get(0);
        assertTrue(own.isAnonymous());
        assertEquals("ola-anonym@example.com", own.getOwnerEmail());
    }

    /**
     * Verifiserer JSON-kontrakten i {@code docs/release3/rest-api.md}: emnet har kode, navn,
     * gjennomsnitt og anmeldelser, og hver anmeldelse har nøyaktig feltene id, displayName,
     * authorType, rating og comment. Eieren sendes aldri ut.
     *
     * @throws IOException          hvis forespørselen feiler
     * @throws InterruptedException hvis testen avbrytes
     */
    @Test
    void submittedReviewFollowsJsonContract() throws IOException, InterruptedException {
        AuthenticationResult user = signIn(
                authenticationClient.register("Kontrakt Bruker", "kontrakt@example.com", PASSWORD));
        reviewService.submitReview("HMS0002", "HMS-kurs for 1. årsstudenter", user.email(),
                user.name(), false, "5", "Kontrakt");

        HttpResponse<String> response = HttpClient.newHttpClient().send(HttpRequest
                .newBuilder(URI.create(serverUrl() + "/api/courses/HMS0002")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        JsonNode course = MAPPER.readTree(response.body());

        assertEquals(200, response.statusCode());
        assertEquals("HMS0002", course.get("code").asText());
        assertEquals("HMS-kurs for 1. årsstudenter", course.get("name").asText());
        assertTrue(course.get("averageRating").isNumber());

        JsonNode review = course.get("reviews").get(0);
        Set<String> fields = new HashSet<>();
        review.fieldNames().forEachRemaining(fields::add);
        assertEquals(Set.of("id", "displayName", "authorType", "rating", "comment"), fields);
        assertEquals("USER", review.get("authorType").asText());
        assertEquals("Kontrakt Bruker", review.get("displayName").asText());
        assertEquals(5, review.get("rating").asInt());
        assertEquals("Kontrakt", review.get("comment").asText());
    }

    /**
     * Verifiserer at en gjest uten token får beskjed om å logge inn, og at ingenting lagres.
     */
    @Test
    void guestCannotSubmitReview() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> repository.addReview("BI2081", "Natur, miljø og bærekraft",
                        new Review("Gjest", 4, "Skrevet som gjest")));

        assertEquals(NOT_LOGGED_IN, e.getMessage());
        assertTrue(repository.findCourse("BI2081").orElseThrow().getReviews().stream()
                .noneMatch(review -> review.getComment().equals("Skrevet som gjest")));
    }

    /**
     * Verifiserer at brukeren kan endre og slette sin egen anmeldelse med klienten, men ikke
     * andres.
     */
    @Test
    void userCanChangeOwnReviewButNotOthers() {
        AuthenticationResult owner = signIn(
                authenticationClient.register("Eier Bruker", "eier@example.com", PASSWORD));
        reviewService.submitReview("IT1901", "Informatikk prosjektarbeid I", owner.email(),
                owner.name(), false, "2", "Før endring");
        final String id = findByComment("IT1901", "Før endring").getId();

        signIn(authenticationClient.register("Annen Bruker", "annen@example.com", PASSWORD));
        assertThrows(NotReviewOwnerException.class,
                () -> repository.updateReview(id, 1, "Fra en annen", false));
        assertThrows(NotReviewOwnerException.class, () -> repository.deleteReview(id));

        signIn(authenticationClient.login("eier@example.com", PASSWORD));
        assertTrue(repository.updateReview(id, 5, "Etter endring", true));
        Review changed = findByComment("IT1901", "Etter endring");
        assertEquals(5, changed.getRating());
        assertEquals("Anonym", changed.getDisplayName());

        assertTrue(repository.deleteReview(id));
        assertFalse(repository.deleteReview(id));
        assertTrue(repository.findReviewsByOwner(owner.email()).isEmpty());
    }

    /**
     * Verifiserer passordbytte og utlogging: det nye passordet virker, det gamle gjør det ikke, og
     * et token som er logget ut, gir beskjed om å logge inn på nytt.
     */
    @Test
    void changePasswordAndLogoutWorkWithServer() {
        AuthenticationResult user = signIn(
                authenticationClient.register("Passord Bruker", "passord@example.com", PASSWORD));

        authenticationClient.changePassword(user.token(), user.email(), PASSWORD, "NyttPassord456");

        assertThrows(IllegalArgumentException.class,
                () -> authenticationClient.login(user.email(), PASSWORD));
        AuthenticationResult again = authenticationClient.login(user.email(), "NyttPassord456");
        assertEquals("Passord Bruker", again.name());

        repository.setToken(again.token());
        authenticationClient.logout(again.token());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> repository.findReviewsByOwner(user.email()));
        assertEquals(NOT_LOGGED_IN, e.getMessage());
    }

    /**
     * Verifiserer at klienten gir et tomt svar for et ukjent emne, og meldingen fra serveren når en
     * innlogget bruker prøver å anmelde det.
     */
    @Test
    void clientHandlesUnknownCourse() {
        signIn(authenticationClient.register("Ukjent Emne", "ukjent@example.com", PASSWORD));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> repository
                .addReview("FINNESIKKE9999", "Ukjent emne", new Review("Ukjent Emne", 3, "")));

        assertEquals("Fant ikke emnet FINNESIKKE9999.", e.getMessage());
        assertTrue(repository.findCourse("FINNESIKKE9999").isEmpty());
    }

    /**
     * Lar repositoryet sende tokenet fra innloggingen, slik appen gjør gjennom sesjonen.
     *
     * @param result innloggingen fra serveren
     * @return den samme innloggingen
     */
    private AuthenticationResult signIn(AuthenticationResult result) {
        repository.setToken(result.token());
        return result;
    }

    /**
     * Henter emnet fra serveren og finner anmeldelsen med en bestemt kommentar.
     *
     * @param courseCode emnekoden
     * @param comment    kommentaren til anmeldelsen
     * @return anmeldelsen slik serveren viser den offentlig
     */
    private Review findByComment(String courseCode, String comment) {
        return repository.findCourse(courseCode).orElseThrow().getReviews().stream()
                .filter(review -> review.getComment().equals(comment)).findFirst().orElseThrow();
    }

    private String serverUrl() {
        return "http://localhost:" + port;
    }
}