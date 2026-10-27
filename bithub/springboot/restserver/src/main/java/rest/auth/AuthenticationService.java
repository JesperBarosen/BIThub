package rest.auth;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Service;
import rest.exception.NotLoggedInException;

/**
 * Felles kontroll av innlogging for REST-endepunktene.
 * Identiteten hentes fra serverens tokenregister, ikke fra klientens brukeropplysninger.
 */
@Service
public class AuthenticationService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;

    /**
     * Oppretter kontrollen med serverens felles tokenregister.
     *
     * @param tokenService tjenesten som holder oversikt over aktive innlogginger
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "Deler tokenregisteret slik at utlogging gjelder alle endepunkter.")
    public AuthenticationService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * Krever et gyldig innloggingstoken og finner eierens e-postadresse.
     *
     * @param authorization Authorization-headeren, eller null hvis den mangler
     * @return e-postadressen registrert på tokenet på serveren
     * @throws NotLoggedInException hvis headeren eller tokenet er ugyldig eller mangler
     */
    public String requireEmail(String authorization) {
        return tokenService.findEmail(bearerToken(authorization))
                .orElseThrow(NotLoggedInException::new);
    }

    /**
     * Avslutter innloggingen som headerens token tilhører.
     *
     * @param authorization Authorization-headeren, eller null hvis den mangler
     * @throws NotLoggedInException hvis tokenet mangler, er ukjent eller allerede er fjernet
     */
    public void logout(String authorization) {
        if (!tokenService.removeToken(bearerToken(authorization))) {
            throw new NotLoggedInException();
        }
    }

    /**
     * Henter tokenet fra en Bearer-header, eller returnerer null hvis headeren mangler
     * eller bruker et annet format.
     */
    private static String bearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length()).strip();
    }
}
