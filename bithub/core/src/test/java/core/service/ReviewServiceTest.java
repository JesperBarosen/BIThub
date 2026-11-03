package core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.exception.NotReviewOwnerException;
import core.exception.ReviewNotFoundException;
import core.model.AuthorType;
import core.model.Course;
import core.model.Review;
import core.model.User;
import core.repository.ReviewRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

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

    private final List<String> updatedIds = new ArrayList<>();
    private final List<String> updatedComments = new ArrayList<>();
    private final List<String> deletedIds = new ArrayList<>();

    /**
     * Om den falske lagringen finner anmeldelsen når den skal endres eller slettes. Settes til
     * false for å etterligne at anmeldelsen ble slettet rett etter eierskapssjekken.
     */
    private boolean storageFindsReview = true;

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

        @Override
        public boolean updateReview(String reviewId, int rating, String comment,
                boolean anonymous) {
            updatedIds.add(reviewId);
            updatedComments.add(comment);
            return storageFindsReview;
        }

        @Override
        public boolean deleteReview(String reviewId) {
            deletedIds.add(reviewId);
            return storageFindsReview;
        }
    };

    private final ReviewService service = new ReviewService(fakeRepository);

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void submitReviewPreservesOwnerAndAnonymity(boolean anonymous) {
        User owner = new User("Ola Nordmann", "ola@example.com", "Passord123");

        Review review = service.submitReview("IT1901", "Prosjektarbeid", owner, anonymous, " 4 ",
                "  Bra fag  ");

        assertSame(review, savedReviews.get(0));
        assertFalse(review.getId().isBlank());
        assertEquals(owner.getEmail(), review.getOwnerEmail());
        assertEquals(owner.getName(), review.getReviewerName());
        assertEquals(anonymous, review.isAnonymous());
        assertEquals(anonymous ? AuthorType.ANONYMOUS : AuthorType.USER, review.getAuthorType());
        assertEquals(anonymous ? "Anonym" : owner.getName(), review.getDisplayName());
        assertEquals(4, review.getRating());
        assertEquals("Bra fag", review.getComment());
    }

    /**
     * Sjekker at innsending med en verifisert eier lagrer manglende kommentar som tom tekst.
     */
    @Test
    void submitReviewWithOwnerSavesEmptyCommentWhenNull() {
        User owner = new User("Ola Nordmann", "ola@example.com", "Passord123");

        Review review = service.submitReview("IT1901", "Prosjektarbeid", owner, false, "4", null);

        assertEquals(1, savedReviews.size());
        assertSame(review, savedReviews.get(0));
        assertEquals("", review.getComment());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void submitReviewWithoutOwnerIsRejected(boolean anonymous) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.submitReview("IT1901", "Prosjektarbeid", null, anonymous, "4", null));

        assertEquals("Du må logge inn for å skrive en anmeldelse.", e.getMessage());
        assertTrue(savedReviews.isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "0", "6", "fire" })
    void submitReviewWithOwnerRejectsInvalidRating(String rating) {
        User owner = new User("Ola Nordmann", "ola@example.com", "Passord123");

        assertThrows(IllegalArgumentException.class,
                () -> service.submitReview("IT1901", "Prosjektarbeid", owner, false, rating, ""));
        assertTrue(savedReviews.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void submitReviewFromClientKeepsLoggedInUserAndAnonymity(boolean anonymous) {
        Review review = service.submitReview("IT1901", "Prosjektarbeid", "kari@ntnu.no",
                "Kari Nordmann", anonymous, " 5 ", "  Lærerikt  ");

        assertSame(review, savedReviews.get(0));
        assertEquals("kari@ntnu.no", review.getOwnerEmail());
        assertEquals("Kari Nordmann", review.getReviewerName());
        assertEquals(anonymous, review.isAnonymous());
        assertEquals(5, review.getRating());
        assertEquals("Lærerikt", review.getComment());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void submitReviewFromClientRequiresOwnerEmail(String ownerEmail) {
        assertThrows(IllegalArgumentException.class, () -> service.submitReview("IT1901",
                "Prosjektarbeid", ownerEmail, "Kari Nordmann", true, "4", ""));
        assertTrue(savedReviews.isEmpty());
    }

    /**
     * Verifiserer at en gyldig anmeldelse sendes videre til lagringen med riktig emne og riktige
     * verdier.
     */
    @Test
    void submitReviewSavesValidReview() {
        service.submitReview("IT1901", "Informatikk prosjektarbeid", "kari@ntnu.no",
                "Kari Nordmann", false, "4", "Bra fag");

        assertEquals(List.of("IT1901 - Informatikk prosjektarbeid"), savedCourses);
        assertEquals(1, savedReviews.size());
        Review review = savedReviews.get(0);
        assertEquals("Kari Nordmann", review.getReviewerName());
        assertEquals(4, review.getRating());
        assertEquals("Bra fag", review.getComment());
    }

    /**
     * Verifiserer at mellomrom rundt vurdering og kommentar fjernes, og at manglende kommentar blir
     * en tom streng.
     */
    @Test
    void submitReviewTrimsInput() {
        submitAsStudent(" 5 ", "  Lærerikt  ");
        submitAsStudent("3", null);

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
                    () -> submitAsStudent(rating, ""));
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
                () -> submitAsStudent("fire", ""));
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
                    () -> submitAsStudent(rating, ""));
            assertEquals("Vurdering må være et tall fra 1 til 5.", e.getMessage());
        }
        assertTrue(savedReviews.isEmpty());
    }

    /**
     * Verifiserer at manglende emne eller forfatter avvises og ikke lagres.
     */
    @Test
    void submitReviewRejectsMissingCourseOrReviewer() {
        assertThrows(IllegalArgumentException.class, () -> service.submitReview("",
                "Prosjektarbeid", "student@ntnu.no", "Student", false, "4", ""));

        assertThrows(IllegalArgumentException.class, () -> service.submitReview("IT1901", "",
                "student@ntnu.no", "Student", false, "4", ""));

        assertThrows(IllegalArgumentException.class, () -> service.submitReview("IT1901",
                "Prosjektarbeid", "student@ntnu.no", "", false, "4", ""));
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
                () -> failingService.submitReview("IT1901", "Prosjektarbeid", "student@ntnu.no",
                        "Student", false, "4", ""));
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

    /**
     * Verifiserer at bare eierens egne anmeldelser hentes, også de anonyme, og ikke anmeldelser fra
     * andre brukere eller gjester.
     */
    @Test
    void findReviewsByOwnerReturnsOnlyOwnReviews() {
        Review own = new Review("id-1", "ola@example.com", "Ola", false, 5, "Bra");
        Review ownAnonymous = new Review("id-2", "ola@example.com", "Ola", true, 4, "");
        storedCourse = new Course("IT1901", "Informatikk prosjektarbeid I");
        storedCourse.addReview(own);
        storedCourse.addReview(ownAnonymous);
        storedCourse.addReview(new Review("id-3", "kari@example.com", "Kari", false, 3, ""));
        storedCourse.addReview(new Review("Gjest", 2, ""));

        List<Course> result = service.findReviewsByOwner("ola@example.com");

        assertEquals(1, result.size());
        assertEquals("IT1901", result.get(0).getCourseCode());
        assertEquals(List.of(own, ownAnonymous), result.get(0).getReviews());
    }

    /**
     * Verifiserer at emner der brukeren ikke har skrevet noe, ikke er med.
     */
    @Test
    void findReviewsByOwnerSkipsCoursesWithoutOwnReviews() {
        storedCourse = new Course("IT1901", "Informatikk prosjektarbeid I");
        storedCourse.addReview(new Review("id-3", "kari@example.com", "Kari", false, 3, ""));

        assertTrue(service.findReviewsByOwner("ola@example.com").isEmpty());
    }

    /**
     * Verifiserer at e-posten sammenlignes uten å skille store og små bokstaver.
     */
    @Test
    void findReviewsByOwnerIgnoresCaseInEmail() {
        storedCourse = new Course("IT1901", "Informatikk prosjektarbeid I");
        storedCourse.addReview(new Review("id-1", "ola@example.com", "Ola", false, 5, "Bra"));

        assertEquals(1, service.findReviewsByOwner("OLA@example.com").size());
    }

    /**
     * Verifiserer at manglende e-post gir en tom liste.
     *
     * @param email e-posten som mangler
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   " })
    void findReviewsByOwnerReturnsEmptyForMissingEmail(String email) {
        storedCourse = new Course("IT1901", "Informatikk prosjektarbeid I");
        storedCourse.addReview(new Review("id-1", "ola@example.com", "Ola", false, 5, "Bra"));

        assertTrue(service.findReviewsByOwner(email).isEmpty());
    }

    /**
     * Verifiserer at eieren kan endre sin egen anmeldelse, og at id, eier og navn beholdes.
     * E-posten sammenlignes uten å skille store og små bokstaver.
     */
    @Test
    void ownerCanUpdateOwnReview() {
        storeReview("id-1", "ola@example.com");

        Review updated = service.updateReview("id-1", " OLA@example.com ", " 2 ", "  Tungt  ",
                true);

        assertEquals(List.of("id-1"), updatedIds);
        assertEquals("id-1", updated.getId());
        assertEquals("ola@example.com", updated.getOwnerEmail());
        assertEquals("Ola", updated.getReviewerName());
        assertTrue(updated.isAnonymous());
        assertEquals(2, updated.getRating());
        assertEquals("Tungt", updated.getComment());
    }

    /**
     * Sjekker at null som ny kommentar blir tom tekst både i lagringen og i svaret.
     */
    @Test
    void ownerCanRemoveReviewComment() {
        storeReview("id-1", "ola@example.com");

        Review updated = service.updateReview("id-1", "ola@example.com", "4", null, false);

        assertEquals(List.of("id-1"), updatedIds);
        assertEquals(List.of(""), updatedComments);
        assertEquals("", updated.getComment());
        assertEquals("id-1", updated.getId());
        assertEquals("ola@example.com", updated.getOwnerEmail());
    }

    /**
     * Verifiserer at eieren kan slette sin egen anmeldelse.
     */
    @Test
    void ownerCanDeleteOwnReview() {
        storeReview("id-1", "ola@example.com");

        service.deleteReview("id-1", "ola@example.com");

        assertEquals(List.of("id-1"), deletedIds);
    }

    /**
     * Verifiserer at en annen bruker ikke kan endre eller slette anmeldelsen, og at lagringen da
     * ikke blir kalt.
     */
    @Test
    void otherUserCannotUpdateOrDeleteReview() {
        storeReview("id-1", "ola@example.com");

        assertThrows(NotReviewOwnerException.class,
                () -> service.updateReview("id-1", "kari@example.com", "1", "", false));
        assertThrows(NotReviewOwnerException.class,
                () -> service.deleteReview("id-1", "kari@example.com"));
        assertTrue(updatedIds.isEmpty());
        assertTrue(deletedIds.isEmpty());
    }

    /**
     * Verifiserer at gjesteanmeldelser uten eier ikke kan endres eller slettes, og at manglende
     * e-post blir avvist.
     *
     * @param ownerEmail e-posten til den innloggede brukeren
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "ola@example.com" })
    void guestReviewCannotBeUpdatedOrDeleted(String ownerEmail) {
        storeReview("id-1", null);

        assertThrows(NotReviewOwnerException.class,
                () -> service.updateReview("id-1", ownerEmail, "1", "", false));
        assertThrows(NotReviewOwnerException.class, () -> service.deleteReview("id-1", ownerEmail));
        assertTrue(updatedIds.isEmpty());
        assertTrue(deletedIds.isEmpty());
    }

    /**
     * Verifiserer at en ukjent id gir ReviewNotFoundException.
     *
     * @param reviewId id-en som ikke finnes
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "finnes-ikke" })
    void unknownReviewIsNotFound(String reviewId) {
        storeReview("id-1", "ola@example.com");

        assertThrows(ReviewNotFoundException.class,
                () -> service.updateReview(reviewId, "ola@example.com", "3", "", false));
        assertThrows(ReviewNotFoundException.class,
                () -> service.deleteReview(reviewId, "ola@example.com"));
    }

    /**
     * Verifiserer at en ugyldig vurdering avvises før lagringen blir kalt.
     *
     * @param rating den ugyldige vurderingen
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "0", "6", "fire" })
    void updateRejectsInvalidRating(String rating) {
        storeReview("id-1", "ola@example.com");

        assertThrows(IllegalArgumentException.class,
                () -> service.updateReview("id-1", "ola@example.com", rating, "", false));
        assertTrue(updatedIds.isEmpty());
    }

    /**
     * Verifiserer at det gir ReviewNotFoundException hvis anmeldelsen blir slettet mellom
     * eierskapssjekken og endringen.
     */
    @Test
    void reviewRemovedBeforeChangeIsNotFound() {
        storeReview("id-1", "ola@example.com");
        storageFindsReview = false;

        assertThrows(ReviewNotFoundException.class,
                () -> service.updateReview("id-1", "ola@example.com", "3", "", false));
        assertThrows(ReviewNotFoundException.class,
                () -> service.deleteReview("id-1", "ola@example.com"));
    }

    /**
     * Verifiserer at tjenesten bruker lagringens egen henting av egne anmeldelser, slik klienten
     * for serveren trenger.
     */
    @Test
    void findReviewsByOwnerUsesRepository() {
        Course ownCourse = new Course("IT1901", "Informatikk prosjektarbeid I");
        List<String> askedFor = new ArrayList<>();
        ReviewService ownReviewsService = new ReviewService(new ReviewRepository() {
            @Override
            public void addReview(String courseCode, String courseName, Review review) {
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

            @Override
            public List<Course> findReviewsByOwner(String ownerEmail) {
                askedFor.add(ownerEmail);
                return List.of(ownCourse);
            }
        });

        assertEquals(List.of(ownCourse), ownReviewsService.findReviewsByOwner(" ola@example.com "));
        assertEquals(List.of("ola@example.com"), askedFor);
    }

    /**
     * Verifiserer at klienten kan endre en hentet anmeldelse, og at id, eier og navn beholdes.
     */
    @Test
    void updateFetchedReviewKeepsIdOwnerAndName() {
        Review review = new Review("id-1", "ola@example.com", "Ola", false, 4, "Bra");

        Review updated = service.updateReview(review, " 2 ", "  Tungt  ", true);

        assertEquals(List.of("id-1"), updatedIds);
        assertEquals("id-1", updated.getId());
        assertEquals("ola@example.com", updated.getOwnerEmail());
        assertEquals("Ola", updated.getReviewerName());
        assertTrue(updated.isAnonymous());
        assertEquals(2, updated.getRating());
        assertEquals("Tungt", updated.getComment());
    }

    /**
     * Verifiserer at en ugyldig vurdering avvises før lagringen blir kalt.
     *
     * @param rating den ugyldige vurderingen
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "0", "6", "fire" })
    void updateFetchedReviewRejectsInvalidRating(String rating) {
        Review review = new Review("id-1", "ola@example.com", "Ola", false, 4, "Bra");

        assertThrows(IllegalArgumentException.class,
                () -> service.updateReview(review, rating, "", false));
        assertTrue(updatedIds.isEmpty());
    }

    /**
     * Verifiserer at klienten kan slette en hentet anmeldelse.
     */
    @Test
    void deleteFetchedReviewDeletesById() {
        service.deleteReview(new Review("id-1", "ola@example.com", "Ola", false, 4, "Bra"));

        assertEquals(List.of("id-1"), deletedIds);
    }

    /**
     * Verifiserer at det gir ReviewNotFoundException hvis anmeldelsen er borte når klienten endrer
     * eller sletter den.
     */
    @Test
    void fetchedReviewThatIsGoneIsNotFound() {
        Review review = new Review("id-1", "ola@example.com", "Ola", false, 4, "Bra");
        storageFindsReview = false;

        assertThrows(ReviewNotFoundException.class,
                () -> service.updateReview(review, "3", "", false));
        assertThrows(ReviewNotFoundException.class, () -> service.deleteReview(review));
    }

    /**
     * Verifiserer at endring og sletting uten anmeldelse avvises.
     */
    @Test
    void fetchedReviewMustBeGiven() {
        assertThrows(NullPointerException.class,
                () -> service.updateReview((Review) null, "3", "", false));
        assertThrows(NullPointerException.class, () -> service.deleteReview((Review) null));
    }

    /**
     * Legger én anmeldelse i emnet som den falske lagringen returnerer.
     *
     * @param id         id-en til anmeldelsen
     * @param ownerEmail eieren, eller null for en gjesteanmeldelse
     */
    private void storeReview(String id, String ownerEmail) {
        storedCourse = new Course("IT1901", "Informatikk prosjektarbeid I");
        storedCourse.addReview(
                new Review(id, ownerEmail, ownerEmail == null ? "Gjest" : "Ola", false, 4, "Bra"));
    }

    /**
     * Sender inn en anmeldelse av IT1901 som en innlogget student.
     *
     * @param rating  vurderingen som tekst
     * @param comment kommentaren
     * @return den lagrede anmeldelsen
     */
    private Review submitAsStudent(String rating, String comment) {
        return service.submitReview("IT1901", "Prosjektarbeid", "student@ntnu.no", "Student", false,
                rating, comment);
    }
}
