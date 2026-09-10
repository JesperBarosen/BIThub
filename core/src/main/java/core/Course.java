package core;

import java.util.ArrayList;
import java.util.List;

public class Course {
  private String courseCode;
  private String courseName;
  private List<Review> reviews;

  public Course(String courseCode, String courseName) {
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
    reviews.add(review);
  }

  public List<Review> getReviews() {
    return reviews;
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
