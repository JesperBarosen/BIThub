package file;

import static org.junit.jupiter.api.Assertions.assertEquals;

import core.Review;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Testklassen for ReviewHandler. Den verifiserer at anmeldelsene lagres til og leses fra JSON-fil.
 * Testene bruker en midlertidig mappe slik at den ekte filen ikke skal berøres.
 */
class ReviewHandlerTest {
  /** 
   * JUnit lager en midlertidig mappe for hver test og sletter den etter.
   */
  @TempDir
  Path tempDir;

  /**
   * Verifiserer at en anmeldelse som er lagret til fil kan leses tilbake av en ny ReviewHandler.
   */
  @Test
  void reviewIsSavedAndCanBeRead() {
    Path file = tempDir.resolve("courses.json");
    ReviewHandler handler = new ReviewHandler(file);
    handler.addReview("IT1901", "Prosjektarbeid", new Review("Ola", 4, "Bra"));
    
    // Ny handler på samme fil som beviser at dataene faktisk ble lagret på disken
    List<CourseDTO> courses = new ReviewHandler(file).readFromFile();

    assertEquals(1, courses.size());
    assertEquals("IT1901", courses.get(0).getCourseCode());
    assertEquals(4, courses.get(0).getReviews().get(0).getRating());
    assertEquals("Ola", courses.get(0).getReviews().get(0).getReviewerName());
  }
}
