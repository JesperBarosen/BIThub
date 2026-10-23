package rest.model;

/**
 * Opplysninger klienten sender når en ny bruker registreres.
 *
 * @param name navnet til brukeren; valideres i core
 * @param email e-postadressen til brukeren; valideres i core
 * @param password passordet til brukeren; lagres bare som hash
 */
public record RegisterRequest(String name, String email, String password) {
}
