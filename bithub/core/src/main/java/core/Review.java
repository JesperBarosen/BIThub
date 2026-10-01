package core;

public final class Review {

    private String reviewerName;
    private int rating;
    private String comment;

    // Tom konstruktør for Jackson
    public Review() {
    }

    public Review(String reviewerName, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        if (reviewerName == null || reviewerName.isBlank()) {
            throw new IllegalArgumentException("Reviewer name cannot be null or empty.");
        }

        if (comment == null) {
            comment = "";
        }

        this.reviewerName = reviewerName;
        this.rating = rating;
        this.comment = comment;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
