package core.exception;

/**
 * Signalerer at brukeren allerede har skrevet en anmeldelse av emnet.
 */
public final class DuplicateReviewException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Oppretter unntaket med en melding om at emnet allerede er anmeldt.
     */
    public DuplicateReviewException() {
        super("Du har allerede skrevet en anmeldelse av dette emnet. "
                + "Du kan endre den under «Mine anmeldelser».");
    }
}