package rest.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import rest.api.NotLoggedInException;

/**
 * Tester kontrollen med et ekte tokenregister, inkludert utlogging og ugyldige headere.
 */
class AuthenticationServiceTest {

    private final TokenService tokenService = new TokenService();
    private final AuthenticationService authenticationService =
            new AuthenticationService(tokenService);

    @Test
    void requireEmailFindsEachTokensOwner() {
        String ola = tokenService.createToken("ola@example.com");
        String kari = tokenService.createToken("kari@example.com");

        assertEquals("ola@example.com", authenticationService.requireEmail("Bearer " + ola));
        assertEquals("kari@example.com", authenticationService.requireEmail("Bearer " + kari));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "Bearer ", "Bearer ukjent-token", "Basic ukjent-token"})
    void requireEmailRejectsInvalidAuthorization(String authorization) {
        assertThrows(NotLoggedInException.class,
                () -> authenticationService.requireEmail(authorization));
    }

    @Test
    void tokenWithoutBearerPrefixIsRejected() {
        String token = tokenService.createToken("ola@example.com");

        assertThrows(NotLoggedInException.class, () -> authenticationService.requireEmail(token));
        assertThrows(NotLoggedInException.class, () -> authenticationService.logout(token));
        assertEquals("ola@example.com", authenticationService.requireEmail("Bearer " + token));
    }

    @Test
    void logoutRevokesOnlyTheSelectedSession() {
        String first = "Bearer " + tokenService.createToken("ola@example.com");
        final String second = "Bearer " + tokenService.createToken("ola@example.com");
        final String otherUser = "Bearer " + tokenService.createToken("kari@example.com");

        authenticationService.logout(first);

        assertThrows(NotLoggedInException.class, () -> authenticationService.requireEmail(first));
        assertThrows(NotLoggedInException.class, () -> authenticationService.logout(first));
        assertEquals("ola@example.com", authenticationService.requireEmail(second));
        assertEquals("kari@example.com", authenticationService.requireEmail(otherUser));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "Bearer ", "Bearer ukjent-token", "Basic ukjent-token"})
    void logoutRejectsInvalidAuthorization(String authorization) {
        assertThrows(NotLoggedInException.class,
                () -> authenticationService.logout(authorization));
    }
}
