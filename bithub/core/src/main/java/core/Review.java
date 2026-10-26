package core;

import java.util.UUID;

/**
 * Representerer en anmeldelse med id, eier, navn, anonymitet, vurdering og kommentar.
 */
public final class Review {

    private final String id;
    private final String ownerEmail;
    private final String reviewerName;
    private final boolean anonymous;
    private final int rating;
    private final String comment;

    /**
     * Oppretter en anmeldelse med alle felt satt.
     *
     * @param id           den unike id-en til anmeldelsen, kan ikke være null eller blank
     * @param ownerEmail   e-posten til eieren, eller null hvis anmeldelsen ikke har en eier
     * @param reviewerName navnet på anmelderen, kan ikke være null eller blankt
     * @param anonymous    om anmeldelsen er anonym
     * @param rating       vurderingen fra 1 til 5
     * @param comment      kommentaren, null behandles som en tom tekst
     * @throws IllegalArgumentException hvis id, navn eller vurdering er ugyldig, eller hvis
     *                                      anmeldelsen er anonym uten å ha en eier.
     */
    public Review(String id, String ownerEmail, String reviewerName, boolean anonymous, int rating,
            String comment) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id kan ikke være null eller tom.");
        }

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Vurderingen må være mellom 1 og 5.");
        }

        if (reviewerName == null || reviewerName.isBlank()) {
            throw new IllegalArgumentException(
                    "Navnet på anmelderen kan ikke være null eller tomt.");
        }

        if (ownerEmail == null && anonymous) {
            throw new IllegalArgumentException("En anmeldelse uten eier kan ikke være anonym.");
        }

        if (comment == null) {
            comment = "";
        }

        this.id = id;
        this.ownerEmail = ownerEmail;
        this.reviewerName = reviewerName;
        this.anonymous = anonymous;
        this.rating = rating;
        this.comment = comment;
    }

    /**
     * Oppretter en ny anmeldelse uten eier som ikke er anonym, og gir den en ny tilfeldig id. Slike
     * gjesteanmeldelser finnes bare fra release 2, siden nye anmeldelser krever innlogging. Brukes
     * derfor mest i tester. For anmeldelser som allerede har en id, for eksempel ved lesing fra
     * fil, brukes {@link #Review(String, String, String, boolean, int, String)}.
     *
     * @param reviewerName navnet på anmelderen, kan ikke være null eller blankt
     * @param rating       vurderingen fra 1 til 5
     * @param comment      kommentaren, null behandles som en tom tekst
     * @throws IllegalArgumentException hvis navnet eller vurderingen er ugyldig
     */
    public Review(String reviewerName, int rating, String comment) {
        this(UUID.randomUUID().toString(), null, reviewerName, false, rating, comment);
    }

    /**
     * Henter den unike id-en til anmeldelsen.
     *
     * @return id-en til anmeldelsen
     */
    public String getId() {
        return id;
    }

    /**
     * Henter e-posten til eieren av anmeldelsen.
     *
     * @return e-posten til eieren, eller null hvis anmeldelsen ikke har en eier
     */
    public String getOwnerEmail() {
        return ownerEmail;
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
     * Sjekker om anmeldelsen er anonym.
     *
     * @return true hvis anmeldelsen er anonym, ellers false
     */
    public boolean isAnonymous() {
        return anonymous;
    }

    /**
     * Finner hvilken type forfatter anmeldelsen har.
     *
     * @return GUEST om anmeldelsen ikke har en eier, ANONYMOUS om eieren har valgt å være anonym,
     *             ellers USER.
     */
    public AuthorType getAuthorType() {
        if (ownerEmail == null) {
            return AuthorType.GUEST;
        }
        if (anonymous) {
            return AuthorType.ANONYMOUS;
        }
        return AuthorType.USER;
    }

    /**
     * Finner navnet som skal vises i anmeldelsen. E-posten til eieren vises aldri.
     *
     * @return "Anonym" om anmeldelsen er anonym, ellers navnet til anmelderen.
     */
    public String getDisplayName() {
        if (getAuthorType() == AuthorType.ANONYMOUS) {
            return "Anonym";
        }
        return reviewerName;
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
     * Henter kommentaren i anmeldelsen.
     *
     * @return kommentaren, eller en tom streng hvis det ikke er skrevet noen
     */
    public String getComment() {
        return comment;
    }
}