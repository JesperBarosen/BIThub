package core;

import java.util.Objects;

/**
 * Håndterer innsending av anmeldelser.
 *
 * <p>
 * Klassen validerer det brukeren har skrevet inn og sender anmeldelsen videre
 * til lagring. Den kjenner bare grensesnittet {@link ReviewRepository}, slik at
 * core ikke blir avhengig av hvordan anmeldelsene lagres.
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
     * @param courseCode emnekoden til emnet
     * @param courseName navnet på emnet
     * @param reviewerName navnet som skal vises på anmeldelsen
     * @param ratingText vurderingen slik brukeren skrev den inn
     * @param comment kommentaren til anmeldelsen, kan være tom
     * @throws IllegalArgumentException hvis emnet eller anmeldelsen er ugyldig
     */
    public void submitReview(String courseCode, String courseName, String reviewerName,
            String ratingText, String comment) {
        int rating = parseRating(ratingText);
        Course course = new Course(courseCode, courseName);
        Review review = new Review(reviewerName, rating, comment == null ? "" : comment.trim());
        reviewRepository.addReview(course.getCourseCode(), course.getCourseName(), review);
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
