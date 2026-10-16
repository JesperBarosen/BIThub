package core;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Håndterer innsending av anmeldelser.
 *
 * <p>Klassen validerer det brukeren har skrevet inn og sender anmeldelsen videre til lagring. Den
 * kjenner bare grensesnittet {@link ReviewRepository}, slik at core ikke blir avhengig av hvordan
 * anmeldelsene lagres.
 */
public final class ReviewService {

    private final ReviewRepository reviewRepository;

    /**
     * Oppretter tjenesten med lagringen den skal bruke.
     *
     * @param reviewRepository lagring for anmeldelser
     * @throws NullPointerException hvis reviewRepository er null
     */
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = Objects.requireNonNull(reviewRepository);
    }

    /**
     * Validerer og lagrer en anmeldelse for et emne.
     *
     * @param courseCode   emnekoden til emnet
     * @param courseName   navnet på emnet
     * @param reviewerName navnet som skal vises på anmeldelsen
     * @param ratingText   vurderingen slik brukeren skrev den inn
     * @param comment      kommentaren til anmeldelsen, kan være tom
     * @return den lagrede anmeldelsen, med id
     * @throws IllegalArgumentException hvis emnet eller anmeldelsen er ugyldig
     */
    public Review submitReview(String courseCode, String courseName, String reviewerName,
            String ratingText, String comment) {
        int rating = parseRating(ratingText);
        Course course = new Course(courseCode, courseName);
        Review review = new Review(reviewerName, rating, comment == null ? "" : comment.trim());
        reviewRepository.addReview(course.getCourseCode(), course.getCourseName(), review);
        return review;
    }

    /**
     * Henter emnet med alle lagrede anmeldelser, slik at de kan vises.
     *
     * <p>Dersom ingen har anmeldt emnet ennå, returneres et emne uten anmeldelser, slik at
     * brukergrensesnittet alltid har noe å vise.
     *
     * @param course emnet som skal vises
     * @return emnet med lagrede anmeldelser, eller et emne uten anmeldelser
     * @throws IllegalArgumentException hvis emnet er null
     */
    public Course getCourseWithReviews(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }
        return reviewRepository.findCourse(course.getCourseCode())
                .orElseGet(() -> new Course(course.getCourseCode(), course.getCourseName()));
    }

    /**
     * Henter alle lagrede emner, også de uten anmeldelser.
     *
     * @return alle emnene med anmeldelsene sine
     */
    public List<Course> getAllCourses() {
        return reviewRepository.findAllCourses();
    }

    /**
     * Finner et lagret emne ut fra emnekoden, med alle anmeldelsene.
     *
     * @param courseCode emnekoden, store og små bokstaver skilles ikke
     * @return emnet, eller tom hvis emnet ikke finnes
     */
    public Optional<Course> findCourse(String courseCode) {
        return reviewRepository.findCourse(courseCode);
    }

    /**
     * Gjør om vurderingen fra tekst til et tall fra 1 til 5.
     *
     * @param ratingText vurderingen slik brukeren skrev den inn
     * @return vurderingen som tall
     * @throws IllegalArgumentException hvis teksten ikke er et tall fra 1 til 5
     */
    private static int parseRating(String ratingText) {
        if (ratingText == null || ratingText.isBlank()) {
            throw new IllegalArgumentException("Vurdering må fylles ut.");
        }
        int rating;
        try {
            rating = Integer.parseInt(ratingText.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Vurdering må være et gyldig tall.", e);
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Vurdering må være et tall fra 1 til 5.");
        }
        return rating;
    }
}