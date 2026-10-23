package rest.api;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import core.Course;
import core.DuplicateEmailException;
import core.Review;
import core.ReviewService;
import core.User;
import core.UserService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rest.auth.AuthenticationService;
import rest.auth.TokenService;

/**
 * Tester registrering, innlogging, passordbytte og egne anmeldelser med falske tjenester,
 * uten fil og uten ekte server.
 */
@WebMvcTest(UserController.class)
@Import({ApiExceptionHandler.class, AuthenticationService.class})
class UserControllerTest {

    private static final String REGISTER_BODY = "{\"name\":\"Ola Nordmann\","
            + "\"email\":\"Ola@example.com\",\"password\":\"Passord123\"}";

    private static final String LOGIN_BODY =
            "{\"email\":\"Ola@example.com\",\"password\":\"Passord123\"}";

    private static final String CHANGE_PASSWORD_BODY =
            "{\"oldPassword\":\"Passord123\",\"newPassword\":\"NyttPassord123\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private TokenService tokenService;

    /**
     * Lager brukeren slik UserService returnerer den, med normalisert e-post.
     *
     * @return brukeren
     */
    private static User ola() {
        return new User("Ola Nordmann", "ola@example.com", "Passord123");
    }

    /**
     * Verifiserer at registrering gir 201 med token, navn og e-post, uten
     * passorddata, og at tokenet lages for den normaliserte e-posten fra core.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void registerReturnsCreatedWithToken() throws Exception {
        when(userService.register("Ola Nordmann", "Ola@example.com", "Passord123"))
                .thenReturn(ola());
        when(tokenService.createToken("ola@example.com")).thenReturn("token-123");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON).content(REGISTER_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.name").value("Ola Nordmann"))
                .andExpect(jsonPath("$.email").value("ola@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.salt").doesNotExist());

        verify(tokenService).createToken("ola@example.com");
    }

    /**
     * Verifiserer at en registrert e-post gir 409 med meldingen fra core, uten at
     * det lages token.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void registerReturnsConflictForDuplicateEmail() throws Exception {
        when(userService.register("Ola Nordmann", "Ola@example.com", "Passord123"))
                .thenThrow(new DuplicateEmailException("E-postadressen er allerede registrert."));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON).content(REGISTER_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("E-postadressen er allerede registrert."));

        verifyNoInteractions(tokenService);
    }

    /**
     * Verifiserer at valideringsfeil fra core gir 400 med den opprinnelige
     * meldingen, uten at det lages token.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void registerReturnsBadRequestForInvalidData() throws Exception {
        when(userService.register("Ola Nordmann", "Ola@example.com", "Passord123"))
                .thenThrow(new IllegalArgumentException("Ugyldig navn"));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON).content(REGISTER_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Ugyldig navn"));

        verifyNoInteractions(tokenService);
    }

    /**
     * Verifiserer at innlogging med riktig e-post og passord gir 200 med token,
     * navn og e-post.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void loginReturnsTokenForCorrectPassword() throws Exception {
        when(userService.login("Ola@example.com", "Passord123")).thenReturn(Optional.of(ola()));
        when(tokenService.createToken("ola@example.com")).thenReturn("token-456");

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-456"))
                .andExpect(jsonPath("$.name").value("Ola Nordmann"))
                .andExpect(jsonPath("$.email").value("ola@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(tokenService).createToken("ola@example.com");
    }

    /**
     * Verifiserer at feil e-post eller passord gir 401 med en felles melding, uten at det
     * lages token.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void loginReturnsUnauthorizedForWrongCredentials() throws Exception {
        when(userService.login("Ola@example.com", "Passord123")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Feil e-post eller passord."));

        verifyNoInteractions(tokenService);
    }

    /**
     * Verifiserer at utlogging fjerner tokenet og gir et tomt svar.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void logoutReturnsNoContent() throws Exception {
        when(tokenService.removeToken("token-123")).thenReturn(true);

        mockMvc.perform(post("/api/logout").header("Authorization", "Bearer token-123"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(tokenService).removeToken("token-123");
        verifyNoInteractions(userService, reviewService);
    }

    /**
     * Verifiserer at utlogging uten gyldig token gir JSON-feilsvar med 401.
     *
     * @param authorization manglende eller ugyldig Authorization-header
     * @throws Exception hvis forespørselen feiler
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "Bearer ukjent-token", "token-123", "Bearer " })
    void logoutRejectsInvalidToken(String authorization) throws Exception {
        var request = post("/api/logout");
        if (authorization != null) {
            request.header("Authorization", authorization);
        }

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Du må være logget inn."));

        verifyNoInteractions(userService, reviewService);
    }

    /**
     * Verifiserer at eget passord kan byttes, og at core får identiteten fra tokenet.
     *
     * @param email e-post i stien, med ulike store og små bokstaver
     * @throws Exception hvis forespørselen feiler
     */
    @ParameterizedTest
    @ValueSource(strings = {"ola@example.com", "OLA@EXAMPLE.COM"})
    void changePasswordReturnsNoContent(String email) throws Exception {
        when(tokenService.findEmail("token-123")).thenReturn(Optional.of("ola@example.com"));

        mockMvc.perform(put("/api/users/{email}/password", email)
                .header("Authorization", "Bearer token-123")
                .contentType(MediaType.APPLICATION_JSON).content(CHANGE_PASSWORD_BODY))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(userService).changePassword("ola@example.com", "Passord123", "NyttPassord123");
    }

    /**
     * Verifiserer at feil gammelt passord gir 400 med meldingen fra core.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void changePasswordReturnsBadRequestForWrongOldPassword() throws Exception {
        when(tokenService.findEmail("token-123")).thenReturn(Optional.of("ola@example.com"));
        doThrow(new IllegalArgumentException("Det gamle passordet er feil.")).when(userService)
                .changePassword("ola@example.com", "Passord123", "NyttPassord123");

        mockMvc.perform(put("/api/users/ola@example.com/password")
                .header("Authorization", "Bearer token-123")
                .contentType(MediaType.APPLICATION_JSON).content(CHANGE_PASSWORD_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Det gamle passordet er feil."));
    }

    @Test
    void changePasswordRejectsAnotherUsersEmail() throws Exception {
        when(tokenService.findEmail("token-123")).thenReturn(Optional.of("ola@example.com"));

        mockMvc.perform(put("/api/users/kari@example.com/password")
                .header("Authorization", "Bearer token-123")
                .contentType(MediaType.APPLICATION_JSON).content(CHANGE_PASSWORD_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message")
                        .value("Du har ikke tilgang til å gjøre denne endringen."));

        verifyNoInteractions(userService, reviewService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Bearer ukjent-token", "token-123", "Bearer "})
    void changePasswordRequiresValidToken(String authorization) throws Exception {
        var request = put("/api/users/ola@example.com/password")
                .contentType(MediaType.APPLICATION_JSON).content(CHANGE_PASSWORD_BODY);
        if (authorization != null) {
            request.header("Authorization", authorization);
        }

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        verifyNoInteractions(userService, reviewService);
    }

    /**
     * Verifiserer at innlogget bruker får egne anmeldelser med emnet de hører til og om de er
     * anonyme, men uten e-posten til eieren.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void getOwnReviewsReturnsOwnReviewsWithCourse() throws Exception {
        Course course = new Course("IT1901", "Informatikk prosjektarbeid I");
        course.addReview(
                new Review("id-1", "ola@example.com", "Ola Nordmann", true, 4, "Mye arbeid"));
        when(tokenService.findEmail("token-123")).thenReturn(Optional.of("ola@example.com"));
        when(reviewService.findReviewsByOwner("ola@example.com")).thenReturn(List.of(course));

        mockMvc.perform(get("/api/me/reviews").header("Authorization", "Bearer token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("id-1"))
                .andExpect(jsonPath("$[0].courseCode").value("IT1901"))
                .andExpect(jsonPath("$[0].courseName").value("Informatikk prosjektarbeid I"))
                .andExpect(jsonPath("$[0].rating").value(4))
                .andExpect(jsonPath("$[0].comment").value("Mye arbeid"))
                .andExpect(jsonPath("$[0].anonymous").value(true))
                .andExpect(jsonPath("$[0].ownerEmail").doesNotExist());
    }

    /**
     * Verifiserer at en forespørsel uten token gir 401 uten at anmeldelser hentes.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void getOwnReviewsReturnsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/me/reviews"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Du må være logget inn."));

        verifyNoInteractions(reviewService);
    }

    /**
     * Verifiserer at et ukjent eller utlogget token gir 401 uten at anmeldelser hentes.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void getOwnReviewsReturnsUnauthorizedForUnknownToken() throws Exception {
        when(tokenService.findEmail("ukjent-token")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/me/reviews").header("Authorization", "Bearer ukjent-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Du må være logget inn."));

        verifyNoInteractions(reviewService);
    }

    /**
     * Verifiserer at tokenet må sendes med «Bearer » foran, slik rest-api.md beskriver.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void getOwnReviewsRequiresBearerPrefix() throws Exception {
        when(tokenService.findEmail("token-123")).thenReturn(Optional.of("ola@example.com"));

        mockMvc.perform(get("/api/me/reviews").header("Authorization", "token-123"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reviewService);
    }
}
