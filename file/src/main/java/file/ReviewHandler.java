package file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import core.Review;

/*
Håndterer persistering av anmeldelser organisert per emne til og fra JSON.
*/
public final class ReviewHandler {

  private final Path path;
  private final ObjectMapper objectMapper;

  public ReviewHandler() {
    this(getDefaultPath("courses.json"));
  }

  /**
   * Konstruerer en ny ReviewHandler med en spesifisert filsti.
   * 
   * @param path filstien som skal brukes til lagring og lesing av anmeldelser
   * @throws IllegalArgumentException hvis den medfølgende filstien er null
   */
  public ReviewHandler(Path path) {
    if (path == null) {
      throw new IllegalArgumentException("Path cannot be null");
    }
    this.path = path;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }


  /**
   * Sjekker om JSON-filen enten ikke eksisterer eller er tom.
   * 
   * @return true hvis filen ikke finnes eller har en størrelse på 0 bytes, ellers false
   */
  public boolean isEmpty() {
    if (!Files.exists(this.path)) {
      return true;
    }
    try {
      return Files.size(this.path) == 0;
    } catch (IOException e) {
      return true;
    }
  }

  /**
  * Leser eksisterende emner og anmeldelser fra JSON-filen.
  * 
  * @return liste av emner med reviews
  */
  public List<CourseDTO> readFromFile() {
    if (isEmpty()) {
      return new ArrayList<>();
    }
    try {
      //Henter info fra filer i form av CourseDTO
      return objectMapper.readValue(
          this.path.toFile(),
          objectMapper.getTypeFactory().constructCollectionType(List.class, CourseDTO.class)
      );
    } catch (IOException e) {
      throw new RuntimeException("Feil ved lesing av JSON-fil: " + this.path, e);
    }
  }



  /**
  * Legger til en anmeldelse for et emne. Dersom emnet allerede finnes i JSON-filen,
  * legges anmeldelsen til i det eksisterende emnet.
  * 
  * @param courseCode emnekoden til faget (f.eks. "IT1901")
  * @param courseName navnet på emnet (f.eks. "Informatikk prosjektarbeid")
  * @param review selve anmeldelsesobjektet som skal legges til
  */
  public void addReview(String courseCode, String courseName, Review review) {
    List<CourseDTO> courses = readFromFile();

    CourseDTO targetCourse = courses.stream()
        .filter(c -> c.courseCode != null && c.courseCode.equalsIgnoreCase(courseCode))
        .findFirst()
        .orElse(null);

    if (targetCourse == null) {
      targetCourse = new CourseDTO(courseCode, courseName);
      courses.add(targetCourse);
    }

    targetCourse.getReviews().add(review);

    writeToFile(courses);
  }



  /**
  * Skriver en liste med emner og anmeldelser ned til JSON-filen. 
  * Oppretter automatisk overliggende mapper dersom de ikke eksisterer fra før.
  *  
  * @param data listen med CourseDTO-objekter som skal lagres
  */
  public void writeToFile(List<CourseDTO> data) {
    try {
      Path parent = this.path.getParent();
      if (parent != null && !Files.exists(parent)) {
        Files.createDirectories(parent);
      }
      objectMapper.writeValue(this.path.toFile(), data);
    } catch (IOException e) {
      throw new RuntimeException("Feil ved skriving til JSON-fil: " + this.path, e);
    }
  }

  public static Path getDefaultPath(String fileName) {
    return Path.of(System.getProperty("user.home"), ".bithub", fileName);
  }
}