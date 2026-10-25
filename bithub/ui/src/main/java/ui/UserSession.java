
package ui;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Holder informasjon om den innloggede brukeren mens appen er åpen.
 *
 * <p>Sesjonen lagres bare i minnet og inneholder aldri passord.
 */
public final class UserSession {

    private final Consumer<String> tokenListener;
    private volatile SessionData current;

    /**
     * Oppretter en tom sesjon som ikke varsler noen når tokenet endres.
     */
    public UserSession() {
        this(token -> {
        });
    }

    /**
     * Oppretter en tom sesjon som varsler når tokenet endres, for eksempel slik at
     * {@link RemoteReviewRepository} sender tokenet med forespørslene.
     *
     * @param tokenListener får det nye tokenet ved innlogging, og null ved utlogging
     * @throws NullPointerException hvis tokenListener er null
     */
    public UserSession(Consumer<String> tokenListener) {
        this.tokenListener = Objects.requireNonNull(tokenListener);
    }

    /**
     * Lagrer en vellykket innlogging eller registrering.
     *
     * @param token tokenet fra serveren
     * @param name  brukerens navn
     * @param email brukerens e-postadresse
     */
    public void signIn(String token, String name, String email) {
        SessionData session = new SessionData(requireText(token, "Token"),
                requireText(name, "Navn"), requireText(email, "E-post"));
        current = session;
        tokenListener.accept(session.token());
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
        tokenListener.accept(null);
    }

    /**
     * Kontrollerer at serveren har returnert en gyldig tekstverdi.
     */
    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " kan ikke være tomt.");
        }
        return value.strip();
    }

    private record SessionData(String token, String name, String email) {
    }
}