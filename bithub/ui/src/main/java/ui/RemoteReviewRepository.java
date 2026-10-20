package ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.Course;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Henter og lagrer emner og anmeldelser ved å sende HTTP-forespørsler til REST-serveren.
 */
public final class RemoteReviewRepository {

    /**
     * Adressen til serveren når ingen annen er oppgitt.
     */
    public static final String DEFAULT_BASE_URL = "http://localhost:8080";

    private final String baseUrl;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Oppretter et repository som bruker serveren på standardadressen.
     */
    public RemoteReviewRepository() {
        this(DEFAULT_BASE_URL);
    }

    /**
     * Oppretter et repository som bruker serveren på den oppgitte adressen.
     *
     * @param baseUrl adressen til serveren, for eksempel "http://localhost:8080"
     * @throws IllegalArgumentException hvis adressen er null eller blank
     */
    public RemoteReviewRepository(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Serveradressen kan ikke være tom.");
        }
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    /**
     * Henter adressen til serveren.
     *
     * @return adressen, uten skråstrek på slutten
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Henter alle emnene fra serveren, også de uten anmeldelser.
     *
     * @return alle emnene, uten anmeldelser
     * @throws UncheckedIOException hvis serveren ikke svarer eller svarer med en feil
     */
    public List<Course> findAllCourses() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/courses"))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new UncheckedIOException(new IOException(
                    "Serveren svarte med status " + response.statusCode() + "."));
        }
        List<Course> courses = new ArrayList<>();
        for (JsonNode courseNode : readJson(response.body())) {
            courses.add(new Course(courseNode.get("code").asText(),
                    courseNode.get("name").asText()));
        }
        return courses;
    }

    /**
     * Sender en forespørsel til serveren og venter på svaret.
     *
     * @param request forespørselen som skal sendes
     * @return svaret fra serveren
     * @throws UncheckedIOException hvis serveren ikke svarer
     */
    private HttpResponse<String> send(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new UncheckedIOException("Fikk ikke kontakt med serveren.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UncheckedIOException(
                    new IOException("Forespørselen til serveren ble avbrutt.", e));
        }
    }

    /**
     * Gjør om JSON-tekst fra serveren til en trestruktur som kan leses felt for felt.
     *
     * @param json teksten fra serveren
     * @return JSON-innholdet
     * @throws UncheckedIOException hvis teksten ikke er gyldig JSON
     */
    private JsonNode readJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            throw new UncheckedIOException("Ugyldig svar fra serveren.", e);
        }
    }
}
