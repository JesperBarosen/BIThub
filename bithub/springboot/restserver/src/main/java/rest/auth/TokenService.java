package rest.auth;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Lager innloggingstokener og husker hvilken bruker hvert token tilhører.
 *
 * <p>Tokenene lagres bare i minnet, så alle må logge inn på nytt når serveren
 * starter på nytt. Kartet er trådsikkert, fordi serveren behandler mange
 * forespørsler samtidig.
 */
@Service
public class TokenService {

    private final Map<String, String> emailsByToken = new ConcurrentHashMap<>();

    /**
     * Lager et nytt tilfeldig token for brukeren med den gitte e-posten.
     *
     * @param email e-posten til brukeren som logges inn
     * @return det nye tokenet
     * @throws NullPointerException hvis e-posten er {@code null}
     */
    public String createToken(String email) {
        Objects.requireNonNull(email, "email");
        String token = UUID.randomUUID().toString();
        emailsByToken.put(token, email);
        return token;
    }

    /**
     * Finner e-posten til brukeren som eier tokenet.
     *
     * @param token tokenet fra Authorization-headeren
     * @return e-posten, eller tom hvis tokenet mangler eller er ukjent
     */
    public Optional<String> findEmail(String token) {
        if (token == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(emailsByToken.get(token));
    }

    /**
     * Fjerner ett innloggingstoken uten å påvirke andre innlogginger.
     *
     * @param token tokenet som skal avsluttes
     * @return true hvis tokenet ble fjernet, ellers false hvis det mangler eller er ukjent
     */
    public boolean removeToken(String token) {
        return token != null && emailsByToken.remove(token) != null;
    }
}
