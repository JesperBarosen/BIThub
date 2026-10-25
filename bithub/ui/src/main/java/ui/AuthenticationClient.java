package ui;

/**
 * Grensesnitt for registrering og innlogging.
 *
 * <p>Implementasjonen bestemmer hvordan serveren kontaktes. Kontrollerne skal ikke kjenne til HTTP
 * eller JSON.
 */
public interface AuthenticationClient {

    /**
     * Logger inn en eksisterende bruker.
     *
     * @param email    brukerens e-postadresse
     * @param password brukerens passord
     * @return token og brukeropplysninger fra serveren
     */
    AuthenticationResult login(String email, String password);

    /**
     * Registrerer en ny bruker og logger vedkommende inn.
     *
     * @param name     brukerens navn
     * @param email    brukerens e-postadresse
     * @param password ønsket passord
     * @return token og brukeropplysninger fra serveren
     */
    AuthenticationResult register(String name, String email, String password);
}
