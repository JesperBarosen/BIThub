package core.exception;

/**
 * Signalerer at det ikke finnes noen anmeldelse med den oppgitte id-en.
 */
public final class ReviewNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Oppretter unntaket for en id som ikke finnes.
     *
     * @param reviewId id-en som ble etterspurt
     */
    public ReviewNotFoundException(String reviewId) {
        super("Fant ingen anmeldelse med id " + reviewId + ".");
    }
}