package rest.model;

/**
 * Opplysninger klienten sender for å logge inn.
 *
 * @param email e-postadressen til brukeren
 * @param password passordet i klartekst
 */
public record LoginRequest(String email, String password) {
}