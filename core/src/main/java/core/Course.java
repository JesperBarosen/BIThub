package core;

import java.util.ArrayList;
import java.util.List;

public class Course {
  private String courseCode;
  private String courseName;
  private List<Review> reviews;

  public Course(String courseCode, String courseName) {
    if (courseCode == null || courseCode.isEmpty()) {
      throw new IllegalArgumentException("Course code cannot be null or empty.");
    }
    if (courseName == null || courseName.isEmpty()) {
      throw new IllegalArgumentException("Course name cannot be null or empty.");
    }
    this.courseCode = courseCode;
    this.courseName = courseName;
    this.reviews = new ArrayList<>();
  }

  public String getCoursecode() {
    return courseCode;
  }

  public String getCoursename() {
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
