package rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.Course;
import core.Review;
import core.ReviewService;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ui.RemoteReviewRepository;

/**
 * Integrasjonstester der klienten fra ui snakker med hele serveren over HTTP.
 *
 * <p>Serveren startes på en ledig port og lagrer i en midlertidig mappe, slik at testene aldri
 * endrer dataene i {@code ~/.bithub}. Klienten er den samme {@link RemoteReviewRepository} som
 * appen bruker, så testene sjekker at klient og server er enige om JSON-formatet.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RemoteClientIntegrationTest {

    @TempDir
    static Path tempDir;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @LocalServerPort
    private int port;

    private RemoteReviewRepository repository;
    private ReviewService reviewService;

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
     * Kobler klienten til serveren som kjører på testporten.
     */
    @BeforeEach
    void connectClient() {
        repository = new RemoteReviewRepository("http://localhost:" + port);
        reviewService = new ReviewService(repository);
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
     * Verifiserer at en anmeldelse sendt inn med klienten lagres på serveren og kan hentes igjen.
     */
    @Test
    void clientSubmitsAndReadsReview() {
        reviewService.submitReview("IT1604", "Digitalt samfunn", "Student", "4", "Bra øvinger");

        Course course = repository.findCourse("it1604").orElseThrow();
        Review review = course.getReviews().stream()
                .filter(saved -> saved.getComment().equals("Bra øvinger"))
                .findFirst().orElseThrow();

        assertEquals("IT1604", course.getCourseCode());
        assertEquals(4, review.getRating());
        assertEquals("Gjest", review.getReviewerName());
        assertFalse(review.getId().isBlank());
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
        reviewService.submitReview("HMS0002", "HMS-kurs for 1. årsstudenter", "Student", "5",
                "Kontrakt");

        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port
                        + "/api/courses/HMS0002")).GET().build(),
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
        assertEquals("GUEST", review.get("authorType").asText());
        assertEquals(5, review.get("rating").asInt());
        assertEquals("Kontrakt", review.get("comment").asText());
    }

    /**
     * Verifiserer at klienten får feilmeldingen fra serveren når emnet ikke finnes, og at
     * emnet ikke blir opprettet.
     */
    @Test
    void clientGetsServerMessageForUnknownCourse() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> repository.addReview("FINNESIKKE9999", "Ukjent emne",
                        new Review("Student", 3, "")));

        assertFalse(e.getMessage().isBlank());
        assertTrue(repository.findCourse("FINNESIKKE9999").isEmpty());
    }

    /**
     * Verifiserer at klienten gir et tomt svar for et emne serveren ikke kjenner.
     */
    @Test
    void clientReturnsEmptyForUnknownCourse() {
        Optional<Course> course = repository.findCourse("FINNESIKKE9999");

        assertTrue(course.isEmpty());
    }
}