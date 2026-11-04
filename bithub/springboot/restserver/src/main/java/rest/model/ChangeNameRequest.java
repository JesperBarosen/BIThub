package rest.model;

/**
 * Navnet klienten sender for å endre navnet til brukeren.
 *
 * @param name det nye navnet
 */
public record ChangeNameRequest(String name) {
}