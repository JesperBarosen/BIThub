package rest.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import core.Course;
import core.Review;
import core.ReviewService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tester endepunktene for emner med en falsk tjeneste, uten fil og uten ekte server.
 */
@WebMvcTest(CourseController.class)
@Import(ApiExceptionHandler.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    /**
     * Lager et emne med to anmeldelser, slik at gjennomsnittet blir 4.5.
     *
     * @return emnet med anmeldelser
     */
    private static Course courseWithReviews() {
        Course course = new Course("IT1901", "Informatikk prosjektarbeid I");
        course.addReview(new Review("Kari", 5, "Lærerikt"));
        course.addReview(new Review("Ola", 4, ""));
        return course;
    }

    /**
     * Verifiserer at alle emner returneres med gjennomsnitt og antall anmeldelser.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void getAllCoursesReturnsCoursesWithAverageAndCount() throws Exception {
        when(reviewService.getAllCourses()).thenReturn(List.of(courseWithReviews(),
                new Course("TDT4100", "Objektorientert programmering")));

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("IT1901"))
                .andExpect(jsonPath("$[0].name").value("Informatikk prosjektarbeid I"))
                .andExpect(jsonPath("$[0].averageRating").value(4.5))
                .andExpect(jsonPath("$[0].reviewCount").value(2))
                .andExpect(jsonPath("$[1].code").value("TDT4100"))
                .andExpect(jsonPath("$[1].averageRating").value(0.0))
                .andExpect(jsonPath("$[1].reviewCount").value(0));
    }

    /**
     * Verifiserer at en tom lagring gir en tom liste, ikke en feil.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void getAllCoursesReturnsEmptyListWhenNoCourses() throws Exception {
        when(reviewService.getAllCourses()).thenReturn(List.of());

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * Verifiserer at ett emne returneres med anmeldelser, og at anmeldelsene vises som gjest uten
     * navnet til den som skrev dem.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void getCourseReturnsCourseWithGuestReviews() throws Exception {
        when(reviewService.findCourse("IT1901")).thenReturn(Optional.of(courseWithReviews()));

        mockMvc.perform(get("/api/courses/IT1901"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("IT1901"))
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.reviews.length()").value(2))
                .andExpect(jsonPath("$.reviews[0].displayName").value("Gjest"))
                .andExpect(jsonPath("$.reviews[0].authorType").value("GUEST"))
                .andExpect(jsonPath("$.reviews[0].rating").value(5))
                .andExpect(jsonPath("$.reviews[0].comment").value("Lærerikt"))
                .andExpect(jsonPath("$.reviews[0].reviewerName").doesNotExist());
    }

    /**
     * Verifiserer at et ukjent emne gir 404.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void getCourseReturnsNotFoundForUnknownCourse() throws Exception {
        when(reviewService.findCourse("TDT4100")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/courses/TDT4100"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Fant ikke emnet TDT4100."));
    }

    /**
     * Verifiserer at valideringsfeil fra core gir 400 med den opprinnelige meldingen.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void getCourseReturnsBadRequestForInvalidCode() throws Exception {
        when(reviewService.findCourse("ugyldig"))
                .thenThrow(new IllegalArgumentException("Ugyldig emnekode."));

        mockMvc.perform(get("/api/courses/ugyldig"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Ugyldig emnekode."));
    }

    /**
     * Verifiserer at anmeldelser med eier vises med navn eller som anonyme, med id og uten e-post.
     *
     * @throws Exception hvis forespørselen feiler
     */
    @Test
    void getCourseShowsUserAndAnonymousReviews() throws Exception {
        Course course = new Course("IT1901", "Informatikk prosjektarbeid I");
        course.addReview(new Review("id-1", "kari@ntnu.no", "Kari", false, 5, "Lærerikt"));
        course.addReview(new Review("id-2", "ola@ntnu.no", "Ola", true, 4, ""));
        when(reviewService.findCourse("IT1901")).thenReturn(Optional.of(course));

        mockMvc.perform(get("/api/courses/IT1901"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews[0].id").value("id-1"))
                .andExpect(jsonPath("$.reviews[0].displayName").value("Kari"))
                .andExpect(jsonPath("$.reviews[0].authorType").value("USER"))
                .andExpect(jsonPath("$.reviews[1].id").value("id-2"))
                .andExpect(jsonPath("$.reviews[1].displayName").value("Anonym"))
                .andExpect(jsonPath("$.reviews[1].authorType").value("ANONYMOUS"))
                .andExpect(jsonPath("$.reviews[0].ownerEmail").doesNotExist());
    }
}