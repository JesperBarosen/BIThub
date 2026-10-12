package core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}