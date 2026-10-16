package file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import core.Course;
import core.Review;
import core.ReviewRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Håndterer persistering av anmeldelser organisert per emne til og fra JSON.
 */
public final class ReviewHandler implements DataHandler<CourseDTO>, ReviewRepository {

    private final Path path;
    private final ObjectMapper objectMapper;

    /**
     * Oppretter en ReviewHandler med standard filsti.
     */
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
     * @throws UncheckedIOException hvis filstørrelsen ikke kan leses
     */
    @Override
    public boolean isEmpty() {
        if (!Files.exists(this.path)) {
            return true;
        }
        try {
            return Files.size(this.path) == 0;
        } catch (IOException e) {
            throw new UncheckedIOException("Kunne ikke lese filstørrelsen: " + this.path, e);
        }
    }

    /**
     * Leser eksisterende emner og anmeldelser fra JSON-filen. Anmeldelser som mangler id, for
     * eksempel fra eldre filer, får en ny id som lagres i filen med en gang.
     *
     * @return liste av emner med anmeldelser
     */
    @Override
    public List<CourseDTO> readFromFile() {
        if (isEmpty()) {
            return new ArrayList<>();
        }
        List<CourseDTO> courses;
        try {
            // Henter informasjon fra filer i form av CourseDTO.
            courses = objectMapper.readValue(this.path.toFile(), objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, CourseDTO.class));
        } catch (IOException e) {
            throw new UncheckedIOException("Feil ved lesing av JSON-fil: " + this.path, e);
        }
        if (assignMissingIds(courses)) {
            writeToFile(courses);
        }
        return courses;
    }

    /**
     * Gir en ny id til anmeldelser som mangler id, for eksempel anmeldelser fra eldre filer.
     *
     * @param courses emnene som er lest fra filen
     * @return true hvis minst én anmeldelse fikk ny id, ellers false
     */
    private static boolean assignMissingIds(List<CourseDTO> courses) {
        boolean changed = false;
        for (CourseDTO course : courses) {
            for (ReviewDTO review : course.getReviews()) {
                if (review.getId() == null || review.getId().isBlank()) {
                    review.setId(UUID.randomUUID().toString());
                    changed = true;
                }
            }
        }
        return changed;
    }

    /**
     * Legger til en anmeldelse for et emne. Dersom emnet allerede finnes i JSON-filen, legges
     * anmeldelsen til i det eksisterende emnet.
     *
     * @param courseCode emnekoden til faget, for eksempel "IT1901"
     * @param courseName navnet på emnet, for eksempel "Informatikk prosjektarbeid"
     * @param review     selve anmeldelsesobjektet som skal legges til
     */
    @Override
    public void addReview(String courseCode, String courseName, Review review) {
        List<CourseDTO> courses = readFromFile();

        CourseDTO targetCourse = courses.stream().filter(
                c -> c.getCourseCode() != null && c.getCourseCode().equalsIgnoreCase(courseCode))
                .findFirst().orElse(null);

        if (targetCourse == null) {
            targetCourse = new CourseDTO(courseCode, courseName);
            courses.add(targetCourse);
        }

        targetCourse.getReviews()
                .add(new ReviewDTO(review.getId(), review.getOwnerEmail(), review.getReviewerName(),
                        review.isAnonymous(), review.getRating(), review.getComment()));

        writeToFile(courses);
    }

    /**
     * Henter et lagret emne med anmeldelser fra JSON-filen og gjør det om til domeneklassen.
     *
     * @param courseCode emnekoden til emnet, store og små bokstaver skilles ikke
     * @return emnet med anmeldelser, eller tom dersom emnet ikke finnes i filen
     * @see Course
     */
    @Override
    public Optional<Course> findCourse(String courseCode) {
        if (courseCode == null) {
            return Optional.empty();
        }
        return readFromFile().stream().filter(c -> courseCode.equalsIgnoreCase(c.getCourseCode()))
                .findFirst().map(ReviewHandler::toCourse);
    }

    /**
     * Henter alle emnene i JSON-filen, også de uten anmeldelser.
     *
     * @return alle lagrede emner, eller en tom liste hvis filen mangler
     */
    @Override
    public List<Course> findAllCourses() {
        return readFromFile().stream().map(ReviewHandler::toCourse).toList();
    }

    /**
     * Legger emner uten anmeldelser inn i JSON-filen hvis de ikke finnes fra før. Emner som
     * allerede er lagret, beholdes med alle anmeldelsene. Filen skrives bare når minst ett emne er
     * lagt til.
     *
     * @param courses emnene som skal finnes i filen
     */
    @Override
    public void addCoursesIfMissing(List<Course> courses) {
        List<CourseDTO> storedCourses = readFromFile();
        boolean changed = false;
        for (Course course : courses) {
            boolean exists = storedCourses.stream()
                    .anyMatch(c -> course.getCourseCode().equalsIgnoreCase(c.getCourseCode()));
            if (!exists) {
                storedCourses.add(new CourseDTO(course.getCourseCode(), course.getCourseName()));
                changed = true;
            }
        }
        if (changed) {
            writeToFile(storedCourses);
        }
    }

    /**
     * Gjør om et lagret emne til domeneklassen, med alle anmeldelsene.
     *
     * @param courseDto emnet slik det er lagret i filen
     * @return emnet som domeneobjekt
     */
    private static Course toCourse(CourseDTO courseDto) {
        Course course = new Course(courseDto.getCourseCode(), courseDto.getCourseName());
        for (ReviewDTO reviewDto : courseDto.getReviews()) {
            course.addReview(new Review(reviewDto.getId(), reviewDto.getOwnerEmail(),
                    reviewDto.getReviewerName(), reviewDto.isAnonymous(), reviewDto.getRating(),
                    reviewDto.getComment()));
        }
        return course;
    }

    /**
     * Skriver en liste med emner og anmeldelser ned til JSON-filen. Oppretter automatisk
     * overliggende mapper dersom de ikke eksisterer fra før.
     *
     * @param data listen med CourseDTO-objekter som skal lagres
     */
    @Override
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

    /**
     * Returnerer standard filbane for en gitt fil i brukerens hjemmemappe under .bithub-katalogen.
     *
     * @param fileName navnet på filen som skal hentes bane til
     * @return et Path-objekt som peker til filen i ~/.bithub/fileName
     */
    public static Path getDefaultPath(String fileName) {
        return Path.of(System.getProperty("user.home"), ".bithub", fileName);
    }
}