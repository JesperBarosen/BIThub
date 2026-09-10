package core;

import java.util.ArrayList;
import java.util.List;

public class Course {
  private String Coursecode;
  private String Coursename;
  private List<Review> reviews;

  public Course(String coursecode, String coursename) {
    this.Coursecode = coursecode;
    this.Coursename = coursename;
    this.reviews = new ArrayList<>();
  }

  public String getCoursecode() {
    return Coursecode;
  }

  public String getCoursename() {
    return Coursename;
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
