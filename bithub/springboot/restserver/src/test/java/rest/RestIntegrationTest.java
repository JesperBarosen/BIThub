package rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.model.Course;
import core.repository.ReviewRepository;
import core.service.DefaultCourses;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Integrasjonstester som starter hele serveren på en ledig port og sender ekte HTTP-forespørsler.
 *
 * <p>Serveren bruker ekte tjenester i core og ekte lagring i file, men skriver til filer i en
 * midlertidig mappe, slik at testene aldri endrer dataene i {@code ~/.bithub}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RestIntegrationTest {

    @TempDir
    static Path tempDir;

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Autowired
    private ReviewRepository reviewRepository;

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

    private HttpResponse<String> post(String path, String json, String token)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> put(String path, String json, String token)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .PUT(HttpRequest.BodyPublishers.ofString(json)).build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> delete(String path, String token)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri(path))
                .header("Authorization", "Bearer " + token).DELETE().build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String register(String name, String email) throws Exception {
        String json = "{\"name\":\"" + name + "\",\"email\":\"" + email
                + "\",\"password\":\"Passord123\"}";
        HttpResponse<String> registered = post("/api/users", json);
        assertEquals(201, registered.statusCode());
        return MAPPER.readTree(registered.body()).get("token").asText();
    }

    private String createReview(String token, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri("/api/courses/IT1901/reviews"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> created = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, created.statusCode());
        return MAPPER.readTree(created.body()).get("id").asText();
    }

    private static core.model.Review savedReview(String id) {
        return new ReviewHandler(coursesFile()).findReview(id).orElseThrow();
    }

    /**
     * Verifiserer at utlogging over HTTP gjør tokenet ubrukelig på beskyttede endepunkter.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void logoutInvalidatesToken() throws Exception {
        String registration = "{\"name\":\"Ola Nordmann\",\"email\":\"logout@example.com\","
                + "\"password\":\"Passord123\"}";
        HttpResponse<String> registered = post("/api/users", registration);
        assertEquals(201, registered.statusCode());
        String token = MAPPER.readTree(registered.body()).get("token").asText();

        HttpRequest ownReviews = HttpRequest.newBuilder(uri("/api/me/reviews"))
                .header("Authorization", "Bearer " + token).GET().build();
        assertEquals(200,
                CLIENT.send(ownReviews, HttpResponse.BodyHandlers.ofString()).statusCode());

        HttpRequest logout = HttpRequest.newBuilder(uri("/api/logout"))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<String> loggedOut = CLIENT.send(logout, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, loggedOut.statusCode());
        assertEquals("", loggedOut.body());

        HttpResponse<String> denied = CLIENT.send(ownReviews, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, denied.statusCode());
        assertEquals(401, MAPPER.readTree(denied.body()).get("status").asInt());
        assertEquals(401, CLIENT.send(logout, HttpResponse.BodyHandlers.ofString()).statusCode());

        String change = "{\"oldPassword\":\"Passord123\",\"newPassword\":\"NyttPassord123\"}";
        assertEquals(401,
                put("/api/users/logout@example.com/password", change, token).statusCode());
        String login = "{\"email\":\"logout@example.com\",\"password\":\"Passord123\"}";
        assertEquals(200, post("/api/login", login).statusCode());

        final int before = savedReviewCount("IT1901");
        HttpRequest reviewRequest = HttpRequest.newBuilder(uri("/api/courses/IT1901/reviews"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"rating\":4}")).build();
        assertEquals(401,
                CLIENT.send(reviewRequest, HttpResponse.BodyHandlers.ofString()).statusCode());
        assertEquals(before, savedReviewCount("IT1901"));
    }

    /**
     * Kontrollerer eierskap i fil, anonymitet i offentlig svar og tilgang til egne anmeldelser.
     *
     * @param anonymous om anmeldelsen skal vises anonymt
     * @throws Exception hvis en forespørsel feiler
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void submittedReviewBelongsToAuthenticatedUser(boolean anonymous) throws Exception {
        final String email = "review-" + anonymous + "@example.com";
        String registration = "{\"name\":\"Ola Nordmann\",\"email\":\"" + email
                + "\",\"password\":\"Passord123\"}";
        HttpResponse<String> registered = post("/api/users", registration);
        assertEquals(201, registered.statusCode());
        final String token = MAPPER.readTree(registered.body()).get("token").asText();

        String body = "{\"rating\":4,\"comment\":\"  Bra fag  \",\"anonymous\":" + anonymous
                + ",\"ownerEmail\":\"someone-else@example.com\",\"reviewerName\":\"Falskt Navn\"}";
        HttpRequest request = HttpRequest.newBuilder(uri("/api/courses/IT1901/reviews"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> created = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, created.statusCode());
        JsonNode response = MAPPER.readTree(created.body());
        final String id = response.get("id").asText();
        assertEquals(anonymous ? "ANONYMOUS" : "USER", response.get("authorType").asText());
        assertEquals(anonymous ? "Anonym" : "Ola Nordmann", response.get("displayName").asText());
        assertFalse(response.has("ownerEmail"));
        assertFalse(response.has("reviewerName"));

        core.model.Review saved = new ReviewHandler(coursesFile()).findCourse("IT1901").orElseThrow()
                .getReviews().stream().filter(review -> id.equals(review.getId())).findFirst()
                .orElseThrow();
        assertEquals(email, saved.getOwnerEmail());
        assertEquals("Ola Nordmann", saved.getReviewerName());
        assertEquals(anonymous, saved.isAnonymous());
        assertEquals("Bra fag", saved.getComment());

        JsonNode course = MAPPER.readTree(get("/api/courses/IT1901").body());
        JsonNode publicReview = course.get("reviews").findParents("id").stream()
                .filter(review -> id.equals(review.get("id").asText())).findFirst().orElseThrow();
        assertEquals(response, publicReview);

        HttpRequest ownReviews = HttpRequest.newBuilder(uri("/api/me/reviews"))
                .header("Authorization", "Bearer " + token).GET().build();
        HttpResponse<String> own = CLIENT.send(ownReviews, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, own.statusCode());
        JsonNode reviews = MAPPER.readTree(own.body());
        assertEquals(1, reviews.size());
        assertEquals(id, reviews.get(0).get("id").asText());
        assertEquals(anonymous, reviews.get(0).get("anonymous").asBoolean());
    }

    /**
     * Verifiserer at tokenet bestemmer hvilke anmeldelser brukeren får se under egne anmeldelser.
     *
     * @throws Exception hvis en av forespørslene feiler
     */
    @Test
    void ownReviewsAreSeparatedBetweenUsers() throws Exception {
        List<String> tokens = new ArrayList<>();
        List<String> reviewIds = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            String registration = "{\"name\":\"Testbruker\",\"email\":\"isolation-" + i
                    + "@example.com\",\"password\":\"Passord123\"}";
            HttpResponse<String> registered = post("/api/users", registration);
            assertEquals(201, registered.statusCode());
            String token = MAPPER.readTree(registered.body()).get("token").asText();
            tokens.add(token);

            HttpRequest reviewRequest = HttpRequest.newBuilder(uri("/api/courses/IT1901/reviews"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"rating\":4,\"anonymous\":true}"))
                    .build();
            HttpResponse<String> created = CLIENT.send(reviewRequest,
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(201, created.statusCode());
            reviewIds.add(MAPPER.readTree(created.body()).get("id").asText());
        }

        for (int i = 0; i < 2; i++) {
            // En ekstra e-post i forespørselen skal ikke kunne overstyre tokenets identitet.
            String otherEmail = "isolation-" + (1 - i) + "@example.com";
            HttpRequest ownRequest = HttpRequest
                    .newBuilder(uri(
                            "/api/me/reviews?email=" + otherEmail + "&ownerEmail=" + otherEmail))
                    .header("Authorization", "Bearer " + tokens.get(i)).GET().build();
            HttpResponse<String> response = CLIENT.send(ownRequest,
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            JsonNode reviews = MAPPER.readTree(response.body());
            assertEquals(1, reviews.size());
            assertEquals(reviewIds.get(i), reviews.get(0).get("id").asText());
            assertTrue(reviews.get(0).get("anonymous").asBoolean());
            assertFalse(reviews.get(0).has("ownerEmail"));
        }
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
                .filter(user -> user.getEmail().equals(email)).count();
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
        String token = register("Kari Nordmann", "kari-it1604@example.com");
        HttpResponse<String> created = post("/api/courses/IT1604/reviews",
                "{\"rating\":5,\"comment\":\"Spennende emne\"}", token);

        assertEquals(201, created.statusCode());
        JsonNode review = MAPPER.readTree(created.body());
        final String id = review.get("id").asText();
        assertFalse(id.isBlank());
        assertEquals("USER", review.get("authorType").asText());
        assertEquals("Kari Nordmann", review.get("displayName").asText());

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
        assertEquals("kari-it1604@example.com", saved.getReviews().get(0).getOwnerEmail());
        assertEquals("Spennende emne", saved.getReviews().get(0).getComment());
    }

    /**
     * Verifiserer at en gjest uten token ikke kan skrive en anmeldelse. Serveren svarer 401, og
     * ingenting lagres.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void reviewWithoutTokenReturnsUnauthorizedAndSavesNothing() throws Exception {
        final int before = savedReviewCount("EXPH0300");

        HttpResponse<String> response = post("/api/courses/EXPH0300/reviews",
                "{\"rating\":4,\"comment\":\"Skrevet som gjest\"}");

        assertEquals(401, response.statusCode());
        assertEquals(401, MAPPER.readTree(response.body()).get("status").asInt());
        assertEquals(before, savedReviewCount("EXPH0300"));
    }

    /**
     * Verifiserer at en ugyldig vurdering gir 400 med feilmelding i JSON, og at ingenting lagres.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void invalidRatingReturnsBadRequestAndSavesNothing() throws Exception {
        final int before = savedReviewCount("BI2081");
        String token = register("Ugyldig Vurdering", "invalid-rating@example.com");

        HttpResponse<String> response = post("/api/courses/BI2081/reviews", "{\"rating\":6}",
                token);

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

        String token = register("Ukjent Emne", "unknown-course@example.com");
        HttpResponse<String> posted = post("/api/courses/TDT9999/reviews", "{\"rating\":4}", token);
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

    /**
     * Verifiserer at et nytt passord lagres, slik at brukeren etterpå kan logge inn med det nye
     * passordet, men ikke med det gamle.
     *
     * @throws Exception hvis en av forespørslene feiler
     */
    @Test
    void changedPasswordIsSavedAndUsedAtLogin() throws Exception {
        String register = "{\"name\":\"Kari Nordmann\",\"email\":\"kari@example.com\","
                + "\"password\":\"Passord123\"}";
        HttpResponse<String> registered = post("/api/users", register);
        assertEquals(201, registered.statusCode());
        String token = MAPPER.readTree(registered.body()).get("token").asText();

        String change = "{\"oldPassword\":\"Passord123\",\"newPassword\":\"NyttPassord123\"}";
        assertEquals(204, put("/api/users/kari@example.com/password", change, token).statusCode());

        String newLogin = "{\"email\":\"kari@example.com\",\"password\":\"NyttPassord123\"}";
        assertEquals(200, post("/api/login", newLogin).statusCode());

        String oldLogin = "{\"email\":\"kari@example.com\",\"password\":\"Passord123\"}";
        assertEquals(401, post("/api/login", oldLogin).statusCode());
    }

    /**
     * Avviser bruker A selv om forespørselen inneholder bruker Bs riktige gamle passord.
     *
     * @throws Exception hvis en av forespørslene feiler
     */
    @Test
    void anotherUsersTokenCannotChangePassword() throws Exception {
        String first = "{\"name\":\"Bruker A\",\"email\":\"user-a@example.com\","
                + "\"password\":\"Passord123\"}";
        HttpResponse<String> registered = post("/api/users", first);
        assertEquals(201, registered.statusCode());
        final String token = MAPPER.readTree(registered.body()).get("token").asText();
        String second = "{\"name\":\"Bruker B\",\"email\":\"user-b@example.com\","
                + "\"password\":\"Passord123\"}";
        assertEquals(201, post("/api/users", second).statusCode());

        String change = "{\"oldPassword\":\"Passord123\",\"newPassword\":\"NyttPassord123\"}";
        HttpResponse<String> denied = put("/api/users/user-b@example.com/password", change, token);
        assertEquals(403, denied.statusCode());
        assertEquals(403, MAPPER.readTree(denied.body()).get("status").asInt());

        String oldLogin = "{\"email\":\"user-b@example.com\",\"password\":\"Passord123\"}";
        assertEquals(200, post("/api/login", oldLogin).statusCode());
        String newLogin = "{\"email\":\"user-b@example.com\",\"password\":\"NyttPassord123\"}";
        assertEquals(401, post("/api/login", newLogin).statusCode());
    }

    /**
     * Verifiserer at eieren kan endre og slette sin egen anmeldelse, at endringen lagres i filen
     * med samme eier, og at anmeldelsen er borte etter sletting.
     *
     * @throws Exception hvis en av forespørslene feiler
     */
    @Test
    void ownerCanUpdateAndDeleteOwnReview() throws Exception {
        String token = register("Eier Nordmann", "crud-owner@example.com");
        String id = createReview(token,
                "{\"rating\":2,\"comment\":\"Første inntrykk\",\"anonymous\":false}");

        HttpResponse<String> updated = put("/api/reviews/" + id,
                "{\"rating\":5,\"comment\":\"Bedre enn ventet\",\"anonymous\":true}", token);
        assertEquals(200, updated.statusCode());
        JsonNode response = MAPPER.readTree(updated.body());
        assertEquals(id, response.get("id").asText());
        assertEquals("ANONYMOUS", response.get("authorType").asText());
        assertEquals("Anonym", response.get("displayName").asText());
        assertEquals(5, response.get("rating").asInt());
        assertFalse(response.has("ownerEmail"));

        core.model.Review saved = savedReview(id);
        assertEquals("crud-owner@example.com", saved.getOwnerEmail());
        assertEquals(5, saved.getRating());
        assertEquals("Bedre enn ventet", saved.getComment());
        assertTrue(saved.isAnonymous());

        HttpResponse<String> deleted = delete("/api/reviews/" + id, token);
        assertEquals(204, deleted.statusCode());
        assertEquals("", deleted.body());
        assertTrue(new ReviewHandler(coursesFile()).findReview(id).isEmpty());

        assertEquals(404, delete("/api/reviews/" + id, token).statusCode());
    }

    /**
     * Verifiserer at bruker A verken kan endre eller slette bruker Bs anmeldelse, og at anmeldelsen
     * er uendret etterpå.
     *
     * @throws Exception hvis en av forespørslene feiler
     */
    @Test
    void anotherUserCannotUpdateOrDeleteReview() throws Exception {
        String tokenA = register("Bruker A", "intruder-a@example.com");
        String tokenB = register("Bruker B", "owner-b@example.com");
        String id = createReview(tokenB,
                "{\"rating\":4,\"comment\":\"Bs anmeldelse\",\"anonymous\":false}");

        HttpResponse<String> updated = put("/api/reviews/" + id,
                "{\"rating\":1,\"comment\":\"Endret av A\",\"anonymous\":false}", tokenA);
        assertEquals(403, updated.statusCode());
        assertEquals(403, MAPPER.readTree(updated.body()).get("status").asInt());

        HttpResponse<String> deleted = delete("/api/reviews/" + id, tokenA);
        assertEquals(403, deleted.statusCode());

        core.model.Review saved = savedReview(id);
        assertEquals("owner-b@example.com", saved.getOwnerEmail());
        assertEquals(4, saved.getRating());
        assertEquals("Bs anmeldelse", saved.getComment());
    }

    /**
     * Verifiserer at en gjesteanmeldelse fra release 2 ikke kan endres eller slettes, heller ikke
     * av en innlogget bruker. Slike anmeldelser kan ikke lages over API-et lenger, så den legges
     * rett i lagringen.
     *
     * @throws Exception hvis en av forespørslene feiler
     */
    @Test
    void guestReviewCannotBeUpdatedOrDeleted() throws Exception {
        core.model.Review guestReview = new core.model.Review("Gjest", 3, "Skrevet som gjest");
        reviewRepository.addReview("IT1901", "Informatikk prosjektarbeid I", guestReview);
        String id = guestReview.getId();
        String token = register("Innlogget Bruker", "guest-test@example.com");

        assertEquals(403, put("/api/reviews/" + id, "{\"rating\":1,\"anonymous\":false}", token)
                .statusCode());
        assertEquals(403, delete("/api/reviews/" + id, token).statusCode());

        core.model.Review saved = savedReview(id);
        assertEquals(null, saved.getOwnerEmail());
        assertEquals(3, saved.getRating());
    }

    /**
     * Verifiserer at et token som er logget ut, ikke kan brukes til å endre eller slette egne
     * anmeldelser.
     *
     * @throws Exception hvis en av forespørslene feiler
     */
    @Test
    void loggedOutTokenCannotUpdateOrDeleteReview() throws Exception {
        String token = register("Utlogget Bruker", "logout-review@example.com");
        String id = createReview(token, "{\"rating\":4,\"anonymous\":false}");

        HttpRequest logout = HttpRequest.newBuilder(uri("/api/logout"))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody()).build();
        assertEquals(204, CLIENT.send(logout, HttpResponse.BodyHandlers.ofString()).statusCode());

        HttpResponse<String> updated = put("/api/reviews/" + id,
                "{\"rating\":1,\"anonymous\":false}", token);
        assertEquals(401, updated.statusCode());
        assertEquals(401, MAPPER.readTree(updated.body()).get("status").asInt());
        assertEquals(401, delete("/api/reviews/" + id, token).statusCode());

        assertEquals(4, savedReview(id).getRating());
    }
}