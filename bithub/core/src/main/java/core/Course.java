package core;

import java.util.ArrayList;
import java.util.List;

//representerer et emne og kursets anmeldelser
public final class Course {

    private String courseCode;
    private String courseName;
    private List<Review> reviews;

    public Course(String courseCode, String courseName) {
        if (courseCode == null || courseCode.isBlank()) {
            throw new IllegalArgumentException("Course code cannot be null or empty.");
        }
        if (courseName == null || courseName.isBlank()) {
            throw new IllegalArgumentException("Course name cannot be null or empty.");
        }
        this.courseCode = courseCode;
        this.courseName = courseName;
        /*Til senere utvikling: Burde denne arrayen hente reviews fra reviews.csv? */
        this.reviews = new ArrayList<>();
        /*Til senere utvikling: Burde denne arrayen hente reviews fra reviews.csv? */
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void addReview(Review review) {
        if (review == null) {
            throw new IllegalArgumentException("Review cannot be null.");
        }
        reviews.add(review);
    }

    public List<Review> getReviews() {
        return List.copyOf(reviews);
    }

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
