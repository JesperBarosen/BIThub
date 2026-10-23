package rest.auth;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tester at TokenService lager tokener og finner brukeren som eier dem.
 */
class TokenServiceTest {

    private final TokenService tokenService = new TokenService();

    /**
     * Verifiserer at et nytt token hører til brukeren det ble laget for.
     */
    @Test
    void createTokenLinksTokenToEmail() {
        String token = tokenService.createToken("ola@example.com");

        assertEquals(Optional.of("ola@example.com"), tokenService.findEmail(token));
    }

    /**
     * Verifiserer at hvert kall gir et nytt token, og at alle disse tokenene
     * til samme bruker virker samtidig, for eksempel når brukeren logger inn
     * fra flere enheter.
     */
    @Test
    void createTokenReturnsNewTokenEachTime() {
        String first = tokenService.createToken("ola@example.com");
        String second = tokenService.createToken("ola@example.com");

        assertAll(
                () -> assertNotEquals(first, second),
                () -> assertEquals(Optional.of("ola@example.com"), tokenService.findEmail(first)),
                () -> assertEquals(Optional.of("ola@example.com"), tokenService.findEmail(second)));
    }

    /**
     * Verifiserer at tokener til ulike brukere ikke blandes sammen.
     */
    @Test
    void createTokenKeepsUsersApart() {
        String olaToken = tokenService.createToken("ola@example.com");
        String kariToken = tokenService.createToken("kari@example.com");

        assertAll(
                () -> assertEquals(Optional.of("ola@example.com"),
                        tokenService.findEmail(olaToken)),
                () -> assertEquals(Optional.of("kari@example.com"),
                        tokenService.findEmail(kariToken)));
    }

    /**
     * Verifiserer at manglende eller ukjente tokener gir tomt resultat.
     *
     * @param token tokenet som slås opp
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "ukjent-token" })
    void findEmailReturnsEmptyForUnknownToken(String token) {
        tokenService.createToken("ola@example.com");

        assertTrue(tokenService.findEmail(token).isEmpty());
    }

    /**
     * Verifiserer at det ikke kan lages token uten e-post.
     */
    @Test
    void createTokenRejectsNullEmail() {
        assertThrows(NullPointerException.class, () -> tokenService.createToken(null));
    }

    /**
     * Verifiserer at et fjernet token ikke kan brukes eller logges ut en gang til.
     */
    @Test
    void removeTokenEndsSession() {
        String token = tokenService.createToken("ola@example.com");

        assertTrue(tokenService.removeToken(token));
        assertTrue(tokenService.findEmail(token).isEmpty());
        assertFalse(tokenService.removeToken(token));
    }

    /**
     * Verifiserer at andre innlogginger beholdes, også for samme bruker.
     */
    @Test
    void removeTokenKeepsOtherSessions() {
        String token = tokenService.createToken("ola@example.com");
        String otherOlaToken = tokenService.createToken("ola@example.com");
        String kariToken = tokenService.createToken("kari@example.com");

        tokenService.removeToken(token);

        assertEquals(Optional.of("ola@example.com"), tokenService.findEmail(otherOlaToken));
        assertEquals(Optional.of("kari@example.com"), tokenService.findEmail(kariToken));
    }

    /**
     * Verifiserer at manglende og ukjente tokener ikke regnes som vellykket utlogging.
     *
     * @param token det ugyldige tokenet
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "ukjent-token" })
    void removeTokenReturnsFalseForUnknownToken(String token) {
        String existingToken = tokenService.createToken("ola@example.com");

        assertFalse(tokenService.removeToken(token));
        assertEquals(Optional.of("ola@example.com"), tokenService.findEmail(existingToken));
    }
}
