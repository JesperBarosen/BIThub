package core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
}