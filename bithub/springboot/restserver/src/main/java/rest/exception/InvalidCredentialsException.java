package rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Kastes når e-post eller passord er feil ved innlogging. Gir svaret 401 uten å
 * si hvilken av dem som var feil.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Oppretter unntaket med en felles melding for feil e-post og feil passord.
     */
    public InvalidCredentialsException() {
        super("Feil e-post eller passord.");
    }
}
