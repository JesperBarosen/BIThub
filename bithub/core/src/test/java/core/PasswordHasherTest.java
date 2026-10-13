package core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import org.junit.jupiter.api.Test;

/**
 * Tester generering av salt og håndtering av passordhasher.
 */
class PasswordHasherTest {

    @Test
    void testGenerateSaltHasCorrectLength() {
        String salt = PasswordHasher.generateSalt();

        byte[] decodedSalt = Base64.getDecoder().decode(salt);

        assertEquals(16, decodedSalt.length);
    }

    @Test
    void testGenerateSaltProducesDifferentValues() {
        String firstSalt = PasswordHasher.generateSalt();
        String secondSalt = PasswordHasher.generateSalt();

        assertNotEquals(firstSalt, secondSalt);
    }

    @Test
    void testHashPasswordIsRepeatable() {
        String salt = PasswordHasher.generateSalt();

        String firstHash = PasswordHasher.hashPassword("passord123", salt);
        String secondHash = PasswordHasher.hashPassword("passord123", salt);

        assertEquals(firstHash, secondHash);
    }

    @Test
    void testHashPasswordChangesWithSalt() {
        String firstSalt = PasswordHasher.generateSalt();
        String secondSalt = PasswordHasher.generateSalt();

        String firstHash = PasswordHasher.hashPassword("passord123", firstSalt);
        String secondHash = PasswordHasher.hashPassword("passord123", secondSalt);

        assertNotEquals(firstHash, secondHash);
    }

    @Test
    void testHashPasswordChangesWithPassword() {
        String salt = PasswordHasher.generateSalt();

        String firstHash = PasswordHasher.hashPassword("passord123", salt);
        String secondHash = PasswordHasher.hashPassword("annetPassord456", salt);

        assertNotEquals(firstHash, secondHash);
    }

    @Test
    void testHashPasswordHasCorrectLength() {
        String salt = PasswordHasher.generateSalt();

        String hash = PasswordHasher.hashPassword("passord123", salt);

        assertEquals(32, Base64.getDecoder().decode(hash).length);
    }

    @Test
    void testHashPasswordRejectsInvalidPasswords() {
        String salt = PasswordHasher.generateSalt();

        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hashPassword(null, salt));
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hashPassword("", salt));
        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.hashPassword("   ", salt));
    }

    @Test
    void testHashPasswordRejectsInvalidSalts() {
        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.hashPassword("passord123", null));
        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.hashPassword("passord123", "ikke!base64"));
        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.hashPassword("passord123", ""));

        String shortSalt = Base64.getEncoder().encodeToString(new byte[15]);
        String longSalt = Base64.getEncoder().encodeToString(new byte[17]);

        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.hashPassword("passord123", shortSalt));
        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.hashPassword("passord123", longSalt));
    }

    @Test
    void testVerifyPasswordAcceptsCorrectPassword() {
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword("passord123", salt);

        assertTrue(PasswordHasher.verifyPassword("passord123", salt, hash));
    }

    @Test
    void testVerifyPasswordRejectsWrongPassword() {
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword("passord123", salt);

        assertFalse(PasswordHasher.verifyPassword("feilPassord", salt, hash));
    }

    @Test
    void testVerifyPasswordRejectsDifferentSalt() {
        String salt = PasswordHasher.generateSalt();
        String otherSalt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword("passord123", salt);

        assertFalse(PasswordHasher.verifyPassword("passord123", otherSalt, hash));
    }

    @Test
    void testVerifyPasswordRejectsInvalidHashes() {
        String salt = PasswordHasher.generateSalt();

        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.verifyPassword("passord123", salt, null));
        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.verifyPassword("passord123", salt, ""));
        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.verifyPassword("passord123", salt, "ikke!base64"));

        String shortHash = Base64.getEncoder().encodeToString(new byte[31]);
        String longHash = Base64.getEncoder().encodeToString(new byte[33]);

        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.verifyPassword("passord123", salt, shortHash));
        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.verifyPassword("passord123", salt, longHash));
    }

    @Test
    void testVerifyPasswordRejectsInvalidPasswords() {
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword("passord123", salt);

        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.verifyPassword(null, salt, hash));
        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.verifyPassword("", salt, hash));
        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.verifyPassword("   ", salt, hash));
    }

    @Test
    void testVerifyPasswordRejectsInvalidSalts() {
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword("passord123", salt);

        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.verifyPassword("passord123", null, hash));
        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.verifyPassword("passord123", "ikke!base64", hash));
        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.verifyPassword("passord123", "", hash));
    }
}