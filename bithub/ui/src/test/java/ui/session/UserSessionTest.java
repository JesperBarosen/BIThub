
package ui.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tester lagring og tømming av innloggingssesjonen.
 */
class UserSessionTest {

    // Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
    @Test
    void startsWithoutLoggedInUser() {
        UserSession session = new UserSession();

        assertFalse(session.isLoggedIn());
        assertNull(session.getToken());
        assertNull(session.getName());
        assertNull(session.getEmail());
    }

    // Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
    @Test
    void storesAuthenticatedUser() {
        UserSession session = new UserSession();

        session.signIn("test-token", "Kari Nordmann", "kari@ntnu.no");

        assertTrue(session.isLoggedIn());
        assertEquals("test-token", session.getToken());
        assertEquals("Kari Nordmann", session.getName());
        assertEquals("kari@ntnu.no", session.getEmail());
    }

    // Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
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

    // Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
    @Test
    void rejectsInvalidAuthenticationData() {
        UserSession session = new UserSession();

        assertThrows(IllegalArgumentException.class,
                () -> session.signIn("", "Kari", "kari@ntnu.no"));
        assertThrows(IllegalArgumentException.class,
                () -> session.signIn("token", null, "kari@ntnu.no"));
        assertThrows(IllegalArgumentException.class, () -> session.signIn("token", "Kari", " "));

        assertFalse(session.isLoggedIn());
    }

    // Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
    @Test
    void invalidSignInPreservesExistingSession() {
        UserSession session = new UserSession();
        session.signIn("original-token", "Kari", "kari@ntnu.no");

        assertThrows(IllegalArgumentException.class,
                () -> session.signIn("", "Ola", "ola@ntnu.no"));

        assertEquals("original-token", session.getToken());
        assertEquals("Kari", session.getName());
    }

    @Test
    void tellsListenerAboutTokenOnSignInAndClear() {
        List<String> tokens = new ArrayList<>();
        UserSession session = new UserSession(tokens::add);

        session.signIn("test-token", "Kari Nordmann", "kari@ntnu.no");
        session.clear();

        assertEquals(Arrays.asList("test-token", null), tokens);
    }

    @Test
    void updateNameKeepsTokenAndEmail() {
        List<String> tokens = new ArrayList<>();
        UserSession session = new UserSession(tokens::add);
        session.signIn("test-token", "Kari Nordmann", "kari@ntnu.no");

        session.updateName(" Kari Normann ");

        assertEquals("Kari Normann", session.getName());
        assertEquals("test-token", session.getToken());
        assertEquals("kari@ntnu.no", session.getEmail());
        assertEquals(List.of("test-token"), tokens);
    }

    @Test
    void updateNameRequiresLoginAndName() {
        UserSession session = new UserSession();
        assertThrows(IllegalStateException.class, () -> session.updateName("Kari"));

        session.signIn("test-token", "Kari Nordmann", "kari@ntnu.no");
        assertThrows(IllegalArgumentException.class, () -> session.updateName(" "));
        assertEquals("Kari Nordmann", session.getName());
    }
}