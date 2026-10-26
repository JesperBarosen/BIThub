package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import core.AuthorType;
import core.Course;
import core.DefaultCourses;
import core.NotReviewOwnerException;
import core.Review;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testklasse for {@link RemoteReviewRepository}.
 */
class RemoteReviewRepositoryTest {

    private HttpServer server;

    private String lastMethod;
    private String lastBody;
    private String lastAuthorization;
    private String lastContentType;
    private final AtomicInteger requestCount = new AtomicInteger();

    /**
     * Starter en falsk server på en ledig port før hver test.
     *
     * @throws IOException hvis serveren ikke kan startes
     */
    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.start();
    }

    /**
     * Stopper den falske serveren etter hver test.
     */
    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    /**
     * Henter adressen til den falske serveren.
     *
     * @return adressen, for eksempel "http://localhost:54321"
     */
    private int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private String serverUrl() {
        return "http://localhost:" + server.getAddress().getPort();
    }

    /**
     * Bestemmer hva den falske serveren skal svare på en sti, og husker forespørselen den fikk.
     *
     * @param path   stien, for eksempel "/api/courses"
     * @param status statuskoden serveren skal svare med
     * @param body   JSON-teksten serveren skal svare med
     */
    private void respond(String path, int status, String body) {
        server.createContext(path, exchange -> {
            requestCount.incrementAndGet();
            lastMethod = exchange.getRequestMethod();
            lastBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            lastAuthorization = exchange.getRequestHeaders().getFirst("Authorization");
            lastContentType = exchange.getRequestHeaders().getFirst("Content-Type");
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
    }

    /**
     * Verifiserer at standardkonstruktøren bruker standardadressen.
     */
    @Test
    void testDefaultConstructorUsesDefaultBaseUrl() {
        RemoteReviewRepository repository = new RemoteReviewRepository();
        assertEquals("http://localhost:8080", repository.getBaseUrl());
    }

    /**
     * Verifiserer at en oppgitt adresse beholdes.
     */
    @Test
    void testConstructorKeepsGivenBaseUrl() {
        RemoteReviewRepository repository = new RemoteReviewRepository("http://localhost:1234");
        assertEquals("http://localhost:1234", repository.getBaseUrl());
    }

    /**
     * Verifiserer at skråstrek på slutten av adressen fjernes.
     */
    @Test
    void testConstructorRemovesTrailingSlash() {
        RemoteReviewRepository repository = new RemoteReviewRepository("http://localhost:1234/");
        assertEquals("http://localhost:1234", repository.getBaseUrl());
    }

    /**
     * Verifiserer at null som adresse kaster IllegalArgumentException.
     */
    @Test
    void testConstructorThrowsWhenBaseUrlIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new RemoteReviewRepository(null));
    }

    /**
     * Verifiserer at en blank adresse kaster IllegalArgumentException.
     */
    @Test
    void testConstructorThrowsWhenBaseUrlIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new RemoteReviewRepository(" "));
    }

    /**
     * Verifiserer at findAllCourses leser emnene fra serveren.
     */
    @Test
    void testFindAllCoursesReadsCoursesFromServer() {
        respond("/api/courses", 200, """
                [
                  {"code": "IT1901", "name": "Prosjektarbeid", "averageRating": 4.5,
                   "reviewCount": 2},
                  {"code": "TDT4100", "name": "Objektorientert", "averageRating": 0.0,
                   "reviewCount": 0}
                ]
                """);

        List<Course> courses = new RemoteReviewRepository(serverUrl()).findAllCourses();

        assertEquals(2, courses.size());
        assertEquals("IT1901", courses.get(0).getCourseCode());
        assertEquals("Prosjektarbeid", courses.get(0).getCourseName());
        assertEquals("TDT4100", courses.get(1).getCourseCode());
    }

    /**
     * Verifiserer at findAllCourses kaster UncheckedIOException når serveren svarer med en feil.
     */
    @Test
    void testFindAllCoursesThrowsWhenServerFails() {
        respond("/api/courses", 500, "{}");
        RemoteReviewRepository repository = new RemoteReviewRepository(serverUrl());

        assertThrows(UncheckedIOException.class, repository::findAllCourses);
    }

    /**
     * Verifiserer at findAllCourses kaster UncheckedIOException når ingen server svarer.
     *
     * @throws IOException hvis en ledig port ikke kan finnes
     */
    @Test
    void testFindAllCoursesThrowsWhenServerIsDown() throws IOException {
        RemoteReviewRepository repository = new RemoteReviewRepository(
                "http://localhost:" + freePort());

        assertThrows(UncheckedIOException.class, repository::findAllCourses);
    }

    /**
     * Verifiserer at findCourse leser emnet og anmeldelsene, og at visningsnavnet fra serveren
     * vises uendret for alle tre typene anmeldelser.
     */
    @Test
    void testFindCourseReadsCourseWithReviews() {
        respond("/api/courses/IT1901", 200, """
                {"code": "IT1901", "name": "Prosjektarbeid", "averageRating": 4.0,
                 "reviews": [
                   {"id": "id-1", "displayName": "Kari", "authorType": "USER",
                    "rating": 5, "comment": "Lærerikt"},
                   {"id": "id-2", "displayName": "Anonym", "authorType": "ANONYMOUS",
                    "rating": 4, "comment": ""},
                   {"id": "id-3", "displayName": "Gjest", "authorType": "GUEST",
                    "rating": 3, "comment": "Greit"}
                 ]}
                """);

        Course course = new RemoteReviewRepository(serverUrl()).findCourse("IT1901").orElseThrow();

        assertEquals("GET", lastMethod);
        assertEquals("IT1901", course.getCourseCode());
        assertEquals("Prosjektarbeid", course.getCourseName());
        assertEquals(3, course.getReviews().size());
        Review first = course.getReviews().get(0);
        assertEquals("id-1", first.getId());
        assertEquals("Kari", first.getDisplayName());
        assertEquals(5, first.getRating());
        assertEquals("Lærerikt", first.getComment());
        assertNull(first.getOwnerEmail());
        assertEquals(AuthorType.GUEST, first.getAuthorType());
        assertEquals("Anonym", course.getReviews().get(1).getDisplayName());
        assertEquals("Gjest", course.getReviews().get(2).getDisplayName());
    }

    /**
     * Verifiserer at et ukjent emne (404) gir et tomt resultat i stedet for en feil.
     */
    @Test
    void testFindCourseReturnsEmptyWhenCourseIsUnknown() {
        respond("/api/courses/TDT9999", 404, "{\"status\": 404, \"error\": \"Not Found\", "
                + "\"message\": \"Fant ikke emnet TDT9999.\"}");

        Optional<Course> course = new RemoteReviewRepository(serverUrl()).findCourse("TDT9999");

        assertTrue(course.isEmpty());
    }

    /**
     * Verifiserer at findCourse gir et tomt resultat uten å spørre serveren når emnekoden mangler.
     */

    @Test
    void testFindCourseReturnsEmptyForMissingCode() {
        respond("/", 200, "{}");
        RemoteReviewRepository repository = new RemoteReviewRepository(serverUrl());

        assertTrue(repository.findCourse(null).isEmpty());
        assertTrue(repository.findCourse(" ").isEmpty());
        assertEquals(0, requestCount.get());
    }

    /**
     * Verifiserer at 400 fra serveren gir IllegalArgumentException med meldingen fra feilsvaret.
     */
    @Test
    void testFindCourseThrowsIllegalArgumentOnBadRequest() {
        respond("/api/courses/ugyldig", 400, "{\"status\": 400, \"error\": \"Bad Request\", "
                + "\"message\": \"Ugyldig emnekode.\"}");
        RemoteReviewRepository repository = new RemoteReviewRepository(serverUrl());

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> repository.findCourse("ugyldig"));
        assertEquals("Ugyldig emnekode.", e.getMessage());
    }

    /**
     * Verifiserer at 500 fra serveren gir UncheckedIOException, slik at appen viser feilmeldingen
     * for lesefeil.
     */
    @Test
    void testFindCourseThrowsUncheckedIoOnServerError() {
        respond("/api/courses/IT1901", 500, "{}");
        RemoteReviewRepository repository = new RemoteReviewRepository(serverUrl());

        assertThrows(UncheckedIOException.class, () -> repository.findCourse("IT1901"));
    }

    /**
     * Verifiserer at addReview sender bare vurdering, kommentar og anonymitet som JSON, og at id og
     * navn ikke sendes, fordi serveren bestemmer dem.
     *
     * @throws IOException hvis JSON-en i forespørselen ikke kan leses
     */
    @Test
    void testAddReviewPostsOnlyRatingCommentAndAnonymous() throws IOException {
        respond("/api/courses/IT1901/reviews", 201, """
                {"id": "ny-id", "displayName": "Gjest", "authorType": "GUEST",
                 "rating": 4, "comment": "Bra fag"}
                """);

        new RemoteReviewRepository(serverUrl()).addReview("IT1901", "Prosjektarbeid",
                new Review("Student", 4, "Bra fag"));

        assertEquals("POST", lastMethod);
        assertTrue(lastContentType.startsWith("application/json"));
        JsonNode body = new ObjectMapper().readTree(lastBody);
        assertEquals(3, body.size());
        assertEquals(4, body.get("rating").asInt());
        assertEquals("Bra fag", body.get("comment").asText());
        assertFalse(body.get("anonymous").asBoolean());
    }

    /**
     * Verifiserer at en anmeldelse som serveren avviser (400), gir IllegalArgumentException med
     * meldingen fra serveren.
     */
    @Test
    void testAddReviewThrowsIllegalArgumentOnBadRequest() {
        respond("/api/courses/IT1901/reviews", 400, "{\"status\": 400, \"error\": \"Bad Request\", "
                + "\"message\": \"Vurdering må være et tall fra 1 til 5.\"}");
        RemoteReviewRepository repository = new RemoteReviewRepository(serverUrl());
        Review review = new Review("Student", 4, "");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> repository.addReview("IT1901", "Prosjektarbeid", review));
        assertEquals("Vurdering må være et tall fra 1 til 5.", e.getMessage());
    }

    /**
     * Verifiserer at en anmeldelse til et ukjent emne (404) gir IllegalArgumentException.
     */
    @Test
    void testAddReviewThrowsIllegalArgumentWhenCourseIsUnknown() {
        respond("/api/courses/TDT9999/reviews", 404, "{\"status\": 404, \"error\": \"Not Found\", "
                + "\"message\": \"Fant ikke emnet TDT9999.\"}");
        RemoteReviewRepository repository = new RemoteReviewRepository(serverUrl());
        Review review = new Review("Student", 4, "");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> repository.addReview("TDT9999", "Ukjent", review));
        assertEquals("Fant ikke emnet TDT9999.", e.getMessage());
    }

    /**
     * Verifiserer at et feilsvar uten JSON gir en standardmelding.
     */
    @Test
    void testAddReviewUsesDefaultMessageWhenErrorIsNotJson() {
        respond("/api/courses/IT1901/reviews", 400, "ikke json");
        RemoteReviewRepository repository = new RemoteReviewRepository(serverUrl());
        Review review = new Review("Student", 4, "");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> repository.addReview("IT1901", "Prosjektarbeid", review));
        assertEquals("Serveren avviste forespørselen (status 400).", e.getMessage());
    }

    /**
     * Verifiserer at addReview gir UncheckedIOException når ingen server svarer.
     *
     * @throws IOException hvis en ledig port ikke kan finnes
     */
    @Test
    void testAddReviewThrowsWhenServerIsDown() throws IOException {
        RemoteReviewRepository repository = new RemoteReviewRepository(
                "http://localhost:" + freePort());
        Review review = new Review("Student", 4, "");

        assertThrows(UncheckedIOException.class,
                () -> repository.addReview("IT1901", "Prosjektarbeid", review));
    }

    /**
     * Verifiserer at addReview avviser manglende emnekode eller anmeldelse.
     */
    @Test
    void testAddReviewRejectsMissingArguments() {
        RemoteReviewRepository repository = new RemoteReviewRepository(serverUrl());
        Review review = new Review("Student", 4, "");

        assertThrows(IllegalArgumentException.class,
                () -> repository.addReview(null, "Prosjektarbeid", review));
        assertThrows(IllegalArgumentException.class,
                () -> repository.addReview("IT1901", "Prosjektarbeid", null));
    }

    /**
     * Verifiserer at addCoursesIfMissing ikke sender noen forespørsel, fordi serveren legger inn
     * standardemnene selv.
     */
    @Test
    void testAddCoursesIfMissingSendsNoRequest() {
        respond("/", 200, "{}");

        new RemoteReviewRepository(serverUrl()).addCoursesIfMissing(DefaultCourses.all());

        assertEquals(0, requestCount.get());
    }

    /**
     * Verifiserer at tokenet sendes i Authorization-headeren når det er satt, og at det ikke sendes
     * når det er fjernet.
     */
    @Test
    void testTokenIsSentInAuthorizationHeader() {
        respond("/api/courses", 200, "[]");
        RemoteReviewRepository repository = new RemoteReviewRepository(serverUrl());

        repository.findAllCourses();
        assertNull(lastAuthorization);

        repository.setToken("abc-123");
        repository.findAllCourses();
        assertEquals("Bearer abc-123", lastAuthorization);

        repository.setToken(null);
        repository.findAllCourses();
        assertNull(lastAuthorization);
    }

    /**
     * Verifiserer at en ugyldig serveradresse avvises med en gang.
     */
    @Test
    void testConstructorThrowsWhenBaseUrlIsInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> new RemoteReviewRepository("http://ugyldig adresse"));
    }

    /**
     * Verifiserer at egne anmeldelser hentes med token fra {@code GET /api/me/reviews}, og at de
     * grupperes etter emne med eier og anonymitet.
     */
    @Test
    void testFindReviewsByOwnerReadsOwnReviewsGroupedByCourse() {
        respond("/api/me/reviews", 200, """
                [
                  {"id": "id-1", "courseCode": "IT1901",
                   "courseName": "Informatikk prosjektarbeid I",
                   "rating": 4, "comment": "Bra", "anonymous": true},
                  {"id": "id-2", "courseCode": "TDT4100", "courseName": "Objektorientert",
                   "rating": 2, "comment": "", "anonymous": false},
                  {"id": "id-3", "courseCode": "IT1901",
                   "courseName": "Informatikk prosjektarbeid I",
                   "rating": 5, "comment": "Enda bedre", "anonymous": false}
                ]
                """);
        RemoteReviewRepository repository = new RemoteReviewRepository(serverUrl());
        repository.setToken("abc-123");

        List<Course> courses = repository.findReviewsByOwner("kari@ntnu.no");

        assertEquals("GET", lastMethod);
        assertEquals("Bearer abc-123", lastAuthorization);
        assertEquals(2, courses.size());
        assertEquals("IT1901", courses.get(0).getCourseCode());
        assertEquals(2, courses.get(0).getReviews().size());
        Review first = courses.get(0).getReviews().get(0);
        assertEquals("id-1", first.getId());
        assertEquals("kari@ntnu.no", first.getOwnerEmail());
        assertTrue(first.isAnonymous());
        assertEquals(4, first.getRating());
        assertEquals("Bra", first.getComment());
        assertEquals("TDT4100", courses.get(1).getCourseCode());
        assertFalse(courses.get(1).getReviews().get(0).isAnonymous());
    }

    /**
     * Verifiserer at henting av egne anmeldelser uten gyldig innlogging (401) gir
     * UncheckedIOException.
     */
    @Test
    void testFindReviewsByOwnerThrowsWhenNotLoggedIn() {
        respond("/api/me/reviews", 401, """
                {"status": 401, "error": "Unauthorized", "message": "Du må logge inn."}
                """);

        assertThrows(UncheckedIOException.class,
                () -> new RemoteReviewRepository(serverUrl()).findReviewsByOwner("kari@ntnu.no"));
    }

    /**
     * Verifiserer at en endring sendes med {@code PUT}, token og bare vurdering, kommentar og
     * anonymitet.
     *
     * @throws IOException hvis JSON-en i forespørselen ikke kan leses
     */
    @Test
    void testUpdateReviewPutsRatingCommentAndAnonymous() throws IOException {
        respond("/api/reviews/id-1", 200, """
                {"id": "id-1", "displayName": "Anonym", "authorType": "ANONYMOUS",
                 "rating": 2, "comment": "Tungt"}
                """);
        RemoteReviewRepository repository = new RemoteReviewRepository(serverUrl());
        repository.setToken("abc-123");

        assertTrue(repository.updateReview("id-1", 2, "Tungt", true));

        assertEquals("PUT", lastMethod);
        assertEquals("Bearer abc-123", lastAuthorization);
        assertTrue(lastContentType.startsWith("application/json"));
        JsonNode body = new ObjectMapper().readTree(lastBody);
        assertEquals(3, body.size());
        assertEquals(2, body.get("rating").asInt());
        assertEquals("Tungt", body.get("comment").asText());
        assertTrue(body.get("anonymous").asBoolean());
    }

    /**
     * Verifiserer at sletting sendes med {@code DELETE} og token.
     */
    @Test
    void testDeleteReviewSendsDelete() {
        respond("/api/reviews/id-1", 204, "");
        RemoteReviewRepository repository = new RemoteReviewRepository(serverUrl());
        repository.setToken("abc-123");

        assertTrue(repository.deleteReview("id-1"));

        assertEquals("DELETE", lastMethod);
        assertEquals("Bearer abc-123", lastAuthorization);
    }

    /**
     * Verifiserer at en anmeldelse serveren ikke finner (404), gir false ved endring og sletting.
     */
    @Test
    void testUpdateAndDeleteReturnFalseWhenReviewIsUnknown() {
        respond("/api/reviews/finnes-ikke", 404, """
                {"status": 404, "error": "Not Found", "message": "Fant ingen anmeldelse."}
                """);
        RemoteReviewRepository repository = new RemoteReviewRepository(serverUrl());

        assertFalse(repository.updateReview("finnes-ikke", 3, "", false));
        assertFalse(repository.deleteReview("finnes-ikke"));
    }

    /**
     * Verifiserer at en anmeldelse som tilhører en annen (403), gir NotReviewOwnerException ved
     * endring og sletting.
     */
    @Test
    void testUpdateAndDeleteThrowWhenReviewBelongsToOther() {
        respond("/api/reviews/id-2", 403, """
                {"status": 403, "error": "Forbidden",
                 "message": "Du har ikke tilgang til å gjøre denne endringen."}
                """);
        RemoteReviewRepository repository = new RemoteReviewRepository(serverUrl());

        assertThrows(NotReviewOwnerException.class,
                () -> repository.updateReview("id-2", 3, "", false));
        assertThrows(NotReviewOwnerException.class, () -> repository.deleteReview("id-2"));
    }

    /**
     * Verifiserer at en ugyldig vurdering som serveren avviser (400), gir IllegalArgumentException
     * med meldingen fra serveren.
     */
    @Test
    void testUpdateReviewThrowsIllegalArgumentOnBadRequest() {
        respond("/api/reviews/id-1", 400, """
                {"status": 400, "error": "Bad Request",
                 "message": "Vurdering må være et tall fra 1 til 5."}
                """);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new RemoteReviewRepository(serverUrl()).updateReview("id-1", 3, "", false));
        assertEquals("Vurdering må være et tall fra 1 til 5.", e.getMessage());
    }

    /**
     * Verifiserer at endring og sletting uten id avvises uten å sende noe til serveren.
     */
    @Test
    void testUpdateAndDeleteRejectMissingId() {
        RemoteReviewRepository repository = new RemoteReviewRepository(serverUrl());

        assertThrows(IllegalArgumentException.class,
                () -> repository.updateReview(" ", 3, "", false));
        assertThrows(IllegalArgumentException.class, () -> repository.deleteReview(null));
        assertEquals(0, requestCount.get());
    }
}