package rest.model;

import core.Course;
import java.util.List;

/**
 * Ett emne med gjennomsnitt og alle anmeldelsene.
 *
 * @param code          emnekoden
 * @param name          navnet på emnet
 * @param averageRating gjennomsnittlig vurdering, eller 0.0 uten anmeldelser
 * @param reviews       anmeldelsene slik de vises offentlig
 */
public record CourseDetailsResponse(String code, String name, double averageRating,
        List<ReviewResponse> reviews) {

    /**
     * Lager en kopi av listen, slik at svaret ikke kan endres utenfra.
     */
    public CourseDetailsResponse {
        reviews = List.copyOf(reviews);
    }

    /**
     * Lager svaret for et emne med anmeldelser.
     *
     * @param course emnet fra core
     * @return emnet med anmeldelser slik det vises i REST-API-et
     */
    public static CourseDetailsResponse from(Course course) {
        List<ReviewResponse> reviews = course.getReviews().stream()
                .map(ReviewResponse::from)
                .toList();
        return new CourseDetailsResponse(course.getCourseCode(), course.getCourseName(),
                course.getAverageRating(), reviews);
    }
}