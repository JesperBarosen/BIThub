package file;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Dataoverføringsobjekt for en anmeldelse.
 *
 * <p>Brukes ved serialisering og deserialisering med Jackson. Verdiene valideres ved konvertering
 * til domeneklassen Review.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@JsonPropertyOrder({ "id", "ownerEmail", "reviewerName", "anonymous", "rating", "comment" })
public class ReviewDTO {

    private String id;
    private String ownerEmail;
    private String reviewerName;
    private boolean anonymous;
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
     * Oppretter lagringsdata for en anmeldelse uten id, eier eller anonymitet.
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
     * Oppretter lagringsdata for en anmeldelse med alle felt.
     *
     * @param id           id-en til anmeldelsen
     * @param ownerEmail   e-posten til eieren, eller null hvis anmeldelsen ikke har en eier
     * @param reviewerName navnet på anmelderen
     * @param anonymous    om anmeldelsen er anonym
     * @param rating       vurderingen
     * @param comment      kommentaren
     */
    public ReviewDTO(String id, String ownerEmail, String reviewerName, boolean anonymous,
            int rating, String comment) {
        this.id = id;
        this.ownerEmail = ownerEmail;
        this.reviewerName = reviewerName;
        this.anonymous = anonymous;
        this.rating = rating;
        this.comment = comment;
    }

    /**
     * Henter id-en til anmeldelsen.
     *
     * @return id-en, eller null hvis den ikke er satt
     */
    public String getId() {
        return id;
    }

    /**
     * Setter id-en til anmeldelsen.
     *
     * @param id id-en som skal lagres
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Henter e-posten til eieren.
     *
     * @return e-posten til eieren, eller null hvis anmeldelsen ikke har en eier
     */
    public String getOwnerEmail() {
        return ownerEmail;
    }

    /**
     * Setter e-posten til eieren.
     *
     * @param ownerEmail e-posten som skal lagres
     */
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
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
     * Sjekker om anmeldelsen er anonym.
     *
     * @return true hvis anmeldelsen er anonym, ellers false
     */
    public boolean isAnonymous() {
        return anonymous;
    }

    /**
     * Setter om anmeldelsen er anonym.
     *
     * @param anonymous om anmeldelsen er anonym
     */
    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
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