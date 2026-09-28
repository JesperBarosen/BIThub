package core;

import core.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Enhetstester for {@link User}-klassen.
 * 
 * Verifiserer at valideringslogikken for navn, e-post og passord fungerer som forventet,
 * samt at tilstandshenting via gettere returnerer korrekte verdier.
 */
class UserTest {

    /**
     * Verifiserer at en opprettelse med gyldige feltverdier lykkes,
     * og at gettere returnerer de innlagte verdiene.
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
        assertEquals(password, user.getPassword());
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
     * Verifiserer at ugyldige navn (null, blank, for kort, for lang, ugyldige tegn) kaster IllegalArgumentException.
     *
     * @param invalidName Ugyldig navnestreng som skal testes
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "A", "AB", "DetteNavnetErAltforLangtOgOverstiger30Tegn", "Ola123", "Kari!"})
    @DisplayName("Skal kaste unntak for ugyldige navn")
    void testInvalidNames(String invalidName) {
        assertThrows(IllegalArgumentException.class, () -> 
            new User(invalidName, "test@example.com", "password123")
        );
    }

    /**
     * Verifiserer at null som navn kaster IllegalArgumentException.
     */
    @Test
    @DisplayName("Skal kaste unntak når navn er null")
    void testNullName() {
        assertThrows(IllegalArgumentException.class, () -> 
            new User(null, "test@example.com", "password123")
        );
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
        assertThrows(IllegalArgumentException.class, () -> 
            new User("Ola Nordmann", null, "password123")
        );
    }

    /**
     * Verifiserer at ugyldige e-postformater kaster IllegalArgumentException.
     *
     * @param invalidEmail Ugyldig e-poststreng som skal testes
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "uten-at-tegn", "mangler-domene@", "@mangler-bruker.no", "test@domain"})
    @DisplayName("Skal kaste unntak for ugyldig e-postformat")
    void testInvalidEmail(String invalidEmail) {
        assertThrows(IllegalArgumentException.class, () -> 
            new User("Ola Nordmann", invalidEmail, "password123")
        );
    }

    /**
     * Verifiserer at passord på akkurat 8 tegn godkjennes (grenseverdi).
     */
    @Test
    @DisplayName("Skal godkjenne passord på akkurat 8 tegn")
    void testBoundaryPasswordLength() {
        String validPassword = "12345678"; // 8 tegn
        User user = new User("Ola Nordmann", "test@example.com", validPassword);
        assertEquals(validPassword, user.getPassword());
    }

    /**
     * Verifiserer at null som passord kaster IllegalArgumentException.
     */
    @Test
    @DisplayName("Skal kaste unntak når passord er null")
    void testNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> 
            new User("Ola Nordmann", "test@example.com", null)
        );
    }

    /**
     * Verifiserer at for korte eller tomme passord kaster IllegalArgumentException.
     *
     * @param invalidPassword Ugyldig passordstreng som skal testes
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "1234567"})
    @DisplayName("Skal kaste unntak for for kort eller tomt passord")
    void testInvalidPassword(String invalidPassword) {
        assertThrows(IllegalArgumentException.class, () -> 
            new User("Ola Nordmann", "test@example.com", invalidPassword)
        );
    }
}