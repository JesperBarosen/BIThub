package ui.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Sjekker standardoppførselen når en autentiseringsklient ikke støtter alle handlingene.
 */
class AuthenticationClientTest {

    private final AuthenticationClient client = new AuthenticationClient() {
        @Override
        public AuthenticationResult login(String email, String password) {
            throw new AssertionError("Innlogging skal ikke kalles i denne testen.");
        }

        @Override
        public AuthenticationResult register(String name, String email, String password) {
            throw new AssertionError("Registrering skal ikke kalles i denne testen.");
        }
    };

    /**
     * Avviser passordbytte når klienten ikke har implementert handlingen.
     */
    @Test
    void changePasswordIsUnsupportedByDefault() {
        UnsupportedOperationException error = assertThrows(UnsupportedOperationException.class,
                () -> client.changePassword("token-1", "ola@example.com", "gammelt", "nytt"));

        assertEquals("Passordbytte støttes ikke.", error.getMessage());
    }

    /**
     * Avviser navnebytte når klienten ikke har implementert handlingen.
     */
    @Test
    void changeNameIsUnsupportedByDefault() {
        UnsupportedOperationException error = assertThrows(UnsupportedOperationException.class,
                () -> client.changeName("token-1", "ola@example.com", "Nytt navn"));

        assertEquals("Navnebytte støttes ikke.", error.getMessage());
    }

    /**
     * Avviser utlogging når klienten ikke har implementert handlingen.
     */
    @Test
    void logoutIsUnsupportedByDefault() {
        UnsupportedOperationException error = assertThrows(UnsupportedOperationException.class,
                () -> client.logout("token-1"));

        assertEquals("Utlogging støttes ikke.", error.getMessage());
    }
}
