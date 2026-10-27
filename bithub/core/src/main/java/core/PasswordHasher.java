package core;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Lager salt og beregner og kontrollerer passordhasher.
 */
public final class PasswordHasher {

    private static final int ITERATIONS = 600_000;
    private static final int HASH_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    /**
     * Kontrollerer et passord mot en lagret hash.
     *
     * @param password     passordet som skal kontrolleres
     * @param salt         saltet som ble brukt til den lagrede hashen
     * @param expectedHash den lagrede hashen kodet som Base64
     * @return true hvis passordet stemmer, ellers false
     * @throws IllegalArgumentException hvis passord, salt eller hash er ugyldig
     * @throws IllegalStateException    hvis hashing ikke kan utføres
     */
    public static boolean verifyPassword(String password, String salt, String expectedHash) {
        if (expectedHash == null) {
            throw new IllegalArgumentException("Hash kan ikke være null.");
        }

        byte[] expectedBytes = Base64.getDecoder().decode(expectedHash);
        if (expectedBytes.length != HASH_LENGTH / Byte.SIZE) {
            throw new IllegalArgumentException("Hash må inneholde 32 byte.");
        }

        String actualHash = hashPassword(password, salt);
        byte[] actualBytes = Base64.getDecoder().decode(actualHash);

        return MessageDigest.isEqual(expectedBytes, actualBytes);
    }

    /**
     * Lager et tilfeldig salt for hashing av et passord.
     *
     * @return et salt på 16 byte kodet som Base64
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Beregner en passordhash med det oppgitte saltet.
     *
     * @param password passordet som skal hashes
     * @param salt     et salt på 16 byte kodet som Base64
     * @return passordhashen kodet som Base64
     * @throws IllegalArgumentException hvis passordet er null eller blankt, eller saltet er ugyldig
     * @throws IllegalStateException    hvis hashing ikke kan utføres
     */
    public static String hashPassword(String password, String salt) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Passordet må fylles ut.");
        }
        if (salt == null) {
            throw new IllegalArgumentException("Salt kan ikke være null.");
        }

        byte[] saltBytes = Base64.getDecoder().decode(salt);
        if (saltBytes.length != SALT_LENGTH) {
            throw new IllegalArgumentException("Salt må inneholde 16 byte.");
        }

        char[] passwordChars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(passwordChars, saltBytes, ITERATIONS, HASH_LENGTH);
        Arrays.fill(passwordChars, '\0');

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Kunne ikke hashe passord.", e);
        } finally {
            spec.clearPassword();
        }
    }
}
