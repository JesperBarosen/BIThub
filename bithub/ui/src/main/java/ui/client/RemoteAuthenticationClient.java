package ui.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Registrerer, logger inn, bytter passord og logger ut brukere ved hjelp av REST- serveren.
 *
 * <p>Klassen håndterer HTTP og JSON, men lagrer ikke token eller passord. Metodene skal kalles fra
 * en bakgrunnstråd, ikke fra JavaFX-tråden.
 */
public final class RemoteAuthenticationClient implements AuthenticationClient {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Oppretter klienten med standardadressen til REST-serveren.
     */
    public RemoteAuthenticationClient() {
        this(DEFAULT_BASE_URL);
    }

    /**
     * Oppretter klienten med en valgfri serveradresse.
     *
     * @param baseUrl adressen til REST-serveren
     */
    public RemoteAuthenticationClient(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Serveradressen kan ikke være tom.");
        }
        String trimmed = baseUrl.strip();
        this.baseUrl = trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
        URI uri = URI.create(this.baseUrl);
        if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null) {
            throw new IllegalArgumentException("Serveradressen må være en HTTP-adresse.");
        }
        this.httpClient = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
    }

    /**
     * Logger inn med e-post og passord.
     *
     * @param email    e-postadressen
     * @param password passordet
     * @return token, navn og e-post fra serveren
     */
    @Override
    public AuthenticationResult login(String email, String password) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("email", email);
        body.put("password", password);
        return post("/api/login", body, 200);
    }

    /**
     * Registrerer en bruker og returnerer innloggingen fra samme kall.
     *
     * @param name     brukerens navn
     * @param email    e-postadressen
     * @param password passordet
     * @return token, navn og e-post fra serveren
     */
    @Override
    public AuthenticationResult register(String name, String email, String password) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("name", name);
        body.put("email", email);
        body.put("password", password);
        return post("/api/users", body, 201);
    }

    /**
     * Bytter passord med {@code PUT /api/users/{email}/password}. Serveren sjekker at tokenet
     * tilhører brukeren og at det gamle passordet stemmer.
     * 
     * <p>Metoden returnerer bare når serveren har bekreftet byttet med {@code 204}. Alle andre svar
     * gir et unntak slik at brukeren aldri får en falsk bekreftelse.
     *
     * @param token       tokenet fra innloggingen
     * @param email       e- postadressen til brukeren som er innlogget
     * @param oldPassword det gamle passordet
     * @param newPassword det nye passordet
     * @throws IllegalArgumentException kastes hvis tokenet eller e- posten mangler, hvis serveren
     *                                      avviser passordene (400), tilgangen (403) eller
     *                                      innloggingen (401)
     * @throws UncheckedIOException     kastes hvis serveren ikke svarer eller svarer med en annen
     *                                      feil
     */
    @Override
    public void changePassword(String token, String email, String oldPassword, String newPassword) {
        requireToken(token);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E- postadressen mangler");
        }
        ObjectNode body = objectMapper.createObjectNode();
        body.put("oldPassword", oldPassword);
        body.put("newPassword", newPassword);

        String encodedEmail = URLEncoder.encode(email.strip(), StandardCharsets.UTF_8).replace("+",
                "%20");
        HttpRequest request = authorizedRequest("/api/users/" + encodedEmail + "/password", token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = send(request);
        int status = response.statusCode();
        if (status == 204) {
            return;
        }
        if (status == 400 || status == 403) {
            throw new IllegalArgumentException(errorMessage(response));
        }
        if (status == 401) {
            throw new IllegalArgumentException(
                    "Du er ikke lenger logget inn. Logg inn på nytt for å bytte passord.");
        }
        throw new UncheckedIOException(
                new IOException("Serveren svarte med status " + status + "."));
    }

    /**
     * Logger ut med {@code POST /api/logout}, slik at serveren sletter tokenet.
     * 
     * <p>Svarer serveren med {@code 401} er tokenet allerede ukjent, for ekempel fordi den startet
     * på nytt. Brukeren er da logget ut så det regnes ikke som en feil.
     *
     * @param token tokenet fra innloggingen
     * @throws IllegalArgumentException kastes hvis tokenet mangler
     * @throws UncheckedIOException     kastes hvis serveren ikke svarer eller svarer med en annen
     *                                      feil
     */
    @Override
    public void logout(String token) {
        requireToken(token);
        HttpRequest request = authorizedRequest("/api/logout", token)
                .POST(HttpRequest.BodyPublishers.noBody()).build();

        int status = send(request).statusCode();
        if (status == 204 || status == 401) {
            return;
        }
        throw new UncheckedIOException(
                new IOException("Serveren svarte med status " + status + "."));
    }

    /**
     * Lager en forespørsel som sender tokenet i {@code Authorization}-headeren.
     */
    private HttpRequest.Builder authorizedRequest(String path, String token) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path)).timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token.strip());
    }

    /**
     * Avviser kall som krever innlogging når tokenet mangler.
     */
    private static void requireToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Du må være logget inn.");
        }
    }

    /**
     * Sender en JSON-forespørsel og kontrollerer svaret.
     */
    private AuthenticationResult post(String path, ObjectNode body, int expectedStatus) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(REQUEST_TIMEOUT).header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = send(request);
        int status = response.statusCode();
        if (status == 400 || status == 409) {
            throw new IllegalArgumentException(errorMessage(response));
        }
        if (status == 401) {
            throw new IllegalArgumentException("Feil e-post eller passord.");
        }
        if (status != expectedStatus) {
            throw new UncheckedIOException(
                    new IOException("Serveren svarte med status " + status + "."));
        }
        JsonNode result = readJson(response.body());
        return new AuthenticationResult(requiredText(result, "token"), requiredText(result, "name"),
                requiredText(result, "email"));
    }

    /**
     * Utfører forespørselen og gjør nettverksfeil om til ukontrollerte IO-feil.
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
     * Leser en JSON-respons og avviser ødelagt eller manglende innhold.
     */
    private JsonNode readJson(String body) {
        try {
            JsonNode result = objectMapper.readTree(body);
            if (result == null || !result.isObject()) {
                throw new IOException("Forventet et JSON-objekt fra serveren.");
            }
            return result;
        } catch (IOException e) {
            throw new UncheckedIOException("Ugyldig svar fra serveren.", e);
        }
    }

    /**
     * Kontrollerer at et nødvendig felt inneholder tekst.
     */
    private static String requiredText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw new UncheckedIOException(
                    new IOException("Svaret fra serveren mangler gyldig " + field + "."));
        }
        return value.asText();
    }

    /**
     * Leser serverens valideringsmelding eller bruker en standardmelding.
     */
    private String errorMessage(HttpResponse<String> response) {
        try {
            JsonNode node = objectMapper.readTree(response.body());
            if (node != null) {
                JsonNode message = node.get("message");
                if (message != null && message.isTextual() && !message.asText().isBlank()) {
                    return message.asText();
                }
            }
        } catch (IOException e) {
            // Feilsvaret er ikke gyldig JSON; standardmeldingen brukes.
        }
        return "Serveren avviste forespørselen (status " + response.statusCode() + ").";
    }
}
