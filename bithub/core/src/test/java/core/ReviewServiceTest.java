package core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Testklasse for ReviewService. Bruker en falsk lagring i minnet i stedet for ReviewHandler, slik
 * at core kan testes uten fil og uten brukergrensesnitt.
 */
class ReviewServiceTest {

    private final List<String> savedCourses = new ArrayList<>();
    private final List<Review> savedReviews = new ArrayList<>();

    /**
     * Emnet den falske lagringen returnerer ved oppslag, eller null for ingen treff.
     */
    private Course storedCourse;

    /**
     * Falsk lagring som bare husker hva den har fått, og som returnerer {@link #storedCourse} ved
     * oppslag.
     */
    private final ReviewRepository fakeRepository = new ReviewRepository() {
        @Override
        public void addReview(String courseCode, String courseName, Review review) {
            savedCourses.add(courseCode + " - " + courseName);
            savedReviews.add(review);
        }

        @Override
        public Optional<Course> findCourse(String courseCode) {
            return Optional.ofNullable(storedCourse)
                    .filter(course -> course.getCourseCode().equals(courseCode));
        }

        @Override
        public List<Course> findAllCourses() {
            return storedCourse == null ? List.of() : List.of(storedCourse);
        }

        @Override
        public void addCoursesIfMissing(List<Course> courses) {
        }
    };

    private final ReviewService service = new ReviewService(fakeRepository);

    /**
     * Verifiserer at en gyldig anmeldelse sendes videre til lagringen med riktig emne og riktige
     * verdier.
     */
    @Test
    void submitReviewSavesValidReview() {
        service.submitReview("IT1901", "Informatikk prosjektarbeid", "Anonym", "4", "Bra fag");

        assertEquals(List.of("IT1901 - Informatikk prosjektarbeid"), savedCourses);
        assertEquals(1, savedReviews.size());
        Review review = savedReviews.get(0);
        assertEquals("Anonym", review.getReviewerName());
        assertEquals(4, review.getRating());
        assertEquals("Bra fag", review.getComment());
    }

    /**
     * Verifiserer at mellomrom rundt vurdering og kommentar fjernes, og at manglende kommentar blir
     * en tom streng.
     */
    @Test
    void submitReviewTrimsInput() {
        service.submitReview("IT1901", "Prosjektarbeid", "Student", " 5 ", "  Lærerikt  ");
        service.submitReview("IT1901", "Prosjektarbeid", "Student", "3", null);

        assertEquals(5, savedReviews.get(0).getRating());
        assertEquals("Lærerikt", savedReviews.get(0).getComment());
        assertEquals("", savedReviews.get(1).getComment());
    }

    /**
     * Verifiserer at tom vurdering avvises med riktig melding og ikke lagres.
     */
    @Test
    void submitReviewRejectsMissingRating() {
        for (String rating : new String[] { null, "", "   " }) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> service.submitReview("IT1901", "Prosjektarbeid", "Student", rating, ""));
            assertEquals("Vurdering må fylles ut.", e.getMessage());
        }
        assertTrue(savedReviews.isEmpty());
    }

    /**
     * Verifiserer at vurdering som ikke er et tall avvises og ikke lagres.
     */
    @Test
    void submitReviewRejectsNonNumericRating() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.submitReview("IT1901", "Prosjektarbeid", "Student", "fire", ""));
        assertEquals("Vurdering må være et gyldig tall.", e.getMessage());
        assertTrue(savedReviews.isEmpty());
    }

    /**
     * Verifiserer at vurderinger utenfor 1 til 5 avvises og ikke lagres.
     */
    @Test
    void submitReviewRejectsRatingOutsideRange() {
        for (String rating : new String[] { "0", "6", "-1" }) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> service.submitReview("IT1901", "Prosjektarbeid", "Student", rating, ""));
            assertEquals("Vurdering må være et tall fra 1 til 5.", e.getMessage());
        }
        assertTrue(savedReviews.isEmpty());
    }

    /**
     * Verifiserer at manglende emne eller forfatter avvises og ikke lagres.
     */
    @Test
    void submitReviewRejectsMissingCourseOrReviewer() {
        assertThrows(IllegalArgumentException.class,
                () -> service.submitReview("", "Prosjektarbeid", "Student", "4", ""));

        assertThrows(IllegalArgumentException.class,
                () -> service.submitReview("IT1901", "", "Student", "4", ""));

        assertThrows(IllegalArgumentException.class,
                () -> service.submitReview("IT1901", "Prosjektarbeid", "", "4", ""));
        assertTrue(savedReviews.isEmpty());
    }

    /**
     * Verifiserer at feil fra lagringen sendes videre til den som kaller, slik at
     * brukergrensesnittet kan vise en feilmelding.
     */
    @Test
    void submitReviewPassesOnStorageFailure() {
        RuntimeException failure = new RuntimeException("Lagring feilet");
        ReviewService failingService = new ReviewService(new ReviewRepository() {
            @Override
            public void addReview(String courseCode, String courseName, Review review) {
                throw failure;
            }

            @Override
            public Optional<Course> findCourse(String courseCode) {
                return Optional.empty();
            }

            @Override
            public List<Course> findAllCourses() {
                return List.of();
            }

            @Override
            public void addCoursesIfMissing(List<Course> courses) {
            }
        });

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> failingService.submitReview("IT1901", "Prosjektarbeid", "Student", "4", ""));
        assertSame(failure, actual);
    }

    /**
     * Verifiserer at et lagret emne hentes med alle anmeldelsene.
     */
    @Test
    void getCourseWithReviewsReturnsStoredCourse() {
        storedCourse = new Course("IT1901", "Prosjektarbeid");
        storedCourse.addReview(new Review("Ola", 4, "Bra"));
        storedCourse.addReview(new Review("Anonym", 2, ""));

        Course course = service.getCourseWithReviews(new Course("IT1901", "Prosjektarbeid"));

        assertSame(storedCourse, course);
        assertEquals(2, course.getReviews().size());
        assertEquals(3.0, course.getAverageRating());
    }

    /**
     * Verifiserer at et emne uten lagrede anmeldelser gir et tomt emne med samme kode og navn, i
     * stedet for en feil.
     */
    @Test
    void getCourseWithReviewsReturnsEmptyCourseWhenNotStored() {
        Course course = service.getCourseWithReviews(new Course("TDT4100", "Objektorientert"));

        assertEquals("TDT4100", course.getCourseCode());
        assertEquals("Objektorientert", course.getCourseName());
        assertTrue(course.getReviews().isEmpty());
        assertEquals(0.0, course.getAverageRating());
    }

    /**
     * Verifiserer at oppslag uten emne avvises.
     */
    @Test
    void getCourseWithReviewsRejectsNullCourse() {
        assertThrows(IllegalArgumentException.class, () -> service.getCourseWithReviews(null));
    }

    /**
     * Verifiserer at tjenesten ikke kan opprettes uten lagring.
     */
    @Test
    void constructorRejectsNullRepository() {
        assertThrows(NullPointerException.class, () -> new ReviewService(null));
    }

    /**
     * Verifiserer at alle emnene hentes fra lagringen.
     */
    @Test
    void getAllCoursesReturnsStoredCourses() {
        storedCourse = new Course("IT1901", "Informatikk prosjektarbeid");

        assertEquals(List.of(storedCourse), service.getAllCourses());
    }

    /**
     * Verifiserer at et lagret emne blir funnet, og at et ukjent emne gir tomt svar.
     */
    @Test
    void findCourseReturnsStoredCourseOrEmpty() {
        storedCourse = new Course("IT1901", "Informatikk prosjektarbeid");

        assertSame(storedCourse, service.findCourse("IT1901").orElseThrow());
        assertTrue(service.findCourse("TDT4100").isEmpty());
    }
}