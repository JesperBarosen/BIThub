package ui.client;

/**
 * Resultatet av vellykket registrering eller innlogging.
 *
 * @param token tokenet fra serveren
 * @param name brukerens navn
 * @param email brukerens e-postadresse
 */
public record AuthenticationResult(String token, String name, String email) {

    /**
     * Skjuler tokenet hvis resultatet skrives til en logg.
     *
     * @return en beskrivelse uten token
     */
    @Override
    public String toString() {
        return "AuthenticationResult[name=" + name + ", email=" + email + "]";
    }
}
