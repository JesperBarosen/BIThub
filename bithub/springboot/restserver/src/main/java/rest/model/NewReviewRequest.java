package rest.model;

/**
 * Opplysninger klienten sender når en anmeldelse opprettes.
 *
 * @param rating vurderingen, eller null hvis feltet mangler; valideres i core
 * @param comment kommentaren, som kan være tom eller mangle
 * @param anonymous ønsket anonymitet, brukes først når innlogging støttes
 */
public record NewReviewRequest(Integer rating, String comment, boolean anonymous) {
}
