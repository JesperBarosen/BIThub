package core.exception;

/**
 * Signalerer at en bruker prøver å endre eller slette en anmeldelse som ikke er brukerens egen.
 * Gjelder også gjesteanmeldelser, som ikke har noen eier.
 */
public final class NotReviewOwnerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Oppretter unntaket uten å avsløre hvem som eier anmeldelsen.
     */
    public NotReviewOwnerException() {
        super("Du har ikke tilgang til å gjøre denne endringen.");
    }
}