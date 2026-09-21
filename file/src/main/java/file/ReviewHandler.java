package file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import core.Review;

/**
 * Håndterer persistering av anmeldelser organisert per emne til og fra JSON.
 */
public final class ReviewHandler {

  private final Path path;
  private final ObjectMapper objectMapper;

  public ReviewHandler() {
    this(getDefaultPath("courses.json"));
  }

  public ReviewHandler(Path path) {
    if (path == null) {
      throw new IllegalArgumentException("Path cannot be null");
    }
    this.path = path;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  /**
   * DTO-struktur for JSON.
   */
  @JsonPropertyOrder({ "courseCode", "courseName", "reviews" })
  public static class CourseDTO {
    public String courseCode;
    public String courseName;
    public List<Review> reviews = new ArrayList<>();

    public CourseDTO() {
    }

    public CourseDTO(String courseCode, String courseName) {
      this.courseCode = courseCode;
      this.courseName = courseName;
    }

    public String getCourseCode() {
      return courseCode;
    }

    public void setCourseCode(String courseCode) {
      this.courseCode = courseCode;
    }

    public String getCourseName() {
      return courseName;
    }

    public void setCourseName(String courseName) {
      this.courseName = courseName;
    }

    public List<Review> getReviews() {
      return reviews;
    }

    public void setReviews(List<Review> reviews) {
      this.reviews = reviews != null ? reviews : new ArrayList<>();
    }
  }

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
   */
  public List<CourseDTO> readFromFile() {
    if (isEmpty()) {
      return new ArrayList<>();
    }
    try {
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
   */
  public void addReview(String courseCode, String courseName, Review review) {
    List<CourseDTO> courses = readFromFile();

    // Søker etter et eksisterende emne basert på emnekode
    CourseDTO targetCourse = courses.stream()
        .filter(c -> c.courseCode != null && c.courseCode.equalsIgnoreCase(courseCode))
        .findFirst()
        .orElse(null);

    if (targetCourse == null) {
      // Oppretter nytt emne dersom det ikke fantes fra før
      targetCourse = new CourseDTO(courseCode, courseName);
      courses.add(targetCourse);
    }

    // Legger til den nye anmeldelsen i emnets liste
    targetCourse.getReviews().add(review);

    // Lagrer hele strukturen på nytt
    writeToFile(courses);
  }

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