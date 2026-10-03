package core;

import java.util.ArrayList;
import java.util.List;

/**
 * Representerer et emne med tilhørende anmeldelser.
 */
public final class Course {

    private String courseCode;
    private String courseName;
    private List<Review> reviews;

    /**
     * Oppretter et emne uten anmeldelser.
     *
     * @param courseCode emnekoden, kan ikke være null eller blank
     * @param courseName emnenavnet, kan ikke være null eller blank
     * @throws IllegalArgumentException hvis emnekoden eller emnenavnet er ugyldig
     */
    public Course(String courseCode, String courseName) {
        if (courseCode == null || courseCode.isBlank()) {
            throw new IllegalArgumentException("Course code cannot be null or empty.");
        }
        if (courseName == null || courseName.isBlank()) {
            throw new IllegalArgumentException("Course name cannot be null or empty.");
        }
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.reviews = new ArrayList<>();
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    /**
     * Legger til en anmeldelse av emnet.
     *
     * @param review anmeldelsen som skal legges til
     * @throws IllegalArgumentException hvis anmeldelsen er null
     */
    public void addReview(Review review) {
        if (review == null) {
            throw new IllegalArgumentException("Review cannot be null.");
        }
        reviews.add(review);
    }

    public List<Review> getReviews() {
        return List.copyOf(reviews);
    }

    /**
     * Beregner gjennomsnittlig vurdering for emnet.
     *
     * @return gjennomsnittlig vurdering, eller 0.0 hvis emnet mangler anmeldelser
     */
    public double getAverageRating() {
        if (reviews.isEmpty()) {
            return 0.0;
        }
        double totalRating = 0.0;
        for (Review review : reviews) {
            totalRating += review.getRating();
        }
        return totalRating / reviews.size();
    }
}
