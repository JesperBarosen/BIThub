package core;

/**
 * Signalerer at e-postadressen allerede er registrert.
 */
public final class DuplicateEmailException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Oppretter unntaket med en melding om duplikatet.
     *
     * @param message feilmeldingen
     */
    public DuplicateEmailException(String message) {
        super(message);
    }
}