package rest.model;

import core.Course;
import core.Review;

/**
 * En anmeldelse slik eieren selv ser den. I motsetning til {@link ReviewResponse} viser den om
 * anmeldelsen er anonym, og hvilket emne den hører til.
 *
 * @param id         den unike id-en til anmeldelsen
 * @param courseCode emnekoden
 * @param courseName navnet på emnet
 * @param rating     vurderingen fra 1-5
 * @param comment    kommentaren til anmeldelsen
 * @param anonymous  om anmeldelsen vises som anonym for andre eller ikke
 */
public record OwnReviewResponse(String id, String courseCode, String courseName, int rating,
        String comment, boolean anonymous) {

    /**
     * Lager svaret for en av brukerens egne anmeldelser.
     *
     * @param course emnet anmeldelsen hører til
     * @param review anmeldelsen
     * @return anmeldelsen slik eieren ser den
     */
    public static OwnReviewResponse from(Course course, Review review) {
        return new OwnReviewResponse(review.getId(), course.getCourseCode(),
                course.getCourseName(), review.getRating(), review.getComment(),
                review.isAnonymous());
    }
}
