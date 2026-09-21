package file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import core.Review;

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


  /**
   * Tester standardkonstruktøren og statisk getDefaultPath-metode.
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
   * Tester isEmpty når filen eksisterer, men er tom (0 bytes).
   */
  @Test
  void testIsEmptyWithEmptyFile() throws IOException {
    Path file = tempDir.resolve("empty.json");
    Files.createFile(file);
    ReviewHandler handler = new ReviewHandler(file);
    assertTrue(handler.isEmpty());
  }


  /**
   * Tester at å legge til en anmeldelse på et emne som *allerede finnes* fungerer
   */
  @Test
  void testAddReviewToExistingCourse() {
    Path file = tempDir.resolve("courses.json");
    ReviewHandler handler = new ReviewHandler(file);
    
    // Legg til første anmeldelse (oppretter emnet)
    handler.addReview("IT1901", "Informatikk", new Review("Kari", 5, "Fantastisk"));
    
    // Legg til en ekstra anmeldelse på nøyaktig samme emnekode
    handler.addReview("IT1901", "Informatikk", new Review("Ola", 3, "Ok"));

    List<CourseDTO> courses = handler.readFromFile();
    assertEquals(1, courses.size());
    assertEquals(2, courses.get(0).getReviews().size());
  }



  /**
   * Tester at lesing av ødelagt/ugyldig JSON kaster RuntimeException.
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
   * Tester at writeToFile oppretter overliggende mapper automatisk hvis de mangler.
   */
  @Test
  void testWriteToFileCreatesParentDirectories() {
    Path nestedFile = tempDir.resolve("subfolder/nested/courses.json");
    ReviewHandler handler = new ReviewHandler(nestedFile);
    
    handler.addReview("TDT4100", "Objektorientert programmering", new Review("Per", 3, "Topp"));
    assertTrue(Files.exists(nestedFile));
  }

  /**
   * Tester at isEmpty håndterer IOException (f.eks. hvis stien peker på en mappe i stedet for fil).
   */
  @Test
  void testIsEmptyThrowsIOExceptionWhenPathIsDirectory() throws IOException {
    // Opprett en mappe i stedet for en fil
    Path dir = tempDir.resolve("someDirectory");
    Files.createDirectories(dir);
    
    ReviewHandler handler = new ReviewHandler(dir);
    
    // Files.exists(dir) er true, men Files.size(dir) vil feile og kaste IOException,
    // noe som gjør at koden går i catch-blokken og returnerer true.
    assertTrue(handler.isEmpty());
  }


  /**
   * Tester at writeToFile kaster RuntimeException ved IOException (f.eks. forsøk på å skrive til en mappe).
   */
  @Test
  void testWriteToFileThrowsRuntimeExceptionOnIOException() throws IOException {
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
    
    // Lagre en liste som manuelt inneholder et emne med null i emnekode
    CourseDTO nullCourse = new CourseDTO(null, "Uten kode");
    handler.writeToFile(List.of(nullCourse));

    // Legg til en ny anmeldelse; filteret skal håndtere at c.getCourseCode() er null uten å krasje
    handler.addReview("IT1901", "Informatikk", new Review("Ola", 5, "Bra"));

    List<CourseDTO> courses = handler.readFromFile();
    assertEquals(2, courses.size());
  }

}
