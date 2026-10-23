package rest.model;

/**
 * Opplysninger klienten sender for å bytte passord.
 *
 * @param oldPassword det nåværende passordet
 * @param newPassword det nye passordet
 */
public record ChangePasswordRequest(String oldPassword, String newPassword) {
}
