package core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Testklasse for Course-klassen. Verifiserer opprettelse av fag, håndtering av
 * vurderinger og beregning av gjennomsnittlig vurdering.
 */
class CourseTest {

    // Toleranse for sammenligning av desimaltall.
    private static final double DELTA = 0.0001;

    /**
     * Verifiserer at konstruktøren setter fagkode og fagnavn korrekt.
     */
    @Test
    void constructorSetsFieldsCorrectly() {
        Course course = new Course("IT1901", "Informatikk Prosjektarbeid 1");
        assertEquals("IT1901", course.getCourseCode());
        assertEquals("Informatikk Prosjektarbeid 1", course.getCourseName());
    }

    /**
     * Verifiserer at null som fagkode kaster IllegalArgumentException.
     */
    @Test
    void constructorThrowsWhenCourseCodeIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Course(null, "Informatikk Prosjektarbeid 1"));
    }

    /**
     * Verifiserer at en tom fagkode kaster IllegalArgumentException.
     */
    @Test
    void constructorThrowsWhenCourseCodeIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new Course("", "Informatikk Prosjektarbeid 1"));
    }

    /**
     * Verifiserer at en blank fagkode kaster IllegalArgumentException.
     */
    @Test
    void constructorThrowsWhenCourseCodeIsBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Course(" ", "Informatikk Prosjektarbeid 1"));
    }

    /**
     * Verifiserer at null som fagnavn kaster IllegalArgumentException.
     */
    @Test
    void constructorThrowsWhenCourseNameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Course("IT1901", null));
    }

    /**
     * Verifiserer at et tomt fagnavn kaster IllegalArgumentException.
     */
    @Test
    void constructorThrowsWhenCourseNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new Course("IT1901", ""));
    }

    /**
     * Verifiserer at et blankt fagnavn kaster IllegalArgumentException.
     */
    @Test
    void constructorThrowsWhenCourseNameIsBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Course("IT1901", " "));
    }

    /**
     * Verifiserer at et nytt fag ikke har noen vurderinger.
     */
    @Test
    void newCourseHasNoReviews() {
        Course course = new Course("IT1901", "Informatikk Prosjektarbeid 1");
        assertTrue(course.getReviews().isEmpty());
    }

    /**
     * Verifiserer at en vurdering blir lagt til i listen over vurderinger.
     */
    @Test
    void addReviewAddsToList() {
        Course course = new Course("IT1901", "Informatikk Prosjektarbeid 1");
        Review review = new Review("Ola Nordmann", 5, "Kjempebra");
        course.addReview(review);
        assertEquals(1, course.getReviews().size());
        assertEquals(review, course.getReviews().get(0));
    }

    /**
     * Verifiserer at null som vurdering kaster IllegalArgumentException.
     */
    @Test
    void addReviewThrowsWhenReviewIsNull() {
        Course course = new Course("IT1901", "Informatikk Prosjektarbeid 1");
        assertThrows(IllegalArgumentException.class, () -> course.addReview(null));
    }

    /**
     * Verifiserer at listen over vurderinger ikke kan endres direkte.
     */
    @Test
    void getReviewsReturnsUnmodifiableList() {
        Course course = new Course("IT1901", "Informatikk Prosjektarbeid 1");
        course.addReview(new Review("Ola Nordmann", 4, "Ikke dårlig"));
        assertThrows(UnsupportedOperationException.class, () -> course.getReviews().add(new Review("Ola Nordmann", 3, "Ait")));
    }

    /**
     * Verifiserer at gjennomsnittlig vurdering er 0 når faget ikke har noen
     * vurderinger.
     */
    @Test
    void averageRatingIsZeroWhenNoReviews() {
        Course course = new Course("IT1901", "Informatikk Prosjektarbeid 1");
        assertEquals(0.0, course.getAverageRating(), DELTA);
    }

    /**
     * Verifiserer at gjennomsnittlig vurdering er lik vurderingen når faget kun
     * har én vurdering.
     */
    @Test
    void averageRatingEqualsSingleReviewRating() {
        Course course = new Course("IT1901", "Informatikk Prosjektarbeid 1");
        course.addReview(new Review("Ola Nordmann", 3, "Bra fag"));
        assertEquals(3.0, course.getAverageRating(), DELTA);
    }

    /**
     * Verifiserer at gjennomsnittlig vurdering beregnes korrekt når faget har
     * flere vurderinger.
     */
    @Test
    void averageRatingIsCalculatedCorrectly() {
        Course course = new Course("IT1901", "Informatikk Prosjektarbeid 1");
        course.addReview(new Review("Ola Nordmann", 4, "Fantastisk"));
        course.addReview(new Review("William Nordmann", 2, "kommentar"));
        assertEquals(3.0, course.getAverageRating(), DELTA);
    }

    /**
     * Verifiserer at gjennomsnittlig vurdering beregnes korrekt når resultatet
     * ikke er et helt tall.
     */
    @Test
    void averageRatingHandlesNonExactDivision() {
        Course course = new Course("IT1901", "Informatikk Prosjektarbeid 1");
        course.addReview(new Review("Ola Nordmann", 5, "kommentar"));
        course.addReview(new Review("William Nordmann", 4, "kommentar"));
        course.addReview(new Review("Tias Nordmann", 4, "kommentar"));
        assertEquals(13.0 / 3.0, course.getAverageRating(), DELTA);
    }
}
