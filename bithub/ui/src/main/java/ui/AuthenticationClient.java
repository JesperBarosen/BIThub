package ui;

/**
 * Grensesnitt for registrering, innlogging, passordbytte og utlogging.
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

    /**
     * Bytter passordet til den innloggede brukeren. Implementasjon som ikke
     * støtter passordbytte kaster {@link UnsupportedOperationException}.
     * 
     * @param token tokenet fra innloggingen 
     * @param email e- postadressen til brukeren som er innlogget
     * @param oldPassword det gamle passordet
     * @param newPassword det nye passordet
     * @throws IllegalArgumentException kastes hvis severen avviser passordbyttet, for eksempel
     *                                  at det gamle passordet er feil eller innlogginen har utløpt
     * @throws UnsupportedOperationException kastes hvis implementasjonen ikke støtter passordbytte 
     *                                       (standardoppførsel)
     */
    default void changePassword(String token, String email, String oldPassword, String newPassword) {
        throw new UnsupportedOperationException("Passordbytte støttes ikke.");
    }

    /**
     * Avslutter innloggingen som tokenet tilhører sånn at tokenet ikke kan brukes igjen.
     * Implementasjon som ikke støtter passordbytte kaster {@link UnsupportedOperationException}.
     * 
     * @param token tokenet fra innloggingen
     * @throws UnsupportedOperationException kastes hvis implementasjonen ikke støtter utlogging
     *                                       (standardoppførsel)
     */
    default void logout(String token) {
        throw new UnsupportedOperationException("Utlogging støttes ikke.");
    }
}
