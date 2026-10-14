package core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Enhetstester for {@link User}-klassen.
 *
 * <p>Verifiserer at valideringslogikken for navn, e-post og passord fungerer som forventet, samt at
 * tilstandshenting via gettere returnerer korrekte verdier.
 */
class UserTest {

    /**
     * Verifiserer at en opprettelse med gyldige feltverdier lykkes, og at gettere returnerer de
     * innlagte verdiene.
     */
    @Test
    @DisplayName("Skal opprette User ved gyldige feltverdier")
    void testValidUserCreation() {
        String name = "Ola Nordmann";
        String email = "ola.nordmann@stud.ntnu.no";
        String password = "sikkerPassord123";

        User user = new User(name, email, password);

        assertNotNull(user);
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertNotEquals(password, user.getPasswordHash());
        assertNotNull(user.getSalt());
        assertTrue(user.verifyPassword(password));
    }

    /**
     * Verifiserer at navnelengder på akkurat 3 og 30 tegn godkjennes (grenseverdier).
     */
    @Test
    @DisplayName("Skal godkjenne navn på akkurat minste og største tillatte lengde")
    void testBoundaryNameLength() {
        String minName = "Ola"; // 3 tegn
        String maxName = "A".repeat(30); // 30 tegn

        User userMin = new User(minName, "test@example.com", "password123");
        User userMax = new User(maxName, "test@example.com", "password123");

        assertEquals(minName, userMin.getName());
        assertEquals(maxName, userMax.getName());
    }

    /**
     * Verifiserer at ugyldige navn (null, blank, for kort, for lang, ugyldige tegn) kaster
     * IllegalArgumentException.
     *
     * @param invalidName Ugyldig navnestreng som skal testes
     */
    @ParameterizedTest
    @ValueSource(strings = { "", "   ", "A", "AB", "DetteNavnetErAltforLangtOgOverstiger30Tegn",
        "Ola123", "Kari!" })
    @DisplayName("Skal kaste unntak for ugyldige navn")
    void testInvalidNames(String invalidName) {
        assertThrows(IllegalArgumentException.class,
                () -> new User(invalidName, "test@example.com", "password123"));
    }

    /**
     * Verifiserer at null som navn kaster IllegalArgumentException.
     */
    @Test
    @DisplayName("Skal kaste unntak når navn er null")
    void testNullName() {
        assertThrows(IllegalArgumentException.class,
                () -> new User(null, "test@example.com", "password123"));
    }

    /**
     * Verifiserer at navneformater med bindestrek og apostrof godkjennes.
     */
    @Test
    @DisplayName("Skal godkjenne navn med bindestrek og apostrof")
    void testNameWithSpecialCharacters() {
        String specialName = "Anne-Lise O'Connor";
        User user = new User(specialName, "test@example.com", "password123");
        assertEquals(specialName, user.getName());
    }

    /**
     * Verifiserer at null som e-post kaster IllegalArgumentException.
     */
    @Test
    @DisplayName("Skal kaste unntak når e-post er null")
    void testNullEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("Ola Nordmann", null, "password123"));
    }

    /**
     * Verifiserer at ugyldige e-postformater kaster IllegalArgumentException.
     *
     * @param invalidEmail Ugyldig e-poststreng som skal testes
     */
    @ParameterizedTest
    @ValueSource(strings = { "", "   ", "uten-at-tegn", "mangler-domene@", "@mangler-bruker.no",
        "test@domain" })
    @DisplayName("Skal kaste unntak for ugyldig e-postformat")
    void testInvalidEmail(String invalidEmail) {
        assertThrows(IllegalArgumentException.class,
                () -> new User("Ola Nordmann", invalidEmail, "password123"));
    }

    /**
     * Verifiserer at passord på akkurat 8 tegn godkjennes (grenseverdi).
     */
    @Test
    @DisplayName("Skal godkjenne passord på akkurat 8 tegn")
    void testBoundaryPasswordLength() {
        String validPassword = "12345678"; // 8 tegn
        User user = new User("Ola Nordmann", "test@example.com", validPassword);
        assertTrue(user.verifyPassword(validPassword));
    }

    /**
     * Verifiserer at null som passord kaster IllegalArgumentException.
     */
    @Test
    @DisplayName("Skal kaste unntak når passord er null")
    void testNullPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("Ola Nordmann", "test@example.com", null));
    }

    /**
     * Verifiserer at for korte eller tomme passord kaster IllegalArgumentException.
     *
     * @param invalidPassword Ugyldig passordstreng som skal testes
     */
    @ParameterizedTest
    @ValueSource(strings = { "", "   ", "1234567" })
    @DisplayName("Skal kaste unntak for for kort eller tomt passord")
    void testInvalidPassword(String invalidPassword) {
        assertThrows(IllegalArgumentException.class,
                () -> new User("Ola Nordmann", "test@example.com", invalidPassword));
    }

    @Test
    @DisplayName("Skal bevare lagret hash og salt ved opprettelse")
    void testCreateUserFromStoredHash() {
        User original = new User("Ola Nordmann", "ola@example.com", "passord123");

        User restored = new User(original.getName(), original.getEmail(),
                original.getPasswordHash(), original.getSalt());

        assertEquals(original.getName(), restored.getName());
        assertEquals(original.getEmail(), restored.getEmail());
        assertEquals(original.getPasswordHash(), restored.getPasswordHash());
        assertEquals(original.getSalt(), restored.getSalt());
        assertTrue(restored.verifyPassword("passord123"));
        assertFalse(restored.verifyPassword("feilPassord"));
    }

    @Test
    @DisplayName("Skal avvise feil passord")
    void testVerifyPasswordRejectsWrongPassword() {
        User user = new User("Ola Nordmann", "ola@example.com", "passord123");

        assertFalse(user.verifyPassword("feilPassord"));
    }

    @Test
    @DisplayName("Skal gi brukere med samme passord ulike salt og hasher")
    void testSamePasswordProducesDifferentHashes() {
        User first = new User("Ola Nordmann", "ola@example.com", "passord123");
        User second = new User("Kari Nordmann", "kari@example.com", "passord123");

        assertNotEquals(first.getSalt(), second.getSalt());
        assertNotEquals(first.getPasswordHash(), second.getPasswordHash());
        assertTrue(first.verifyPassword("passord123"));
        assertTrue(second.verifyPassword("passord123"));
    }

    @Test
    @DisplayName("Skal avvise ugyldige lagrede passordhasher")
    void testStoredUserRejectsInvalidHashes() {
        String salt = PasswordHasher.generateSalt();

        assertThrows(IllegalArgumentException.class,
                () -> new User("Ola Nordmann", "ola@example.com", null, salt));

        String[] invalidHashes = { "", "ikke!base64",
            Base64.getEncoder().encodeToString(new byte[31]),
            Base64.getEncoder().encodeToString(new byte[33]) };

        for (String hash : invalidHashes) {
            assertThrows(IllegalArgumentException.class,
                    () -> new User("Ola Nordmann", "ola@example.com", hash, salt));
        }
    }

    @Test
    @DisplayName("Skal avvise ugyldige lagrede salt")
    void testStoredUserRejectsInvalidSalts() {
        String hash = Base64.getEncoder().encodeToString(new byte[32]);

        assertThrows(IllegalArgumentException.class,
                () -> new User("Ola Nordmann", "ola@example.com", hash, null));

        String[] invalidSalts = { "", "ikke!base64",
            Base64.getEncoder().encodeToString(new byte[15]),
            Base64.getEncoder().encodeToString(new byte[17]) };

        for (String salt : invalidSalts) {
            assertThrows(IllegalArgumentException.class,
                    () -> new User("Ola Nordmann", "ola@example.com", hash, salt));
        }
    }

    @Test
    @DisplayName("Skal validere navn og e-post ved opprettelse fra lagrede data")
    void testStoredUserRejectsInvalidIdentity() {
        String hash = Base64.getEncoder().encodeToString(new byte[32]);
        String salt = PasswordHasher.generateSalt();

        assertThrows(IllegalArgumentException.class,
                () -> new User(null, "ola@example.com", hash, salt));
        assertThrows(IllegalArgumentException.class,
                () -> new User("Ola123", "ola@example.com", hash, salt));
        assertThrows(IllegalArgumentException.class,
                () -> new User("Ola Nordmann", null, hash, salt));
        assertThrows(IllegalArgumentException.class,
                () -> new User("Ola Nordmann", "ugyldig", hash, salt));
    }
}
