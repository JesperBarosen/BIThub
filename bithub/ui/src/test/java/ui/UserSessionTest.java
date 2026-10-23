
package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tester lagring og tømming av innloggingssesjonen.
 */
class UserSessionTest {

    @Test
    void startsWithoutLoggedInUser() {
        UserSession session = new UserSession();

        assertFalse(session.isLoggedIn());
        assertNull(session.getToken());
        assertNull(session.getName());
        assertNull(session.getEmail());
    }

    @Test
    void storesAuthenticatedUser() {
        UserSession session = new UserSession();

        session.signIn("test-token", "Kari Nordmann", "kari@ntnu.no");

        assertTrue(session.isLoggedIn());
        assertEquals("test-token", session.getToken());
        assertEquals("Kari Nordmann", session.getName());
        assertEquals("kari@ntnu.no", session.getEmail());
    }

    @Test
    void clearsAuthenticatedUser() {
        UserSession session = new UserSession();
        session.signIn("test-token", "Kari Nordmann", "kari@ntnu.no");

        session.clear();

        assertFalse(session.isLoggedIn());
        assertNull(session.getToken());
        assertNull(session.getName());
        assertNull(session.getEmail());
    }

    @Test
    void rejectsInvalidAuthenticationData() {
        UserSession session = new UserSession();

        assertThrows(IllegalArgumentException.class,
                () -> session.signIn("", "Kari", "kari@ntnu.no"));
        assertThrows(IllegalArgumentException.class,
                () -> session.signIn("token", null, "kari@ntnu.no"));
        assertThrows(IllegalArgumentException.class,
                () -> session.signIn("token", "Kari", " "));

        assertFalse(session.isLoggedIn());
    }

    @Test
    void invalidSignInPreservesExistingSession() {
        UserSession session = new UserSession();
        session.signIn("original-token", "Kari", "kari@ntnu.no");

        assertThrows(IllegalArgumentException.class,
                () -> session.signIn("", "Ola", "ola@ntnu.no"));

        assertEquals("original-token", session.getToken());
        assertEquals("Kari", session.getName());
    }
}
