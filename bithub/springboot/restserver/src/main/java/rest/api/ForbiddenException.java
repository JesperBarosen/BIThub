package rest.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Kastes når en innlogget bruker prøver å endre noe som tilhører en annen.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Oppretter feilen uten å avsløre opplysninger om den andre brukeren.
     */
    public ForbiddenException() {
        super("Du har ikke tilgang til å gjøre denne endringen.");
    }
}
