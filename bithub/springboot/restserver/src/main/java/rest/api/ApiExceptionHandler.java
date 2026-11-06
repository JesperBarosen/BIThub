package rest.api;

import core.exception.DuplicateEmailException;
import core.exception.DuplicateReviewException;
import core.exception.NotReviewOwnerException;
import core.exception.ReviewNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rest.exception.CourseNotFoundException;
import rest.exception.ForbiddenException;
import rest.exception.InvalidCredentialsException;
import rest.exception.NotLoggedInException;
import rest.model.ErrorResponse;

/**
 * Gjør feil fra core og kontrollerne om til JSON-feilsvar med felles format.
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

    /**
     * Forklarer hvilken anmeldelse som ikke finnes.
     *
     * @param exception feilen med id-en til anmeldelsen
     * @return feilsvar med status 404
     */
    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReviewNotFound(ReviewNotFoundException exception) {
        return errorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    /**
     * Forteller at e-postadressen allerede er registrert.
     *
     * @param exception feilen fra core
     * @return feilsvar med status 409
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException exception) {
        return errorResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    /**
     * Forteller at brukeren allerede har skrevet en anmeldelse av emnet.
     *
     * @param exception feilen fra core
     * @return feilsvar med status 409
     */
    @ExceptionHandler(DuplicateReviewException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateReview(DuplicateReviewException exception) {
        return errorResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    /**
     * Avviser innlogging uten å si om det var e-posten eller passordet som var feil.
     *
     * @param exception feilen ved innlogging
     * @return feilsvar med status 401
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException exception) {
        return errorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    /**
     * Avviser forespørsler som krever innlogging når tokenet mangler eller er ukjent.
     *
     * @param exception feilen ved manglende innlogging
     * @return feilsvar med status 401
     */
    @ExceptionHandler(NotLoggedInException.class)
    public ResponseEntity<ErrorResponse> handleNotLoggedIn(NotLoggedInException exception) {
        return errorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    /**
     * Avviser endringer en innlogget bruker ikke har tilgang til.
     *
     * @param exception feilen ved manglende tilgang
     * @return feilsvar med status 403
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException exception) {
        return errorResponse(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    /**
     * Avviser endring eller sletting av andres anmeldelser og gjesteanmeldelser.
     *
     * @param exception feilen fra core
     * @return feilsvar med status 403
     */
    @ExceptionHandler(NotReviewOwnerException.class)
    public ResponseEntity<ErrorResponse> handleNotReviewOwner(NotReviewOwnerException exception) {
        return errorResponse(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    /**
     * Samler statuskode, HTTP-statusnavn og norsk feilforklaring i det felles JSON-formatet.
     */
    private static ResponseEntity<ErrorResponse> errorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), status.getReasonPhrase(), message));
    }
}