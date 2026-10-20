package rest.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rest.model.ErrorResponse;

/**
 * Gjør ugyldige forespørsler og manglende emner om til JSON-feilsvar.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Returnerer valideringsmeldingen fra core til klienten.
     *
     * @param exception valideringsfeilen
     * @return feilsvar med status 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidArgument(IllegalArgumentException exception) {
        return errorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    /**
     * Gir en forståelig melding når JSON ikke kan leses som forventet.
     *
     * @return feilsvar med status 400
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage() {
        return errorResponse(HttpStatus.BAD_REQUEST,
                "Ugyldig JSON eller feil datatype i forespørselen.");
    }

    /**
     * Forklarer hvilket emne som ikke finnes.
     *
     * @param exception feilen med emnekoden
     * @return feilsvar med status 404
     */
    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCourseNotFound(CourseNotFoundException exception) {
        return errorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    private static ResponseEntity<ErrorResponse> errorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), status.getReasonPhrase(), message));
    }
}
