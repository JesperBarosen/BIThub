package ui.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tester autentiseringsklienten mot en lokal falsk HTTP-server.
 */
class RemoteAuthenticationClientTest {

    private static final String SUCCESS_RESPONSE = """
            {"token":"test-token","name":"Kari Nordmann","email":"kari@ntnu.no"}
            """;

    private final ObjectMapper mapper = new ObjectMapper();
    private HttpServer server;
    private volatile String receivedMethod;
    private volatile String receivedBody;
    private volatile String receivedContentType;
    private volatile String receivedAuthorization;
    private volatile String receivedPath;

    /**
     * Starter en lokal testserver før hver test.
     *
     * @throws IOException hvis serveren ikke kan startes
     */
    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.start();
    }

    /**
     * Stopper testserveren etter hver test.
     */
    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    /**
     * Returnerer adressen til testserveren.
     */
    private String serverUrl() {
        return "http://localhost:" + server.getAddress().getPort();
    }

    /**
     * Gir et bestemt JSON-svar og registrerer mottatt forespørsel.
     */
    private void respond(String path, int status, String body) {
        server.createContext(path, exchange -> {
            receivedMethod = exchange.getRequestMethod();
            receivedPath = exchange.getRequestURI().getRawPath();
            receivedBody = new String(exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8);
            receivedContentType = exchange.getRequestHeaders().getFirst("Content-Type");
            receivedAuthorization = exchange.getRequestHeaders().getFirst("Authorization");
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        });
    }

    @Test
    void loginSendsCorrectJsonAndReturnsUser() throws IOException {
        respond("/api/login", 200, SUCCESS_RESPONSE);

        final AuthenticationResult result = new RemoteAuthenticationClient(serverUrl())
                .login("kari@ntnu.no", "passord\"123");

        assertEquals("POST", receivedMethod);
        JsonNode body = mapper.readTree(receivedBody);
        assertEquals("kari@ntnu.no", body.get("email").asText());
        assertEquals("passord\"123", body.get("password").asText());
        assertEquals(2, body.size());
        assertEquals("application/json", receivedContentType);
        assertNull(receivedAuthorization);
        assertEquals("test-token", result.token());
        assertEquals("Kari Nordmann", result.name());
        assertEquals("kari@ntnu.no", result.email());
        assertFalse(result.toString().contains("test-token"));
    }

    @Test
    void registerSendsCorrectJsonAndReturnsUser() throws IOException {
        respond("/api/users", 201, SUCCESS_RESPONSE);

        final AuthenticationResult result = new RemoteAuthenticationClient(serverUrl())
                .register("Kari Nordmann", "kari@ntnu.no", "Hemmelig123");

        assertEquals("POST", receivedMethod);
        JsonNode body = mapper.readTree(receivedBody);
        assertEquals("Kari Nordmann", body.get("name").asText());
        assertEquals("kari@ntnu.no", body.get("email").asText());
        assertEquals("Hemmelig123", body.get("password").asText());
        assertEquals(3, body.size());
        assertEquals("test-token", result.token());
    }

    @Test
    void loginShowsGenericMessageForWrongCredentials() {
        respond("/api/login", 401, "{\"status\":401,\"message\":\"Ukjent e-post.\"}");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new RemoteAuthenticationClient(serverUrl()).login("kari@ntnu.no", "feil"));

        assertEquals("Feil e-post eller passord.", error.getMessage());
    }

    @Test
    void registerShowsServerMessageForDuplicateEmail() {
        respond("/api/users", 409,
                "{\"status\":409,\"message\":\"E-posten er allerede registrert.\"}");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new RemoteAuthenticationClient(serverUrl()).register("Kari", "kari@ntnu.no",
                        "Hemmelig123"));

        assertEquals("E-posten er allerede registrert.", error.getMessage());
    }

    @Test
    void registerShowsValidationMessageForBadInput() {
        respond("/api/users", 400, "{\"status\":400,\"message\":\"Ugyldig e-post.\"}");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new RemoteAuthenticationClient(serverUrl()).register("Kari", "feil",
                        "Hemmelig123"));

        assertEquals("Ugyldig e-post.", error.getMessage());
    }

    @Test
    void serverErrorIsNotTreatedAsValidationError() {
        respond("/api/login", 500, "{\"message\":\"Intern feil\"}");

        assertThrows(UncheckedIOException.class, () -> new RemoteAuthenticationClient(serverUrl())
                .login("kari@ntnu.no", "Hemmelig123"));
    }

    @Test
    void malformedSuccessJsonIsRejected() {
        respond("/api/login", 200, "ikke-json");

        assertThrows(UncheckedIOException.class, () -> new RemoteAuthenticationClient(serverUrl())
                .login("kari@ntnu.no", "Hemmelig123"));
    }

    /**
     * Sjekker at en JSON-liste ikke godtas som et vellykket innloggingssvar.
     */
    @Test
    void loginRejectsJsonArrayAsSuccessResponse() {
        respond("/api/login", 200, "[]");

        UncheckedIOException error = assertThrows(UncheckedIOException.class,
                () -> new RemoteAuthenticationClient(serverUrl()).login("kari@ntnu.no",
                        "Hemmelig123"));

        assertEquals("Ugyldig svar fra serveren.", error.getMessage());
    }

    @Test
    void missingTokenIsRejected() {
        respond("/api/users", 201, "{\"name\":\"Kari Nordmann\",\"email\":\"kari@ntnu.no\"}");

        assertThrows(UncheckedIOException.class, () -> new RemoteAuthenticationClient(serverUrl())
                .register("Kari Nordmann", "kari@ntnu.no", "Hemmelig123"));
    }

    @Test
    void wrongSuccessStatusIsRejected() {
        respond("/api/login", 201, SUCCESS_RESPONSE);

        assertThrows(UncheckedIOException.class, () -> new RemoteAuthenticationClient(serverUrl())
                .login("kari@ntnu.no", "Hemmelig123"));
    }

    @Test
    void unavailableServerProducesNetworkError() throws IOException {
        int unusedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            unusedPort = socket.getLocalPort();
        }

        RemoteAuthenticationClient client = new RemoteAuthenticationClient(
                "http://localhost:" + unusedPort);
        assertThrows(UncheckedIOException.class, () -> client.login("kari@ntnu.no", "Hemmelig123"));
    }

    @Test
    void invalidBaseUrlIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new RemoteAuthenticationClient(" "));
        assertThrows(IllegalArgumentException.class,
                () -> new RemoteAuthenticationClient("localhost:8080"));
        assertNotNull(new RemoteAuthenticationClient(serverUrl()));
    }

    @Test
    void changePasswordSendsTokenAndPasswordsToEncodedPath() throws IOException {
        respond("/api/users/", 204, "");

        new RemoteAuthenticationClient(serverUrl()).changePassword("test-token", " kari@ntnu.no ",
                "Gammelt123", "Nytt12345");

        assertEquals("PUT", receivedMethod);
        assertEquals("/api/users/kari%40ntnu.no/password", receivedPath);
        assertEquals("Bearer test-token", receivedAuthorization);
        assertEquals("application/json", receivedContentType);
        JsonNode body = mapper.readTree(receivedBody);
        assertEquals("Gammelt123", body.get("oldPassword").asText());
        assertEquals("Nytt12345", body.get("newPassword").asText());
        assertEquals(2, body.size());
    }

    @Test
    void changePasswordShowsServerMessageForWrongPassword() {
        respond("/api/users/", 400,
                "{\"status\":400,\"message\":\"Det gamle passordet er feil.\"}");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new RemoteAuthenticationClient(serverUrl()).changePassword("test-token",
                        "kari@ntnu.no", "Feil12345", "Nytt12345"));

        assertEquals("Det gamle passordet er feil.", error.getMessage());
    }

    @Test
    void changePasswordShowsServerMessageForOtherUser() {
        respond("/api/users/", 403,
                "{\"status\":403,\"message\":\"Du kan bare endre ditt eget passord.\"}");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new RemoteAuthenticationClient(serverUrl()).changePassword("test-token",
                        "ola@ntnu.no", "Gammelt123", "Nytt12345"));

        assertEquals("Du kan bare endre ditt eget passord.", error.getMessage());
    }

    @Test
    void changePasswordAsksForNewLoginWhenTokenIsUnknown() {
        respond("/api/users/", 401, "{\"status\":401,\"message\":\"Du må være logget inn.\"}");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new RemoteAuthenticationClient(serverUrl()).changePassword("gammelt-token",
                        "kari@ntnu.no", "Gammelt123", "Nytt12345"));

        assertEquals("Du er ikke lenger logget inn. Logg inn på nytt for å bytte passord.",
                error.getMessage());
    }

    @Test
    void changePasswordTreatsServerErrorAsNetworkError() {
        respond("/api/users/", 500, "{\"message\":\"Intern feil\"}");

        assertThrows(UncheckedIOException.class, () -> new RemoteAuthenticationClient(serverUrl())
                .changePassword("test-token", "kari@ntnu.no", "Gammelt123", "Nytt12345"));
    }

    @Test
    void changePasswordRequiresTokenAndEmailWithoutRequest() {
        RemoteAuthenticationClient client = new RemoteAuthenticationClient(serverUrl());

        assertThrows(IllegalArgumentException.class,
                () -> client.changePassword(" ", "kari@ntnu.no", "Gammelt123", "Nytt12345"));
        assertThrows(IllegalArgumentException.class,
                () -> client.changePassword("test-token", null, "Gammelt123", "Nytt12345"));
        assertNull(receivedMethod);
    }

    @Test
    void logoutSendsTokenWithPost() {
        respond("/api/logout", 204, "");

        new RemoteAuthenticationClient(serverUrl()).logout("test-token");

        assertEquals("POST", receivedMethod);
        assertEquals("Bearer test-token", receivedAuthorization);
    }

    @Test
    void logoutAcceptsUnknownToken() {
        respond("/api/logout", 401, "{\"status\":401,\"message\":\"Du må være logget inn.\"}");

        new RemoteAuthenticationClient(serverUrl()).logout("gammelt-token");

        assertEquals("POST", receivedMethod);
    }

    @Test
    void logoutTreatsServerErrorAsNetworkError() {
        respond("/api/logout", 500, "{}");

        assertThrows(UncheckedIOException.class,
                () -> new RemoteAuthenticationClient(serverUrl()).logout("test-token"));
    }

    @Test
    void logoutRequiresToken() {
        assertThrows(IllegalArgumentException.class,
                () -> new RemoteAuthenticationClient(serverUrl()).logout(null));
        assertNull(receivedMethod);
    }
}
