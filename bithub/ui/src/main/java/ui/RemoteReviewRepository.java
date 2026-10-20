package ui;

/**
 * Henter og lagrer emner og anmeldelser ved å sende HTTP-forespørsler til REST-serveren.
 */
public final class RemoteReviewRepository {

    /**
     * Adressen til serveren når ingen annen er oppgitt.
     */
    public static final String DEFAULT_BASE_URL = "http://localhost:8080";

    private final String baseUrl;

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
}
