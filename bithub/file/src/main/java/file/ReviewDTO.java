package file;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Dataoverføringsobjekt for en anmeldelse.
 *
 * <p>Brukes ved serialisering og deserialisering med Jackson. Verdiene valideres ved konvertering
 * til domeneklassen Review.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@JsonPropertyOrder({ "reviewerName", "rating", "comment" })
public class ReviewDTO {

    private String reviewerName;
    private int rating;
    private String comment;

    /**
     * Oppretter et tomt ReviewDTO-objekt.
     *
     * <p>Kreves av Jackson ved deserialisering.
     */
    public ReviewDTO() {
    }

    /**
     * Oppretter lagringsdata for en anmeldelse.
     *
     * @param reviewerName navnet på anmelderen
     * @param rating       vurderingen
     * @param comment      kommentaren
     */
    public ReviewDTO(String reviewerName, int rating, String comment) {
        this.reviewerName = reviewerName;
        this.rating = rating;
        this.comment = comment;
    }

    /**
     * Henter navnet på anmelderen.
     *
     * @return navnet på anmelderen
     */
    public String getReviewerName() {
        return reviewerName;
    }

    /**
     * Setter navnet på anmelderen.
     *
     * @param reviewerName navnet som skal lagres
     */
    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    /**
     * Henter vurderingen.
     *
     * @return vurderingen
     */
    public int getRating() {
        return rating;
    }

    /**
     * Setter vurderingen.
     *
     * @param rating vurderingen som skal lagres
     */
    public void setRating(int rating) {
        this.rating = rating;
    }

    /**
     * Henter kommentaren.
     *
     * @return kommentaren
     */
    public String getComment() {
        return comment;
    }

    /**
     * Setter kommentaren.
     *
     * @param comment kommentaren som skal lagres
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
}