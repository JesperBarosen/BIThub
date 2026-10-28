package rest.model;

import core.model.User;

/**
 * Svar etter registrering eller innlogging. Inneholder tokenet klienten skal sende videre, men
 * aldri passord, passordhash eller salt.
 *
 * @param token tokenet som identifiserer brukeren i senere forespørsler
 * @param name navnet til brukeren
 * @param email e-postadressen til brukeren
 */
public record AuthResponse(String token, String name, String email) {

    /**
     * Lager svaret for en innlogget bruker.
     *
     * @param user brukeren fra core
     * @param token tokenet som ble laget for brukeren
     * @return svaret med token, navn og e-post
     */
    public static AuthResponse from(User user, String token) {
        return new AuthResponse(token, user.getName(), user.getEmail());
    }
}
