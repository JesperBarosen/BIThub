package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import core.Course;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testklasse for {@link RemoteReviewRepository}.
 */
class RemoteReviewRepositoryTest {

    private HttpServer server;

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
    private String serverUrl() {
        return "http://localhost:" + server.getAddress().getPort();
    }

    /**
     * Bestemmer hva den falske serveren skal svare på en sti.
     *
     * @param path stien, for eksempel "/api/courses"
     * @param status statuskoden serveren skal svare med
     * @param body JSON-teksten serveren skal svare med
     */
    private void respond(String path, int status, String body) {
        server.createContext(path, exchange -> {
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
        int freePort;
        try (ServerSocket socket = new ServerSocket(0)) {
            freePort = socket.getLocalPort();
        }
        RemoteReviewRepository repository =
                new RemoteReviewRepository("http://localhost:" + freePort);

        assertThrows(UncheckedIOException.class, repository::findAllCourses);
    }
}
