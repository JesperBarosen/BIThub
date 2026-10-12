package core;

import java.security.GeneralSecurityException;
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
            throw new IllegalArgumentException("Password cannot be null or blank.");
        }
        if (salt == null) {
            throw new IllegalArgumentException("Salt cannot be null.");
        }

        byte[] saltBytes = Base64.getDecoder().decode(salt);
        if (saltBytes.length != SALT_LENGTH) {
            throw new IllegalArgumentException("Salt must contain 16 bytes.");
        }

        char[] passwordChars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(passwordChars, saltBytes, ITERATIONS, HASH_LENGTH);
        Arrays.fill(passwordChars, '\0');

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not hash password.", e);
        } finally {
            spec.clearPassword();
        }
    }
}