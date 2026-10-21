package ui;

import core.Course;
import core.Review;
import core.ReviewRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Falsk lagring for ui-testene som holder emnene i minnet.
 *
 * <p>ui avhenger ikke av file, så testene kan ikke bruke {@code ReviewHandler}. Denne klassen
 * oppfører seg som lagringen på serveren: emnekoder sammenlignes uten å skille store og små
 * bokstaver, emnene beholder rekkefølgen de ble lagt inn i, og et nytt emne opprettes når det får
 * sin første anmeldelse.
 */
final class InMemoryReviewRepository implements ReviewRepository {

    private final Map<String, Course> courses = new LinkedHashMap<>();

    @Override
    public void addReview(String courseCode, String courseName, Review review) {
        courses.computeIfAbsent(key(courseCode), key -> new Course(courseCode, courseName))
                .addReview(review);
    }

    @Override
    public Optional<Course> findCourse(String courseCode) {
        if (courseCode == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(courses.get(key(courseCode)))
                .map(InMemoryReviewRepository::copy);
    }

    @Override
    public List<Course> findAllCourses() {
        List<Course> result = new ArrayList<>();
        for (Course course : courses.values()) {
            result.add(copy(course));
        }
        return result;
    }

    @Override
    public void addCoursesIfMissing(List<Course> newCourses) {
        for (Course course : newCourses) {
            courses.putIfAbsent(key(course.getCourseCode()),
                    new Course(course.getCourseCode(), course.getCourseName()));
        }
    }

    /**
     * Lager nøkkelen et emne lagres under, slik at «it1901» og «IT1901» er samme emne.
     *
     * @param courseCode emnekoden
     * @return emnekoden uten mellomrom rundt og med store bokstaver
     */
    private static String key(String courseCode) {
        return courseCode.strip().toUpperCase(Locale.ROOT);
    }

    /**
     * Lager en kopi av emnet, slik at testene ikke kan endre det som ligger i lagringen.
     *
     * @param course emnet som skal kopieres
     * @return et nytt emne med de samme anmeldelsene
     */
    private static Course copy(Course course) {
        Course copy = new Course(course.getCourseCode(), course.getCourseName());
        course.getReviews().forEach(copy::addReview);
        return copy;
    }
}