package core;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Lager salt og beregner og kontrollerer passordhasher.
 */
public final class PasswordHasher {

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
}