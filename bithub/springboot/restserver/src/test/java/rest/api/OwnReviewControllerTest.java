package rest.api;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import core.exception.NotReviewOwnerException;
import core.exception.ReviewNotFoundException;
import core.model.Review;
import core.service.ReviewService;
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

/**
 * Tester endring og sletting av egne anmeldelser med falske tjenester, uten fil og uten ekte
 * server.
 */
@WebMvcTest(OwnReviewController.class)
@Import({ApiExceptionHandler.class, AuthenticationService.class})
class OwnReviewControllerTest {

    private static final String EMAIL = "ola@example.com";

    private static final String AUTHORIZATION = "Bearer token-123";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private TokenService tokenService;

    private void loggedIn() {
        when(tokenService.findEmail("token-123")).thenReturn(Optional.of(EMAIL));
    }

    /**
     * Verifiserer at endringen bruker e-posten fra tokenet, ikke en eier klienten sender med, og
     * at svaret ikke viser eieren.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void updateUsesServerIdentityAndReturnsUpdatedReview() throws Exception {
        loggedIn();
        Review updated = new Review("review-id", EMAIL, "Ola Nordmann", false, 5, "Bedre nå");
        when(reviewService.updateReview("review-id", EMAIL, "5", "Bedre nå", false))
                .thenReturn(updated);

        mockMvc.perform(put("/api/reviews/review-id")
                .header("Authorization", AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":5,\"comment\":\"Bedre nå\",\"anonymous\":false,"
                        + "\"ownerEmail\":\"kari@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("review-id"))
                .andExpect(jsonPath("$.displayName").value("Ola Nordmann"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Bedre nå"))
                .andExpect(jsonPath("$.ownerEmail").doesNotExist());

        verify(reviewService).updateReview("review-id", EMAIL, "5", "Bedre nå", false);
    }

    /**
     * Verifiserer at sletting av egen anmeldelse gir 204 uten innhold.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void deleteOwnReviewReturnsNoContent() throws Exception {
        loggedIn();

        mockMvc.perform(delete("/api/reviews/review-id")
                .header("Authorization", AUTHORIZATION))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(reviewService).deleteReview("review-id", EMAIL);
    }

    /**
     * Verifiserer at tom, ukjent eller feilformatert innlogging gir 401 uten at noe endres.
     *
     * @param authorization headeren klienten sender
     * @throws Exception hvis forespørselen feiler
     */
    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"Bearer ukjent-token", "token-123"})
    void updateWithoutValidLoginIsRejected(String authorization) throws Exception {
        mockMvc.perform(put("/api/reviews/review-id")
                .header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON).content("{\"rating\":4}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(reviewService);
    }

    /**
     * Verifiserer at sletting uten Authorization-header gir 401 uten at noe slettes.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void deleteWithoutHeaderIsRejected() throws Exception {
        mockMvc.perform(delete("/api/reviews/review-id"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(reviewService);
    }

    /**
     * Verifiserer at en anmeldelse som tilhører en annen, eller er en gjesteanmeldelse, gir 403.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void deleteOtherUsersReviewIsForbidden() throws Exception {
        loggedIn();
        doThrow(new NotReviewOwnerException()).when(reviewService)
                .deleteReview("review-id", EMAIL);

        mockMvc.perform(delete("/api/reviews/review-id")
                .header("Authorization", AUTHORIZATION))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message")
                        .value("Du har ikke tilgang til å gjøre denne endringen."));
    }

    /**
     * Verifiserer at en ukjent anmeldelse gir 404.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void updateUnknownReviewReturnsNotFound() throws Exception {
        loggedIn();
        when(reviewService.updateReview("ukjent-id", EMAIL, "4", null, false))
                .thenThrow(new ReviewNotFoundException("ukjent-id"));

        mockMvc.perform(put("/api/reviews/ukjent-id")
                .header("Authorization", AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON).content("{\"rating\":4}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    /**
     * Verifiserer at en ugyldig eller manglende vurdering gir 400 med meldingen fra core.
     *
     * @param rating vurderingen slik kontrolleren sender den videre
     * @throws Exception hvis forespørselen feiler
     */
    @ParameterizedTest
    @ValueSource(strings = {"0", "6", "null"})
    void invalidRatingReturnsBadRequest(String rating) throws Exception {
        loggedIn();
        String message = "Vurdering må være et tall fra 1 til 5.";
        when(reviewService.updateReview("review-id", EMAIL, rating, null, false))
                .thenThrow(new IllegalArgumentException(message));

        String body = rating.equals("null") ? "{}" : "{\"rating\":" + rating + "}";
        mockMvc.perform(put("/api/reviews/review-id")
                .header("Authorization", AUTHORIZATION)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(message));
    }
}