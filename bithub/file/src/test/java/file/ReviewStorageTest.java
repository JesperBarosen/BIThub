package file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.Review;
import core.ReviewService;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tester at domenelaget (ReviewService i core) og persistenslaget
 * (ReviewHandler i file) spiller sammen. Anmeldelsene skrives til en
 * midlertidig fil, slik at testene ikke rører den ekte courses.json.
 */
class ReviewStorageTest {

    @TempDir
    Path tempDir;

    private Path file;
    private ReviewService service;

    @BeforeEach
    void setUp() {
        file = tempDir.resolve("courses.json");
        service = new ReviewService(new ReviewHandler(file));
    }

    /**
     * Verifiserer at en anmeldelse sendt inn via ReviewService havner i
     * JSON-filen og kan leses tilbake med riktige verdier.
     */
    @Test
    void submittedReviewIsSavedToFile() {
        service.submitReview("IT1901", "Informatikk prosjektarbeid", "Anonym", "4", "Bra fag");

        List<CourseDTO> courses = new ReviewHandler(file).readFromFile();
        assertEquals(1, courses.size());
        CourseDTO course = courses.get(0);
        assertEquals("IT1901", course.getCourseCode());
        assertEquals("Informatikk prosjektarbeid", course.getCourseName());
        assertEquals(1, course.getReviews().size());

        Review review = course.getReviews().get(0);
        assertEquals("Anonym", review.getReviewerName());
        assertEquals(4, review.getRating());
        assertEquals("Bra fag", review.getComment());
    }

    /**
     * Verifiserer at anmeldelser for samme emne samles, mens anmeldelser
     * for andre emner lagres hver for seg.
     */
    @Test
    void reviewsAreGroupedByCourse() {
        service.submitReview("IT1901", "Informatikk prosjektarbeid", "Student", "5", "");
        service.submitReview("IT1901", "Informatikk prosjektarbeid", "Anonym", "3", "Mye jobb");
        service.submitReview("IT1604", "Digitalt samfunn", "Student", "4", "");

        List<CourseDTO> courses = new ReviewHandler(file).readFromFile();
        assertEquals(2, courses.size());
        assertEquals(2, findCourse(courses, "IT1901").getReviews().size());
        assertEquals(1, findCourse(courses, "IT1604").getReviews().size());
    }

    /**
     * Verifiserer at en ugyldig anmeldelse stoppes i core og aldri
     * skrives til fil.
     */
    @Test
    void invalidReviewIsNotWrittenToFile() {
        assertThrows(IllegalArgumentException.class,
            () -> service.submitReview("IT1901", "Informatikk prosjektarbeid", "Student", "9", ""));

        assertTrue(new ReviewHandler(file).isEmpty());
    }

    private static CourseDTO findCourse(List<CourseDTO> courses, String courseCode) {
        return courses.stream()
            .filter(course -> courseCode.equals(course.getCourseCode()))
            .findFirst()
            .orElseThrow();
    }
}
