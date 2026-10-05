package file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import core.Course;
import core.Review;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

/**
 * Testklassen for ReviewHandler.
 *
 * <p>Verifiserer at anmeldelser lagres til og leses fra JSON-fil. Testene bruker
 * en midlertidig mappe slik at den ekte filen ikke berøres.
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
        handler.addReview(
                "IT1901",
                "Prosjektarbeid",
                new Review("Ola", 4, "Bra"));

        // Ny handler på samme fil viser at dataene faktisk ble lagret på disken.
        List<CourseDTO> courses = new ReviewHandler(file).readFromFile();

        assertEquals(1, courses.size());
        assertEquals("IT1901", courses.get(0).getCourseCode());
        assertEquals(4, courses.get(0).getReviews().get(0).getRating());
        assertEquals(
                "Ola",
                courses.get(0).getReviews().get(0).getReviewerName());
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
        assertThrows(
                IllegalArgumentException.class,
                () -> {
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
        handler.addReview(
                "IT1901",
                "Informatikk",
                new Review("Kari", 5, "Fantastisk"));

        // Legg til en ekstra anmeldelse på samme emnekode.
        handler.addReview(
                "IT1901",
                "Informatikk",
                new Review("Ola", 3, "Ok"));

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

        assertThrows(
                RuntimeException.class,
                () -> {
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

        handler.addReview(
                "TDT4100",
                "Objektorientert programmering",
                new Review("Per", 3, "Topp"));

        assertTrue(Files.exists(nestedFile));
    }

    /**
     * Tester feilhåndtering når filstørrelsen ikke kan leses.
     *
     * @throws IOException dersom mocking av filoperasjonen feiler
     */
    @Test
    void testIsEmptyReturnsTrueWhenSizeThrowsIoException() throws IOException {
        Path file = tempDir.resolve("unreadable.json");
        ReviewHandler handler = new ReviewHandler(file);

        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.exists(file)).thenReturn(true);
            files.when(() -> Files.size(file))
                    .thenThrow(new IOException("Kan ikke lese filstørrelsen"));

            assertTrue(handler.isEmpty());
            files.verify(() -> Files.size(file));
        }
    }

    /**
     * Tester at writeToFile kaster RuntimeException ved IOException.
     *
     * @throws IOException dersom testmappen ikke kan opprettes
     */
    @Test
    void testWriteToFileThrowsRuntimeExceptionOnIoException()
            throws IOException {
        Path dir = tempDir.resolve("directoryAsFile");
        Files.createDirectories(dir);
        ReviewHandler handler = new ReviewHandler(dir);

        assertThrows(
                RuntimeException.class,
                () -> {
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
        handler.addReview(
                "IT1901",
                "Informatikk",
                new Review("Ola", 5, "Bra"));

        List<CourseDTO> courses = handler.readFromFile();

        assertEquals(2, courses.size());
    }

    /**
     * Verifiserer at et lagret emne leses tilbake som domeneklassen Course, med
     * alle anmeldelsene og riktig gjennomsnitt.
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
     * Verifiserer at oppslaget ikke skiller mellom store og små bokstaver, slik
     * som addReview.
     */
    @Test
    void findCourseIgnoresCase() {
        Path file = tempDir.resolve("courses.json");
        ReviewHandler handler = new ReviewHandler(file);
        handler.addReview("IT1901", "Prosjektarbeid", new Review("Ola", 4, "Bra"));

        assertTrue(handler.findCourse("it1901").isPresent());
    }

    /**
     * Verifiserer at emner som ikke finnes, manglende fil og null gir tomt
     * resultat i stedet for feil.
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
     * Verifiserer at JSON fra før innføringen av ReviewDTO fortsatt kan leses.
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

        Course course = new ReviewHandler(file)
                .findCourse("IT1901").orElseThrow();

        assertEquals("IT1901", course.getCourseCode());
        assertEquals("Prosjektarbeid", course.getCourseName());
        assertEquals(1, course.getReviews().size());

        Review review = course.getReviews().get(0);
        assertEquals("Ola", review.getReviewerName());
        assertEquals(4, review.getRating());
        assertEquals("Bra fag", review.getComment());
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

        assertThrows(
                IllegalArgumentException.class,
                () -> reader.findCourse("IT1901"));
    }
}
