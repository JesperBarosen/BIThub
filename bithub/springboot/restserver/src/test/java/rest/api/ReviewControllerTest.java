package rest.api;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import core.exception.DuplicateReviewException;
import core.model.Course;
import core.model.Review;
import core.model.User;
import core.service.ReviewService;
import core.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rest.auth.AuthenticationService;
import rest.auth.TokenService;

// Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
/**
 * Tester innsending av anmeldelser, krav om innlogging og JSON-feilsvar uten å skrive til fil.
 */
@WebMvcTest(ReviewController.class)
@Import({ ApiExceptionHandler.class, AuthenticationService.class })
class ReviewControllerTest {

    private static final String TOKEN_HEADER = "Bearer token-123";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TokenService tokenService;

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void loggedInReviewUsesServerIdentity(boolean anonymous) throws Exception {
        knownCourse();
        User owner = new User("Ola Nordmann", "ola@example.com", "Passord123");
        when(tokenService.findEmail("token-123")).thenReturn(Optional.of(owner.getEmail()));
        when(userService.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        Review saved = new Review("review-id", owner.getEmail(), owner.getName(), anonymous, 4, "");
        when(reviewService.submitReview("IT1901", "Prosjektarbeid", owner, anonymous, "4", null))
                .thenReturn(saved);

        mockMvc.perform(post("/api/courses/IT1901/reviews")
                .header("Authorization", "Bearer token-123").contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":4,\"anonymous\":" + anonymous
                        + ",\"ownerEmail\":\"kari@example.com\",\"reviewerName\":\"Kari\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.displayName").value(anonymous ? "Anonym" : "Ola Nordmann"))
                .andExpect(jsonPath("$.authorType").value(anonymous ? "ANONYMOUS" : "USER"))
                .andExpect(jsonPath("$.ownerEmail").doesNotExist())
                .andExpect(jsonPath("$.reviewerName").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(reviewService).submitReview("IT1901", "Prosjektarbeid", owner, anonymous, "4", null);
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = { "Bearer ukjent-token", "Bearer ", "token-123" })
    void invalidAuthorizationIsRejected(String authorization) throws Exception {
        mockMvc.perform(post("/api/courses/IT1901/reviews").header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content("{\"rating\":4}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(reviewService, userService);
    }

    @Test
    void tokenForMissingUserIsRejected() throws Exception {
        when(tokenService.findEmail("token-123")).thenReturn(Optional.of("missing@example.com"));
        when(userService.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(
                post("/api/courses/IT1901/reviews").header("Authorization", "Bearer token-123")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"rating\":4}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reviewService);
    }

    @Test
    void reviewWithoutTokenIsRejectedWithoutSaving() throws Exception {
        mockMvc.perform(post("/api/courses/IT1901/reviews").contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":4,\"comment\":\"Bra fag\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(reviewService, userService);
    }

    @Test
    void missingCommentIsReturnedAsEmptyText() throws Exception {
        knownCourse();
        final User owner = loggedInUser();
        when(reviewService.submitReview("IT1901", "Prosjektarbeid", owner, false, "5", null))
                .thenReturn(
                        new Review("review-id", owner.getEmail(), owner.getName(), false, 5, null));

        mockMvc.perform(post("/api/courses/IT1901/reviews").header("Authorization", TOKEN_HEADER)
                .contentType(MediaType.APPLICATION_JSON).content("{\"rating\":5}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.comment").value(""));

        verify(reviewService).submitReview("IT1901", "Prosjektarbeid", owner, false, "5", null);
    }

    /**
     * Verifiserer at en andre anmeldelse av samme emne gir 409 med meldingen fra core.
     */
    @Test
    void secondReviewOfSameCourseReturnsConflict() throws Exception {
        knownCourse();
        final User owner = loggedInUser();
        DuplicateReviewException error = new DuplicateReviewException();
        doThrow(error).when(reviewService).submitReview("IT1901", "Prosjektarbeid", owner, true,
                "4", null);

        mockMvc.perform(post("/api/courses/IT1901/reviews").header("Authorization", TOKEN_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":4,\"anonymous\":true}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(error.getMessage()));
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "6", "null" })
    void invalidRatingReturnsValidationMessage(String rating) throws Exception {
        knownCourse();
        final User owner = loggedInUser();
        String message = "Vurdering må være et tall fra 1 til 5.";
        doThrow(new IllegalArgumentException(message)).when(reviewService).submitReview("IT1901",
                "Prosjektarbeid", owner, false, rating, null);

        String body = rating.equals("null") ? "{}" : "{\"rating\":" + rating + "}";
        mockMvc.perform(post("/api/courses/IT1901/reviews").header("Authorization", TOKEN_HEADER)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(message));

        verify(reviewService).submitReview("IT1901", "Prosjektarbeid", owner, false, rating, null);
    }

    @Test
    void reviewIsSavedWithCourseCodeFromStorage() throws Exception {
        when(reviewService.findCourse("it1901"))
                .thenReturn(Optional.of(new Course("IT1901", "Prosjektarbeid")));
        final User owner = loggedInUser();
        when(reviewService.submitReview("IT1901", "Prosjektarbeid", owner, false, "3", null))
                .thenReturn(
                        new Review("review-id", owner.getEmail(), owner.getName(), false, 3, null));

        mockMvc.perform(post("/api/courses/it1901/reviews").header("Authorization", TOKEN_HEADER)
                .contentType(MediaType.APPLICATION_JSON).content("{\"rating\":3}"))
                .andExpect(status().isCreated());

        verify(reviewService).submitReview("IT1901", "Prosjektarbeid", owner, false, "3", null);
    }

    @Test
    void unknownCourseReturnsNotFoundWithoutSaving() throws Exception {
        loggedInUser();
        when(reviewService.findCourse("IT1901")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/courses/IT1901/reviews").header("Authorization", TOKEN_HEADER)
                .contentType(MediaType.APPLICATION_JSON).content("{\"rating\":4}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Fant ikke emnet IT1901."));

        verify(reviewService).findCourse("IT1901");
        verifyNoMoreInteractions(reviewService);
    }

    @ParameterizedTest
    @ValueSource(strings = { "{", "{\"rating\":\"abc\"}", "{\"rating\":4.5}", "null", "" })
    void unreadableRequestReturnsBadRequestWithoutSaving(String body) throws Exception {
        mockMvc.perform(post("/api/courses/IT1901/reviews").contentType(MediaType.APPLICATION_JSON)
                .content(body)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request")).andExpect(jsonPath("$.message")
                        .value("Ugyldig JSON eller feil datatype i forespørselen."));

        verifyNoInteractions(reviewService);
    }

    private void knownCourse() {
        when(reviewService.findCourse("IT1901"))
                .thenReturn(Optional.of(new Course("IT1901", "Prosjektarbeid")));
    }

    /**
     * Lar tokenet i {@link #TOKEN_HEADER} høre til en registrert bruker.
     *
     * @return brukeren som er logget inn
     */
    private User loggedInUser() {
        User owner = new User("Ola Nordmann", "ola@example.com", "Passord123");
        when(tokenService.findEmail("token-123")).thenReturn(Optional.of(owner.getEmail()));
        when(userService.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
        return owner;
    }
}