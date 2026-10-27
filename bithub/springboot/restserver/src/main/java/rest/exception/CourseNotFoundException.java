package rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Kastes når et emne ikke finnes. Gir svaret 404.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CourseNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Oppretter unntaket for en emnekode.
     *
     * @param courseCode emnekoden som ikke finnes
     */
    public CourseNotFoundException(String courseCode) {
        super("Fant ikke emnet " + courseCode + ".");
    }
}