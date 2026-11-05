package file.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tester innlesing av emnedata fra JSON.
 */
class CourseDtoTest {

    /**
     * Sjekker at null i JSON blir en tom liste som nye anmeldelser kan legges til i.
     *
     * @throws IOException hvis testdataene ikke kan leses
     */
    @Test
    void nullReviewsInJsonBecomeEmptyList() throws IOException {
        String json = """
                {"courseCode":"IT1901","courseName":"Prosjektarbeid","reviews":null}
                """;

        CourseDTO course = new ObjectMapper().readValue(json, CourseDTO.class);

        assertTrue(course.getReviews().isEmpty());

        ReviewDTO review = new ReviewDTO("Ola", 4, "Bra fag");
        course.getReviews().add(review);
        assertEquals(List.of(review), course.getReviews());
    }
}
