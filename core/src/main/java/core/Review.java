package core;

public class Review {
  private String reviewerName;
  private int rating;
  private String comment;

  public Review(String reviewerName, int rating, String comment) {

    if (rating < 1 || rating > 5) {
      throw new IllegalArgumentException("Rating must be between 1 and 5.");
    }

    if (reviewerName == null || reviewerName.isEmpty()) {
      throw new IllegalArgumentException("Reviewer name cannot be null or empty.");
    }

    this.reviewerName = reviewerName;
    this.rating = rating;
    this.comment = comment;
  }

  public String getReviewerName() {
    return reviewerName;
  }

  public int getRating() {
    return rating;
  }

  public String getComment() {
    return comment;
  }
}
