package rest.api;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import core.Course;
import core.Review;
import core.ReviewService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tester innsending av anmeldelser og JSON-feilsvar uten å skrive til fil.
 */
@WebMvcTest(ReviewController.class)
@Import(ApiExceptionHandler.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    private void knownCourse() {
        when(reviewService.findCourse("IT1901"))
                .thenReturn(Optional.of(new Course("IT1901", "Prosjektarbeid")));
    }

    @Test
    void validReviewIsSubmittedAsGuest() throws Exception {
        knownCourse();
        Review saved = new Review("Gjest", 4, "Bra fag");
        when(reviewService.submitReview("IT1901", "Prosjektarbeid", "Gjest", "4", "  Bra fag  "))
                .thenReturn(saved);

        mockMvc.perform(post("/api/courses/IT1901/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":4,\"comment\":\"  Bra fag  \",\"anonymous\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.displayName").value("Gjest"))
                .andExpect(jsonPath("$.authorType").value("GUEST"))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Bra fag"));

        verify(reviewService).submitReview("IT1901", "Prosjektarbeid", "Gjest", "4", "  Bra fag  ");
    }

    @Test
    void missingCommentIsReturnedAsEmptyText() throws Exception {
        knownCourse();
        when(reviewService.submitReview("IT1901", "Prosjektarbeid", "Gjest", "5", null))
                .thenReturn(new Review("Gjest", 5, null));

        mockMvc.perform(post("/api/courses/IT1901/reviews")
                .contentType(MediaType.APPLICATION_JSON).content("{\"rating\":5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comment").value(""));

        verify(reviewService).submitReview("IT1901", "Prosjektarbeid", "Gjest", "5", null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "6", "null"})
    void invalidRatingReturnsValidationMessage(String rating) throws Exception {
        knownCourse();
        String message = "Vurdering må være et tall fra 1 til 5.";
        doThrow(new IllegalArgumentException(message)).when(reviewService)
                .submitReview("IT1901", "Prosjektarbeid", "Gjest", rating, null);

        String body = rating.equals("null") ? "{}" : "{\"rating\":" + rating + "}";
        mockMvc.perform(post("/api/courses/IT1901/reviews")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(message));

        verify(reviewService).submitReview("IT1901", "Prosjektarbeid", "Gjest", rating, null);
    }

    @Test
    void reviewIsSavedWithCourseCodeFromStorage() throws Exception {
        when(reviewService.findCourse("it1901"))
                .thenReturn(Optional.of(new Course("IT1901", "Prosjektarbeid")));
        when(reviewService.submitReview("IT1901", "Prosjektarbeid", "Gjest", "3", null))
                .thenReturn(new Review("Gjest", 3, null));

        mockMvc.perform(post("/api/courses/it1901/reviews")
                .contentType(MediaType.APPLICATION_JSON).content("{\"rating\":3}"))
                .andExpect(status().isCreated());

        verify(reviewService).submitReview("IT1901", "Prosjektarbeid", "Gjest", "3", null);
    }

    @Test
    void unknownCourseReturnsNotFoundWithoutSaving() throws Exception {
        when(reviewService.findCourse("IT1901")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/courses/IT1901/reviews")
                .contentType(MediaType.APPLICATION_JSON).content("{\"rating\":4}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Fant ikke emnet IT1901."));

        verify(reviewService).findCourse("IT1901");
        verifyNoMoreInteractions(reviewService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "{\"rating\":\"abc\"}", "{\"rating\":4.5}", "null", ""})
    void unreadableRequestReturnsBadRequestWithoutSaving(String body) throws Exception {
        mockMvc.perform(post("/api/courses/IT1901/reviews")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Ugyldig JSON eller feil datatype i forespørselen."));

        verifyNoInteractions(reviewService);
    }
}