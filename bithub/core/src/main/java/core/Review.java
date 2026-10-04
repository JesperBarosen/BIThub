package core;

/**
 * Representerer en anmeldelse med navn, vurdering og kommentar.
 */
public final class Review {

    private final String reviewerName;
    private final int rating;
    private final String comment;

    /**
     * Oppretter en anmeldelse med navn, vurdering og kommentar.
     *
     * @param reviewerName navnet på anmelderen, kan ikke være null eller blankt
     * @param rating       vurderingen fra 1 til 5
     * @param comment      kommentaren, null behandles som en tom tekst
     * @throws IllegalArgumentException hvis navnet eller vurderingen er ugyldig
     */
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

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }
}
