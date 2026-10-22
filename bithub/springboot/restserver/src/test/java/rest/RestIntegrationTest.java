package rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.Course;
import core.DefaultCourses;
import file.ReviewHandler;
import file.UserHandler;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Integrasjonstester som starter hele serveren på en ledig port og sender ekte HTTP-forespørsler.
 *
 * <p>Serveren bruker ekte {@code ReviewService} og {@code ReviewHandler}, men skriver til filer i
 * en midlertidig mappe, slik at testene aldri endrer dataene i {@code ~/.bithub}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RestIntegrationTest {

    @TempDir
    static Path tempDir;

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @LocalServerPort
    private int port;

    /**
     * Lar serveren bruke filer i den midlertidige mappen i stedet for standardfilene.
     *
     * @param registry innstillingene Spring bruker når serveren startes
     */
    @DynamicPropertySource
    static void useTemporaryFiles(DynamicPropertyRegistry registry) {
        registry.add("bithub.reviews-file", () -> coursesFile().toString());
        registry.add("bithub.users-file", () -> tempDir.resolve("users.json").toString());
    }

    private static Path coursesFile() {
        return tempDir.resolve("courses.json");
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri(path)).GET().build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String json)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private static int savedReviewCount(String courseCode) {
        return new ReviewHandler(coursesFile()).findCourse(courseCode)
                .map(course -> course.getReviews().size()).orElse(0);
    }

    private static long savedUserCount(String email) {
        return new UserHandler(tempDir.resolve("users.json")).getUsers().stream()
                .filter(user -> user.getEmail().equals(email))
                .count();
    }

    /**
     * Verifiserer at serveren ved første oppstart lager datafilen med standardemnene, og at listen
     * over emner kan hentes over HTTP.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void firstStartReturnsDefaultCourses() throws Exception {
        HttpResponse<String> response = get("/api/courses");

        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("")
                .startsWith("application/json"));
        JsonNode courses = MAPPER.readTree(response.body());
        assertEquals(DefaultCourses.all().size(), courses.size());
        assertTrue(Files.isRegularFile(coursesFile()));
    }

    /**
     * Verifiserer hele flyten for en ny anmeldelse: den sendes inn over HTTP, lagres i filen, og
     * hentes tilbake med samme id, også når emnekoden skrives med små bokstaver.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void submittedReviewIsSavedToFileAndReturnedByGet() throws Exception {
        HttpResponse<String> created = post("/api/courses/IT1604/reviews",
                "{\"rating\":5,\"comment\":\"Spennende emne\"}");

        assertEquals(201, created.statusCode());
        JsonNode review = MAPPER.readTree(created.body());
        final String id = review.get("id").asText();
        assertFalse(id.isBlank());
        assertEquals("GUEST", review.get("authorType").asText());
        assertEquals("Gjest", review.get("displayName").asText());

        HttpResponse<String> fetched = get("/api/courses/it1604");
        assertEquals(200, fetched.statusCode());
        JsonNode course = MAPPER.readTree(fetched.body());
        assertEquals("IT1604", course.get("code").asText());
        assertEquals(5.0, course.get("averageRating").asDouble());
        assertEquals(id, course.get("reviews").get(0).get("id").asText());
        assertFalse(course.get("reviews").get(0).has("ownerEmail"));

        // En ny leser bekrefter at anmeldelsen faktisk ligger i filen.
        Course saved = new ReviewHandler(coursesFile()).findCourse("IT1604").orElseThrow();
        assertEquals(1, saved.getReviews().size());
        assertEquals(id, saved.getReviews().get(0).getId());
        assertEquals("Spennende emne", saved.getReviews().get(0).getComment());
    }

    /**
     * Verifiserer at en ugyldig vurdering gir 400 med feilmelding i JSON, og at ingenting lagres.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void invalidRatingReturnsBadRequestAndSavesNothing() throws Exception {
        final int before = savedReviewCount("BI2081");

        HttpResponse<String> response = post("/api/courses/BI2081/reviews", "{\"rating\":6}");

        assertEquals(400, response.statusCode());
        JsonNode error = MAPPER.readTree(response.body());
        assertEquals(400, error.get("status").asInt());
        assertEquals("Bad Request", error.get("error").asText());
        assertFalse(error.get("message").asText().isBlank());
        assertEquals(before, savedReviewCount("BI2081"));
    }

    /**
     * Verifiserer at JSON som ikke kan leses, gir 400 med felles feilformat.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void unreadableJsonReturnsBadRequest() throws Exception {
        HttpResponse<String> response = post("/api/courses/BI2081/reviews", "{");

        assertEquals(400, response.statusCode());
        assertEquals("Ugyldig JSON eller feil datatype i forespørselen.",
                MAPPER.readTree(response.body()).get("message").asText());
    }

    /**
     * Verifiserer at et ukjent emne gir 404 både ved henting og innsending, og at det ukjente emnet
     * ikke blir lagret.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void unknownCourseReturnsNotFound() throws Exception {
        HttpResponse<String> fetched = get("/api/courses/TDT9999");
        assertEquals(404, fetched.statusCode());
        assertEquals("Fant ikke emnet TDT9999.",
                MAPPER.readTree(fetched.body()).get("message").asText());

        HttpResponse<String> posted = post("/api/courses/TDT9999/reviews", "{\"rating\":4}");
        assertEquals(404, posted.statusCode());
        assertEquals(404, MAPPER.readTree(posted.body()).get("status").asInt());
        assertTrue(new ReviewHandler(coursesFile()).findCourse("TDT9999").isEmpty());
    }

    /**
     * Verifiserer at bare en av flere samtidige registreringer med samme e-post lykkes gjennom
     * endepunktet. En får 201, de andre får 409, og bare en bruker lagres i filen.
     *
     * @throws Exception hvis en av forespørslene feiler uventet eller ikke blir ferdig i tide
     */
    @Test
    void simultaneousRegistrationsWithSameEmailSaveOneUser() throws Exception {
        int attempts = 6;
        String json = "{\"name\":\"Samtidig Bruker\",\"email\":\"samtidig@example.com\","
                + "\"password\":\"Passord123\"}";

        ExecutorService executor = Executors.newFixedThreadPool(attempts);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> results = new ArrayList<>();
        try {
            for (int i = 0; i < attempts; i++) {
                results.add(executor.submit(() -> {
                    start.await();
                    return post("/api/users", json).statusCode();
                }));
            }
            start.countDown();

            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> result : results) {
                statuses.add(result.get(10, TimeUnit.SECONDS));
            }

            assertEquals(1, Collections.frequency(statuses, 201));
            assertEquals(attempts - 1, Collections.frequency(statuses, 409));
            assertEquals(1L, savedUserCount("samtidig@example.com"));
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }
}