package core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CourseTest {

    private static final double DELTA = 0.0001;

    @Test 
    void ConstructorSetsFieldsCorrectly(){ 
        Course course = new Course("IT1901", "Informatikk Prosjektarbeid 1");
        assertEquals("IT1901", course.getCourseCode());
        assertEquals("Informatikk Prosjektarbeid 1", course.getCourseName());
    }

    @Test 
    void constructorThrowsWhenCourseCodeIsNull(){ 
        assertThrows(IllegalArgumentException.class,() -> new Course(null, "Informatikk Prosjektarbeid 1"));
    }

    @Test 
    void constructorThrowsWhenCourseCodeIsBlank(){ 
        assertThrows(IllegalArgumentException.class,() -> new Course("", "Informatikk Prosjektarbeid 1"));
    }

    @Test 
    void constructorThrowsWhenCourseNameIsNull(){ 
        assertThrows(IllegalArgumentException.class,() -> new Course("IT1901", null));
    }

    @Test 
    void constructorThrowsWhenCourseNameIsBlank(){ 
        assertThrows(IllegalArgumentException.class,() -> new Course("IT1901", ""));
    }

    @Test 
    void newCourseHasNoReviews(){ 
        Course course = new Course("IT1901", "Informatikk ProsjektArbeid 1");
        assertTrue(course.getReviews().isEmpty());
    }

    @Test 
    void addReviewsAddsToList(){ 
        Course course = new Course("IT1901", "Informatikk ProsjektArbeid 1");
        Review review = new Review("Ola Nordmann", 5, "Kjempebra");
        course.addReview(review);
        assertEquals(1, course.getReviews().size());
        assertEquals(review, course.getReviews().get(0));
    }

    @Test 
    void addReviewThrowsWhenReviewIsNull(){ 
        Course course = new Course("IT1901", "Informatikk Prosjektarbeid 1");
        assertThrows(IllegalArgumentException.class, () -> course.addReview(null));
    }

    @Test 
    void getReviewsReturnsImmutableList(){ 
        Course course = new Course("IT1901", "Informatikk Prosjektarbeid 1");
        course.addReview(new Review("Ola Nordmann", 4, "Ikke dårlig"));
        assertThrows(UnsupportedOperationException.class, () -> course.getReviews().add(new Review("Ola Nordmann", 3, "Ait")));
    }

    @Test 
    void averageRatingIsZeroWhenNoReviews(){ 
        Course course = new Course("IT1901", "Informatikk Prosjektarbeid 1");
        assertEquals(0.0, course.getAverageRating(), DELTA);
    }

    @Test 
    void averageRatingWithSingleReviewEqualsIsThatRating(){ 
        Course course = new Course("IT1901", "Informatikk Prosjektarbeid 1");
        course.addReview(new Review("Ola Nordmann", 3, "Bra fag"));
        assertEquals(3.0, course.getAverageRating(),DELTA);
    }

    @Test 
    void averageRatingIsCalculatedCorrectly(){ 
        Course course = new Course("IT1901", "Informatikk Prosjektarbeid 1");
        course.addReview(new Review("Ola Nordmann", 4, "Fantastisk"));
        course.addReview(new Review("William Nordmann", 2, "kommentar"));
        assertEquals(3.0, course.getAverageRating(),DELTA);
    }

    @Test 
    void averageRatingHandlesNonExactDivision(){ 
        Course course = new Course("IT1901", "Informatikk Prosjektarbeid 1");
        course.addReview(new Review("Ola Nordmann", 5, "kommentar"));
        course.addReview(new Review("William Nordmann", 4, "kommentar"));
        course.addReview(new Review("Tias Nordmann", 4, "kommentar"));
        assertEquals(13.0/3.0, course.getAverageRating(),DELTA);
    }








    




    




   






}
