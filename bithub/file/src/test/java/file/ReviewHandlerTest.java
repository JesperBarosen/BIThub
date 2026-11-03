package file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import core.model.AuthorType;
import core.model.Course;
import core.model.Review;
import core.service.DefaultCourses;
import file.dto.CourseDTO;
import file.dto.ReviewDTO;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

/**
 * Testklassen for ReviewHandler.
 *
 * <p>Verifiserer at anmeldelser lagres til og leses fra JSON-fil. Testene bruker en midlertidig
 * mappe slik at den ekte filen ikke berøres.
 */
class ReviewHandlerTest {

    /**
     * JUnit lager en midlertidig mappe for hver test og sletter den etter.
     */
    @TempDir
    Path tempDir;

    /**
     * Verifiserer at en lagret anmeldelse kan leses av en ny ReviewHandler.
     */
    @Test
    void reviewIsSavedAndCanBeRead() {
        Path file = tempDir.resolve("courses.json");
        ReviewHandler handler = new ReviewHandler(file);
        handler.addReview("IT1901", "Prosjektarbeid", new Review("Ola", 4, "Bra"));

        // Ny handler på samme fil viser at dataene faktisk ble lagret på disken.
        List<CourseDTO> courses = new ReviewHandler(file).readFromFile();

        assertEquals(1, courses.size());
        assertEquals("IT1901", courses.get(0).getCourseCode());
        assertEquals(4, courses.get(0).getReviews().get(0).getRating());
        assertEquals("Ola", courses.get(0).getReviews().get(0).getReviewerName());
    }

    /**
     * Tester standardkonstruktøren og den statiske getDefaultPath-metoden.
     */
    @Test
    void testDefaultConstructorAndPath() {
        ReviewHandler handler = new ReviewHandler();
        assertNotNull(handler);

        Path defaultPath = ReviewHandler.getDefaultPath("courses.json");
        assertNotNull(defaultPath);
    }

    /**
     * Sjekker at konstruktøren kaster IllegalArgumentException hvis stien er null.
     */
    @Test
    void testNullPathThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ReviewHandler(null);
        });
    }

    /**
     * Tester isEmpty når filen ikke eksisterer.
     */
    @Test
    void testIsEmptyWithNonExistentFile() {
        Path file = tempDir.resolve("nonexistent.json");
        ReviewHandler handler = new ReviewHandler(file);

        assertTrue(handler.isEmpty());
    }

    /**
     * Tester isEmpty når filen eksisterer, men er tom.
     *
     * @throws IOException dersom testfilen ikke kan opprettes
     */
    @Test
    void testIsEmptyWithEmptyFile() throws IOException {
        Path file = tempDir.resolve("empty.json");
        Files.createFile(file);
        ReviewHandler handler = new ReviewHandler(file);

        assertTrue(handler.isEmpty());
    }

    /**
     * Tester at en anmeldelse kan legges til på et emne som allerede finnes.
     */
    @Test
    void testAddReviewToExistingCourse() {
        Path file = tempDir.resolve("courses.json");
        ReviewHandler handler = new ReviewHandler(file);

        // Legg til første anmeldelse og opprett emnet.
        handler.addReview("IT1901", "Informatikk", new Review("Kari", 5, "Fantastisk"));

        // Legg til en ekstra anmeldelse på samme emnekode.
        handler.addReview("IT1901", "Informatikk", new Review("Ola", 3, "Ok"));

        List<CourseDTO> courses = handler.readFromFile();

        assertEquals(1, courses.size());
        assertEquals(2, courses.get(0).getReviews().size());
    }

    /**
     * Tester at lesing av ugyldig JSON kaster RuntimeException.
     *
     * @throws IOException dersom testfilen ikke kan skrives
     */
    @Test
    void testReadFromMalformedJsonThrowsRuntimeException() throws IOException {
        Path file = tempDir.resolve("malformed.json");
        Files.writeString(file, "Dette er ikke gyldig JSON-format!");
        ReviewHandler handler = new ReviewHandler(file);

        assertThrows(RuntimeException.class, () -> {
            handler.readFromFile();
        });
    }

    /**
     * Tester at writeToFile oppretter manglende overliggende mapper automatisk.
     */
    @Test
    void testWriteToFileCreatesParentDirectories() {
        Path nestedFile = tempDir.resolve("subfolder/nested/courses.json");
        ReviewHandler handler = new ReviewHandler(nestedFile);

        handler.addReview("TDT4100", "Objektorientert programmering", new Review("Per", 3, "Topp"));

        assertTrue(Files.exists(nestedFile));
    }

    /**
     * Verifiserer at isEmpty kaster UncheckedIOException når filstørrelsen ikke kan leses.
     *
     * @throws IOException dersom mocking av filoperasjonen feiler
     */
    @Test
    void testIsEmptyThrowsWhenSizeThrowsIoException() throws IOException {
        Path file = tempDir.resolve("unreadable.json");
        ReviewHandler handler = new ReviewHandler(file);

        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.exists(file)).thenReturn(true);
            files.when(() -> Files.size(file))
                    .thenThrow(new IOException("Kan ikke lese filstørrelsen"));

            assertThrows(UncheckedIOException.class, handler::isEmpty);
        }
    }

    /**
     * Tester at writeToFile kaster RuntimeException ved IOException.
     *
     * @throws IOException dersom testmappen ikke kan opprettes
     */
    @Test
    void testWriteToFileThrowsRuntimeExceptionOnIoException() throws IOException {
        Path dir = tempDir.resolve("directoryAsFile");
        Files.createDirectories(dir);
        ReviewHandler handler = new ReviewHandler(dir);

        assertThrows(RuntimeException.class, () -> {
            handler.writeToFile(List.of());
        });
    }

    /**
     * Tester addReview når en eksisterende CourseDTO har null som emnekode.
     */
    @Test
    void testAddReviewWithNullCourseCodeInExistingList() {
        Path file = tempDir.resolve("courses.json");
        ReviewHandler handler = new ReviewHandler(file);

        // Lagre en liste med et emne som har null som emnekode.
        CourseDTO nullCourse = new CourseDTO(null, "Uten kode");
        handler.writeToFile(List.of(nullCourse));

        // Filteret skal håndtere null-emnekoden uten å krasje.
        handler.addReview("IT1901", "Informatikk", new Review("Ola", 5, "Bra"));

        List<CourseDTO> courses = handler.readFromFile();

        assertEquals(2, courses.size());
    }

    /**
     * Verifiserer at et lagret emne leses tilbake som domeneklassen Course, med alle anmeldelsene
     * og riktig gjennomsnitt.
     */
    @Test
    void findCourseReturnsCourseWithReviews() {
        Path file = tempDir.resolve("courses.json");
        ReviewHandler handler = new ReviewHandler(file);
        handler.addReview("IT1901", "Prosjektarbeid", new Review("Ola", 5, "Bra"));
        handler.addReview("IT1901", "Prosjektarbeid", new Review("Anonym", 2, "Mye jobb"));
        handler.addReview("TDT4100", "Objektorientert", new Review("Kari", 1, ""));

        Optional<Course> course = new ReviewHandler(file).findCourse("IT1901");

        assertTrue(course.isPresent());
        assertEquals("IT1901", course.get().getCourseCode());
        assertEquals("Prosjektarbeid", course.get().getCourseName());
        assertEquals(2, course.get().getReviews().size());
        assertEquals("Ola", course.get().getReviews().get(0).getReviewerName());
        assertEquals(3.5, course.get().getAverageRating());
    }

    /**
     * Verifiserer at oppslaget ikke skiller mellom store og små bokstaver, slik som addReview.
     */
    @Test
    void findCourseIgnoresCase() {
        Path file = tempDir.resolve("courses.json");
        ReviewHandler handler = new ReviewHandler(file);
        handler.addReview("IT1901", "Prosjektarbeid", new Review("Ola", 4, "Bra"));

        assertTrue(handler.findCourse("it1901").isPresent());
    }

    /**
     * Verifiserer at emner som ikke finnes, manglende fil og null gir tomt resultat i stedet for
     * feil.
     */
    @Test
    void findCourseReturnsEmptyWhenNotFound() {
        Path file = tempDir.resolve("courses.json");
        ReviewHandler handler = new ReviewHandler(file);

        assertFalse(handler.findCourse("IT1901").isPresent());

        handler.addReview("IT1901", "Prosjektarbeid", new Review("Ola", 4, "Bra"));
        assertFalse(handler.findCourse("TDT4100").isPresent());
        assertFalse(handler.findCourse(null).isPresent());
    }

    /**
     * Verifiserer at JSON fra release 2 fortsatt kan leses, og at anmeldelsene blir uten eier.
     *
     * @throws IOException dersom testfilen ikke kan skrives
     */
    @Test
    void findCourseReadsExistingJsonFormat() throws IOException {
        Path file = tempDir.resolve("courses.json");
        Files.writeString(file, """
                [
                  {
                    "courseCode": "IT1901",
                    "courseName": "Prosjektarbeid",
                    "reviews": [
                      {
                        "reviewerName": "Ola",
                        "rating": 4,
                        "comment": "Bra fag"
                      }
                    ]
                  }
                ]
                """);

        Course course = new ReviewHandler(file).findCourse("IT1901").orElseThrow();

        assertEquals("IT1901", course.getCourseCode());
        assertEquals("Prosjektarbeid", course.getCourseName());
        assertEquals(1, course.getReviews().size());

        Review review = course.getReviews().get(0);
        assertEquals("Ola", review.getReviewerName());
        assertEquals(4, review.getRating());
        assertEquals("Bra fag", review.getComment());
        assertNull(review.getOwnerEmail());
        assertFalse(review.isAnonymous());
        assertEquals(AuthorType.GUEST, review.getAuthorType());
    }

    /**
     * Verifiserer at ugyldig lagret rating avvises ved konvertering til Review.
     */
    @Test
    void findCourseRejectsInvalidStoredRating() {
        Path file = tempDir.resolve("courses.json");
        ReviewHandler handler = new ReviewHandler(file);
        CourseDTO course = new CourseDTO("IT1901", "Prosjektarbeid");
        course.getReviews().add(new ReviewDTO("Ola", 0, "Kommentar"));
        handler.writeToFile(List.of(course));

        ReviewHandler reader = new ReviewHandler(file);

        assertThrows(IllegalArgumentException.class, () -> reader.findCourse("IT1901"));
    }

    /**
     * Verifiserer at alle standardemnene finnes ved første oppstart, når datafilen ikke finnes fra
     * før.
     */
    @Test
    void allDefaultCoursesAreAvailableOnFirstStartup() {
        Path file = tempDir.resolve("courses.json");
        ReviewHandler handler = new ReviewHandler(file);
        assertFalse(Files.exists(file));

        handler.addCoursesIfMissing(DefaultCourses.all());

        List<Course> courses = new ReviewHandler(file).findAllCourses();
        assertEquals(DefaultCourses.all().size(), courses.size());
        assertEquals(DefaultCourses.all().stream().map(Course::getCourseCode).toList(),
                courses.stream().map(Course::getCourseCode).toList());
        assertTrue(courses.stream().allMatch(course -> course.getReviews().isEmpty()));
    }

    /**
     * Verifiserer at emner uten anmeldelser er med når alle emner hentes.
     */
    @Test
    void findAllCoursesIncludesCoursesWithoutReviews() {
        Path file = tempDir.resolve("courses.json");
        ReviewHandler handler = new ReviewHandler(file);
        handler.addReview("IT1901", "Prosjektarbeid", new Review("Ola", 4, "Bra"));
        handler.addCoursesIfMissing(List.of(new Course("TDT4100", "Objektorientert")));

        List<Course> courses = handler.findAllCourses();

        assertEquals(2, courses.size());
        assertEquals(1, courses.get(0).getReviews().size());
        assertEquals("TDT4100", courses.get(1).getCourseCode());
        assertTrue(courses.get(1).getReviews().isEmpty());
    }

    /**
     * Verifiserer at eksisterende anmeldelser bevares, og at ingen emner blir lagt inn to ganger,
     * når standardemnene initialiseres på nytt.
     */
    @Test
    void reinitializingDefaultCoursesKeepsExistingReviews() {
        Path file = tempDir.resolve("courses.json");
        ReviewHandler handler = new ReviewHandler(file);
        handler.addCoursesIfMissing(DefaultCourses.all());
        handler.addReview("IT1901", "Informatikk prosjektarbeid I", new Review("Ola", 5, "Bra"));
        handler.addReview("IT1901", "Informatikk prosjektarbeid I", new Review("Kari", 3, ""));

        handler.addCoursesIfMissing(DefaultCourses.all());
        new ReviewHandler(file).addCoursesIfMissing(DefaultCourses.all());

        List<Course> courses = new ReviewHandler(file).findAllCourses();
        assertEquals(DefaultCourses.all().size(), courses.size());
        Course course = handler.findCourse("IT1901").orElseThrow();
        assertEquals(2, course.getReviews().size());
        assertEquals("Ola", course.getReviews().get(0).getReviewerName());
        assertEquals("Kari", course.getReviews().get(1).getReviewerName());
    }

    /**
     * Verifiserer at en fil med emnelisten fra før får de nye standardemnene, at alle emnene
     * hentes sortert på emnekode, og at anmeldelser og navn som finnes fra før, beholdes.
     */
    @Test
    void newDefaultCoursesAreAddedSortedToExistingFile() {
        Path file = tempDir.resolve("courses.json");
        ReviewHandler handler = new ReviewHandler(file);
        handler.addCoursesIfMissing(List.of(new Course("BI2081", "Natur, miljø og bærekraft"),
                new Course("EXPH0300", "Examen philosophicum"),
                new Course("HMS0002", "HMS-kurs for 1. årsstudenter"),
                new Course("IT1604", "Digitalt samfunn"),
                new Course("IT1901", "Informatikk prosjektarbeid I")));
        handler.addReview("IT1901", "Informatikk prosjektarbeid I", new Review("Ola", 5, "Bra"));

        new ReviewHandler(file).addCoursesIfMissing(DefaultCourses.all());

        List<Course> courses = new ReviewHandler(file).findAllCourses();
        assertEquals(DefaultCourses.all().stream().map(Course::getCourseCode).toList(),
                courses.stream().map(Course::getCourseCode).toList());
        assertEquals(1, handler.findCourse("IT1901").orElseThrow().getReviews().size());
        assertEquals("Examen philosophicum",
                handler.findCourse("EXPH0300").orElseThrow().getCourseName());
    }

    /**
     * Verifiserer at id, eier og anonymitet lagres i filen og leses tilbake uendret.
     */
    @Test
    void findCourseKeepsIdOwnerAndAnonymity() {
        Path file = tempDir.resolve("courses.json");
        new ReviewHandler(file).addReview("IT1901", "Prosjektarbeid",
                new Review("abc-123", "ola@ntnu.no", "Ola", true, 4, "Bra"));

        Review review = new ReviewHandler(file).findCourse("IT1901").orElseThrow().getReviews()
                .get(0);

        assertEquals("abc-123", review.getId());
        assertEquals("ola@ntnu.no", review.getOwnerEmail());
        assertTrue(review.isAnonymous());
    }

    /**
     * Verifiserer at en anmeldelse uten id i en fil fra release 2 får en id som lagres i filen,
     * slik at id-en er den samme ved neste lesing.
     *
     * @throws IOException dersom testfilen ikke kan skrives eller leses
     */
    @Test
    void findCourseGivesOldReviewStableId() throws IOException {
        Path file = tempDir.resolve("courses.json");
        Files.writeString(file, """
                [
                  {
                    "courseCode": "IT1901",
                    "courseName": "Prosjektarbeid",
                    "reviews": [
                      {
                        "reviewerName": "Ola",
                        "rating": 4,
                        "comment": "Bra fag"
                      }
                    ]
                  }
                ]
                """);

        String firstId = new ReviewHandler(file).findCourse("IT1901").orElseThrow().getReviews()
                .get(0).getId();
        String secondId = new ReviewHandler(file).findCourse("IT1901").orElseThrow().getReviews()
                .get(0).getId();

        assertNotNull(firstId);
        assertEquals(firstId, secondId);
        assertTrue(Files.readString(file).contains(firstId));
    }

    /**
     * Sjekker at en blank id erstattes og lagres, slik at anmeldelsen får samme id ved ny lesing.
     */
    @Test
    void blankReviewIdIsReplacedAndSaved() {
        Path file = tempDir.resolve("blank-review-id.json");
        CourseDTO course = new CourseDTO("IT1901", "Prosjektarbeid");
        course.getReviews()
                .add(new ReviewDTO("   ", "ola@ntnu.no", "Ola", false, 4, "Bra fag"));
        new ReviewHandler(file).writeToFile(List.of(course));

        Review firstRead = new ReviewHandler(file).findCourse("IT1901").orElseThrow()
                .getReviews().get(0);
        Review secondRead = new ReviewHandler(file).findCourse("IT1901").orElseThrow()
                .getReviews().get(0);

        assertFalse(firstRead.getId().isBlank());
        assertEquals(firstRead.getId(), secondRead.getId());
        assertEquals("Bra fag", secondRead.getComment());
    }

    /**
     * Kontrollerer at oppdateringer som skjer samtidig fra flere ReviewHandler-instanser ikke fører
     * til at anmeldelser går tapt i JSON-filen.
     *
     * @throws Exception hvis det er feil i en av trådene eller den ikke blir ferdig i tide
     */
    @Test
    void testReviewsWhenAddedSimultaneously() throws Exception {
        int numberOfReviews = 20;
        Path path = tempDir.resolve("simultaneously-reviews.json");
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch ready = new CountDownLatch(8);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < numberOfReviews; i++) {
                final int reviewNumber = i;

                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();

                    ReviewHandler handler = new ReviewHandler(path);
                    handler.addReview("IT1901", "Informatikk prosjektarbeid",
                            new Review("Reviewer " + reviewNumber, 4, "Kommentar " + reviewNumber));

                    return null;
                }));
            }

            assertTrue(ready.await(10, TimeUnit.SECONDS), "Alle trådene skulle bli klare");

            start.countDown();

            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }

            List<CourseDTO> courses = new ReviewHandler(path).readFromFile();

            assertEquals(1, courses.size());
            assertEquals(numberOfReviews, courses.get(0).getReviews().size());
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    /**
     * Lager en anmeldelse med eier, slik den blir når en innlogget bruker skriver den.
     *
     * @param id      id-en til anmeldelsen
     * @param owner   e-posten til eieren
     * @param rating  vurderingen
     * @param comment kommentaren
     * @return anmeldelsen
     */
    private static Review ownedReview(String id, String owner, int rating, String comment) {
        return new Review(id, owner, "Kari", false, rating, comment);
    }

    /**
     * Verifiserer at findReview finner en lagret anmeldelse med id, og gir tomt svar for ukjent id
     * og null.
     */
    @Test
    void findReviewReturnsStoredReview() {
        ReviewHandler handler = new ReviewHandler(tempDir.resolve("find-review.json"));
        handler.addReview("IT1901", "Prosjektarbeid",
                ownedReview("id-1", "kari@ntnu.no", 4, "Bra"));

        Review review = handler.findReview("id-1").orElseThrow();

        assertEquals("kari@ntnu.no", review.getOwnerEmail());
        assertEquals(4, review.getRating());
        assertTrue(handler.findReview("finnes-ikke").isEmpty());
        assertTrue(handler.findReview(null).isEmpty());
    }

    /**
     * Verifiserer at updateReview endrer vurdering, kommentar og anonymitet, og beholder id, eier
     * og navn. Endringen skal være lagret i filen, slik at en ny ReviewHandler leser den.
     */
    @Test
    void updateReviewChangesRatingCommentAndAnonymity() {
        Path path = tempDir.resolve("update-review.json");
        ReviewHandler handler = new ReviewHandler(path);
        handler.addReview("IT1901", "Prosjektarbeid", ownedReview("id-1", "kari@ntnu.no", 2, "Ok"));

        assertTrue(handler.updateReview("id-1", 5, "Mye bedre", true));

        Review review = new ReviewHandler(path).findReview("id-1").orElseThrow();
        assertEquals("id-1", review.getId());
        assertEquals("kari@ntnu.no", review.getOwnerEmail());
        assertEquals("Kari", review.getReviewerName());
        assertEquals(5, review.getRating());
        assertEquals("Mye bedre", review.getComment());
        assertTrue(review.isAnonymous());
    }

    /**
     * Verifiserer at updateReview gir false for en ukjent id, uten å endre filen.
     */
    @Test
    void updateReviewReturnsFalseForUnknownId() {
        ReviewHandler handler = new ReviewHandler(tempDir.resolve("update-unknown.json"));
        handler.addReview("IT1901", "Prosjektarbeid", ownedReview("id-1", "kari@ntnu.no", 2, "Ok"));

        assertFalse(handler.updateReview("finnes-ikke", 5, "Ny", false));
        assertEquals(2, handler.findReview("id-1").orElseThrow().getRating());
    }

    /**
     * Verifiserer at en ugyldig vurdering avvises, og at den lagrede anmeldelsen ikke endres.
     */
    @Test
    void updateReviewRejectsInvalidRating() {
        ReviewHandler handler = new ReviewHandler(tempDir.resolve("update-invalid.json"));
        handler.addReview("IT1901", "Prosjektarbeid", ownedReview("id-1", "kari@ntnu.no", 2, "Ok"));

        assertThrows(IllegalArgumentException.class,
                () -> handler.updateReview("id-1", 9, "Ugyldig", false));
        assertEquals(2, handler.findReview("id-1").orElseThrow().getRating());
    }

    /**
     * Verifiserer at deleteReview bare sletter den ene anmeldelsen, og at emnet beholdes.
     */
    @Test
    void deleteReviewRemovesOnlyThatReview() {
        Path path = tempDir.resolve("delete-review.json");
        ReviewHandler handler = new ReviewHandler(path);
        handler.addReview("IT1901", "Prosjektarbeid", ownedReview("id-1", "kari@ntnu.no", 4, "En"));
        handler.addReview("IT1901", "Prosjektarbeid", ownedReview("id-2", "ola@ntnu.no", 3, "To"));

        assertTrue(handler.deleteReview("id-1"));

        Course course = new ReviewHandler(path).findCourse("IT1901").orElseThrow();
        assertEquals(1, course.getReviews().size());
        assertEquals("id-2", course.getReviews().get(0).getId());
    }

    /**
     * Verifiserer at deleteReview gir false for en ukjent id.
     */
    @Test
    void deleteReviewReturnsFalseForUnknownId() {
        ReviewHandler handler = new ReviewHandler(tempDir.resolve("delete-unknown.json"));
        handler.addReview("IT1901", "Prosjektarbeid", ownedReview("id-1", "kari@ntnu.no", 4, "En"));

        assertFalse(handler.deleteReview("finnes-ikke"));
        assertEquals(1, handler.findCourse("IT1901").orElseThrow().getReviews().size());
    }

    /**
     * Kontrollerer at samtidige endringer og slettinger fra flere ReviewHandler-instanser ikke
     * overskriver hverandre. Halvparten av anmeldelsene endres og resten slettes samtidig, og til
     * slutt skal nøyaktig de endrede være igjen.
     *
     * @throws Exception hvis det er feil i en av trådene eller den ikke blir ferdig i tide
     */
    @Test
    void testReviewsWhenUpdatedAndDeletedSimultaneously() throws Exception {
        int numberOfReviews = 20;
        Path path = tempDir.resolve("simultaneously-update-delete.json");
        ReviewHandler setup = new ReviewHandler(path);
        for (int i = 0; i < numberOfReviews; i++) {
            setup.addReview("IT1901", "Prosjektarbeid",
                    ownedReview("id-" + i, "kari@ntnu.no", 1, "Kommentar " + i));
        }

        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch ready = new CountDownLatch(8);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < numberOfReviews; i++) {
                final String reviewId = "id-" + i;
                final boolean update = i % 2 == 0;

                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();

                    ReviewHandler handler = new ReviewHandler(path);
                    return update ? handler.updateReview(reviewId, 5, "Endret", false)
                            : handler.deleteReview(reviewId);
                }));
            }

            assertTrue(ready.await(10, TimeUnit.SECONDS), "Alle trådene skulle bli klare");

            start.countDown();

            for (Future<Boolean> future : futures) {
                assertTrue(future.get(10, TimeUnit.SECONDS));
            }

            List<Review> reviews = new ReviewHandler(path).findCourse("IT1901").orElseThrow()
                    .getReviews();

            assertEquals(numberOfReviews / 2, reviews.size());
            for (Review review : reviews) {
                assertEquals(5, review.getRating());
                assertEquals("Endret", review.getComment());
            }
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }
}
