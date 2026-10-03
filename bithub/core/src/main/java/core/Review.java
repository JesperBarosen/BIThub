package core;

/**
 * Representerer en anmeldelse med navn, vurdering og kommentar.
 */
public final class Review {

    private String reviewerName;
    private int rating;
    private String comment;

    /**
     * Oppretter en tom anmeldelse som Jackson kan fylle med lagrede data.
     */
    public Review() {
    }

    /**
     * Oppretter en anmeldelse med navn, vurdering og kommentar.
     *
     * @param reviewerName navnet på anmelderen, kan ikke være null eller blankt
     * @param rating vurderingen fra 1 til 5
     * @param comment kommentaren, null behandles som en tom tekst
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

    /**
     * Henter navnet på den som skrev anmeldelsen.
     *
     * @return navnet på anmelderen
     */
    public String getReviewerName() {
        return reviewerName;
    }

    /**
     * Setter navnet på den som skrev anmeldelsen.
     *
     * <p>Brukes av Jackson når anmeldelser leses fra fil.
     *
     * @param reviewerName navnet på anmelderen
     */
    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    /**
     * Henter vurderingen av emnet.
     *
     * @return vurderingen fra 1 til 5
     */
    public int getRating() {
        return rating;
    }

    /**
     * Setter vurderingen av emnet.
     *
     * <p>Brukes av Jackson når anmeldelser leses fra fil.
     *
     * @param rating vurderingen fra 1 til 5
     */
    public void setRating(int rating) {
        this.rating = rating;
    }

    /**
     * Henter kommentaren i anmeldelsen.
     *
     * @return kommentaren, eller en tom streng hvis det ikke er skrevet noen
     */
    public String getComment() {
        return comment;
    }

    /**
     * Setter kommentaren i anmeldelsen.
     *
     * <p>Brukes av Jackson når anmeldelser leses fra fil.
     *
     * @param comment kommentaren
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
}
