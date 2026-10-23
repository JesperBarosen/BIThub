
package ui;

/**
 * Holder informasjon om den innloggede brukeren mens appen er åpen.
 *
 * <p>Sesjonen lagres bare i minnet og inneholder aldri passord.
 */
public final class UserSession {

    private volatile SessionData current;

    /**
     * Lagrer en vellykket innlogging eller registrering.
     *
     * @param token tokenet fra serveren
     * @param name brukerens navn
     * @param email brukerens e-postadresse
     */
    public void signIn(String token, String name, String email) {
        current = new SessionData(
                requireText(token, "Token"),
                requireText(name, "Navn"),
                requireText(email, "E-post"));
    }

    /**
     * Sjekker om en bruker er innlogget.
     *
     * @return true hvis en innlogget bruker finnes
     */
    public boolean isLoggedIn() {
        return current != null;
    }

    /**
     * Henter tokenet som brukes i autentiserte HTTP-kall.
     *
     * @return tokenet, eller null uten innlogging
     */
    public String getToken() {
        SessionData session = current;
        return session == null ? null : session.token();
    }

    /**
     * Henter navnet til den innloggede brukeren.
     *
     * @return navnet, eller null uten innlogging
     */
    public String getName() {
        SessionData session = current;
        return session == null ? null : session.name();
    }

    /**
     * Henter e-postadressen til den innloggede brukeren.
     *
     * @return e-postadressen, eller null uten innlogging
     */
    public String getEmail() {
        SessionData session = current;
        return session == null ? null : session.email();
    }

    /**
     * Fjerner brukeropplysningene og tokenet fra minnet.
     */
    public void clear() {
        current = null;
    }

    /**
     * Kontrollerer at serveren har returnert en gyldig tekstverdi.
     */
    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " kan ikke være tomt.");
        }
        return value.strip();
    }

    private record SessionData(String token, String name, String email) {
    }
}
