package ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.Course;
import core.Review;
import core.ReviewRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Henter og lagrer emner og anmeldelser ved å sende HTTP-forespørsler til REST-serveren.
 *
 * <p>Klassen implementerer {@link ReviewRepository}, slik at {@code ReviewService} og kontrollerne
 * kan bruke serveren uten å vite at dataene kommer over HTTP. Formatet på forespørslene og svarene
 * er beskrevet i {@code docs/release3/rest-api.md}.
 *
 * <p>Feil håndteres slik: <ul> <li>{@code 400}, {@code 401}, {@code 403} og {@code 404} gir
 * {@link IllegalArgumentException} med meldingen fra serveren, unntatt {@code 404} i
 * {@link #findCourse(String)}, som gir et tomt resultat.</li> <li>Andre feilkoder, ugyldige svar,
 * nettverksfeil og tidsavbrudd gir {@link UncheckedIOException}.</li> </ul>
 */
public final class RemoteReviewRepository implements ReviewRepository {

    /**
     * Adressen til serveren når ingen annen er oppgitt.
     */
    public static final String DEFAULT_BASE_URL = "http://localhost:8080";

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final String baseUrl;
    private final Duration requestTimeout;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String token;

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
     * @throws IllegalArgumentException hvis adressen er null, blank eller ugyldig
     */
    public RemoteReviewRepository(String baseUrl) {
        this(baseUrl, REQUEST_TIMEOUT);
    }

    /**
     * Oppretter et repository med egen tidsgrense per forespørsel. Brukes av testene, slik at
     * tidsavbrudd kan testes uten å vente i ti sekunder.
     *
     * @param baseUrl        adressen til serveren
     * @param requestTimeout hvor lenge en forespørsel kan vente på svar
     * @throws IllegalArgumentException hvis adressen er null, blank eller ugyldig
     */
    RemoteReviewRepository(String baseUrl, Duration requestTimeout) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Serveradressen kan ikke være tom.");
        }
        String trimmed = baseUrl.strip();
        this.baseUrl = trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
        // Sjekker adressen med en gang, slik at en skrivefeil oppdages ved oppstart.
        URI.create(this.baseUrl);
        this.requestTimeout = requestTimeout;
        this.httpClient = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
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
     * Setter tokenet som sendes i {@code Authorization}-headeren. Uten token sendes forespørslene
     * som gjest.
     *
     * @param token tokenet fra innloggingen, eller null for å fjerne det
     */
    public void setToken(String token) {
        this.token = token == null || token.isBlank() ? null : token;
    }

    /**
     * Henter alle emnene fra serveren med {@code GET /api/courses}, også de uten anmeldelser.
     * Emnene i listen har ikke med anmeldelsene; de hentes med {@link #findCourse(String)}.
     *
     * @return alle emnene, uten anmeldelser
     * @throws UncheckedIOException hvis serveren ikke svarer eller svarer med en feil
     */
    @Override
    public List<Course> findAllCourses() {
        HttpResponse<String> response = send(request("/api/courses").GET().build());
        checkStatus(response);

        List<Course> courses = new ArrayList<>();
        for (JsonNode courseNode : readJson(response.body())) {
            courses.add(new Course(text(courseNode, "code"), text(courseNode, "name")));
        }
        return courses;
    }

    /**
     * Henter ett emne med anmeldelser fra serveren med {@code GET /api/courses/{code}}.
     *
     * <p>Serveren sender visningsnavnet, men ikke eieren eller om anmeldelsen er anonym. Derfor
     * lages anmeldelsene uten eier, med visningsnavnet som navn. Da vises de med navnet serveren
     * har bestemt, enten det er brukerens navn, «Anonym» eller «Gjest».
     *
     * @param courseCode emnekoden, store og små bokstaver skilles ikke
     * @return emnet med anmeldelser, eller tomt hvis emnet ikke finnes
     * @throws IllegalArgumentException hvis serveren svarer med 400
     * @throws UncheckedIOException     hvis serveren ikke svarer eller svarer med en annen feil
     */
    @Override
    public Optional<Course> findCourse(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) {
            return Optional.empty();
        }
        HttpResponse<String> response = send(request(coursePath(courseCode)).GET().build());
        if (response.statusCode() == 404) {
            return Optional.empty();
        }
        checkStatus(response);

        JsonNode courseNode = readJson(response.body());
        Course course = new Course(text(courseNode, "code"), text(courseNode, "name"));
        for (JsonNode reviewNode : courseNode.path("reviews")) {
            course.addReview(toReview(reviewNode));
        }
        return Optional.of(course);
    }

    /**
     * Sender en ny anmeldelse til serveren med {@code POST /api/courses/{code}/reviews}.
     *
     * <p>Bare vurderingen, kommentaren og om anmeldelsen skal være anonym sendes. Serveren lager
     * id-en selv og bestemmer eieren ut fra tokenet, så id-en og navnet i {@code review} brukes
     * ikke.
     *
     * @param courseCode emnekoden
     * @param courseName navnet på emnet, brukes ikke fordi serveren kjenner emnet fra før
     * @param review     anmeldelsen som skal lagres
     * @throws IllegalArgumentException hvis serveren avviser anmeldelsen (400), innloggingen er
     *                                      ugyldig (401), brukeren mangler tilgang (403) eller
     *                                      emnet ikke finnes (404)
     * @throws UncheckedIOException     hvis serveren ikke svarer eller svarer med en annen feil
     */
    @Override
    public void addReview(String courseCode, String courseName, Review review) {
        if (courseCode == null || courseCode.isBlank() || review == null) {
            throw new IllegalArgumentException("Emnekode og anmeldelse må være oppgitt.");
        }
        ObjectNode body = objectMapper.createObjectNode();
        body.put("rating", review.getRating());
        body.put("comment", review.getComment());
        body.put("anonymous", review.isAnonymous());

        HttpRequest request = request(coursePath(courseCode) + "/reviews")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();
        checkStatus(send(request));
    }

    /**
     * Gjør ingenting. Serveren legger inn standardemnene selv når den starter.
     *
     * @param courses emnene som skulle vært lagt inn
     */
    @Override
    public void addCoursesIfMissing(List<Course> courses) {
        // Serveren eier dataene og legger inn standardemnene ved oppstart (#146).
    }

    /**
     * Lager en forespørsel til en sti på serveren, med token hvis brukeren er innlogget.
     *
     * @param path stien, for eksempel "/api/courses"
     * @return en forespørsel som kan bygges videre på
     */
    private HttpRequest.Builder request(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(requestTimeout).header("Accept", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    /**
     * Lager stien til et emne, med emnekoden kodet slik at den er trygg i en URL.
     *
     * @param courseCode emnekoden
     * @return stien, for eksempel "/api/courses/IT1901"
     */
    private static String coursePath(String courseCode) {
        String encoded = URLEncoder.encode(courseCode.strip(), StandardCharsets.UTF_8).replace("+",
                "%20");
        return "/api/courses/" + encoded;
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
     * Sjekker statuskoden i svaret. Feil fra klienten (400, 401, 403 og 404) gir meldingen fra
     * serveren, mens andre feil behandles som at serveren ikke kunne svare.
     *
     * @param response svaret fra serveren
     * @throws IllegalArgumentException hvis statuskoden er 400, 401, 403 eller 404
     * @throws UncheckedIOException     hvis statuskoden er en annen feil
     */
    private void checkStatus(HttpResponse<String> response) {
        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return;
        }
        if (status == 400 || status == 401 || status == 403 || status == 404) {
            throw new IllegalArgumentException(errorMessage(response));
        }
        throw new UncheckedIOException(
                new IOException("Serveren svarte med status " + status + "."));
    }

    /**
     * Henter feilmeldingen fra feilsvaret, eller lager en enkel melding hvis svaret ikke har en.
     *
     * @param response feilsvaret fra serveren
     * @return meldingen som kan vises til brukeren
     */
    private String errorMessage(HttpResponse<String> response) {
        try {
            JsonNode message = objectMapper.readTree(response.body()).get("message");
            if (message != null && !message.asText().isBlank()) {
                return message.asText();
            }
        } catch (IOException e) {
            // Svaret er ikke JSON, så vi bruker standardmeldingen under.
        }
        return "Serveren avviste forespørselen (status " + response.statusCode() + ").";
    }

    /**
     * Gjør om en anmeldelse fra serveren til en {@link Review}.
     *
     * @param reviewNode anmeldelsen i JSON
     * @return anmeldelsen uten eier, med visningsnavnet som navn
     */
    private static Review toReview(JsonNode reviewNode) {
        return new Review(text(reviewNode, "id"), null, text(reviewNode, "displayName"), false,
                reviewNode.path("rating").asInt(), reviewNode.path("comment").asText(""));
    }

    /**
     * Leser et tekstfelt som må finnes i svaret.
     *
     * @param node  JSON-objektet
     * @param field navnet på feltet
     * @return verdien som tekst
     * @throws UncheckedIOException hvis feltet mangler
     */
    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            throw new UncheckedIOException(
                    new IOException("Svaret fra serveren mangler feltet " + field + "."));
        }
        return value.asText();
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