package rest.model;

import core.Course;

/**
 * Et emne i listen over emner, uten anmeldelsene.
 *
 * @param code          emnekoden
 * @param name          navnet på emnet
 * @param averageRating gjennomsnittlig vurdering, eller 0.0 hvis ingen anmeldelser
 * @param reviewCount   antall anmeldelser
 */
public record CourseResponse(String code, String name, double averageRating, int reviewCount) {

    /**
     * Lager svaret fra et emne.
     *
     * @param course emnet fra core
     * @return emnet slik det vises i listen
     */
    public static CourseResponse from(Course course) {
        return new CourseResponse(course.getCourseCode(), course.getCourseName(),
                course.getAverageRating(), course.getReviews().size());
    }
}