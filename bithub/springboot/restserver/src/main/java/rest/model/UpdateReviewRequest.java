package rest.model;

/**
 * Opplysninger klienten sender når en egen anmeldelse endres.
 *
 * @param rating den nye vurderingen, eller null hvis feltet mangler
 * @param comment den nye kommentaren, som kan være tom eller mangle
 * @param anonymous om anmeldelsen skal være anonym
 */
public record UpdateReviewRequest(Integer rating, String comment, boolean anonymous) {
}