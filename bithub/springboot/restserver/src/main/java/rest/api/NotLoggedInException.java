package rest.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Kastes når en forespørsel som krever innlogging mangler token, eller tokenet er ukjent. Gir
 * svaret 401.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class NotLoggedInException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Oppretter unntaket med en felles melding for manglende og ukjent token.
     */
    public NotLoggedInException() {
        super("Du må være logget inn.");
    }
}
