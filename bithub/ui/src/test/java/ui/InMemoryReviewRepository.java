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
import java.util.function.UnaryOperator;

/**
 * Falsk lagring for ui-testene som holder emnene i minnet.
 *
 * <p>ui avhenger ikke av file, så testene kan ikke bruke {@code ReviewHandler}. Denne klassen
 * oppfører seg som lagringen på serveren: emnekoder sammenlignes uten å skille store og små
 * bokstaver, emnene beholder rekkefølgen de ble lagt inn i, og et nytt emne opprettes når det får
 * sin første anmeldelse. Anmeldelser kan også endres og slettes.
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

    @Override
    public boolean updateReview(String reviewId, int rating, String comment, boolean anonymous) {
        return replaceReview(reviewId, review -> new Review(review.getId(), review.getOwnerEmail(),
                review.getReviewerName(), anonymous, rating, comment));
    }

    @Override
    public boolean deleteReview(String reviewId) {
        return replaceReview(reviewId, review -> null);
    }

    /**
     * Bytter ut anmeldelsen med id-en, eller fjerner den hvis {@code change} gir null. Course har
     * ingen metode for å fjerne anmeldelser, så emnet bygges opp på nytt.
     *
     * @param reviewId id-en til anmeldelsen
     * @param change   lager den nye anmeldelsen, eller null for å slette den
     * @return true hvis anmeldelsen fantes, ellers false
     */
    private boolean replaceReview(String reviewId, UnaryOperator<Review> change) {
        for (Map.Entry<String, Course> entry : courses.entrySet()) {
            Course course = entry.getValue();
            if (course.getReviews().stream().noneMatch(review -> review.getId().equals(reviewId))) {
                continue;
            }
            Course changed = new Course(course.getCourseCode(), course.getCourseName());
            for (Review review : course.getReviews()) {
                Review kept = review.getId().equals(reviewId) ? change.apply(review) : review;
                if (kept != null) {
                    changed.addReview(kept);
                }
            }
            entry.setValue(changed);
            return true;
        }
        return false;
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