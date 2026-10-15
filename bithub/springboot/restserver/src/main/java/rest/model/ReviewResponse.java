package rest.model;

import core.Review;

/**
 * En anmeldelse slik den vises offentlig i REST-API-et. Eieren er aldri med i svaret, slik at
 * ingen kan se hvem som skrev en anonym anmeldelse.
 *
 * @param displayName navnet som vises: brukerens navn, «Anonym» eller «Gjest»
 * @param authorType  hvem som skrev anmeldelsen
 * @param rating      vurderingen fra 1 til 5
 * @param comment     kommentaren, kan være tom
 */
public record ReviewResponse(String displayName, AuthorType authorType, int rating,
    String comment) {

    /**
     * Navnet som vises på gjesteanmeldelser.
     */
    public static final String GUEST_NAME = "Gjest";

    /**
     * Lager svaret for en anmeldelse. Anmeldelser har ingen eier før #142, så alle vises foreløpig
     * som gjesteanmeldelser.
     *
     * @param review anmeldelsen fra core
     * @return anmeldelsen slik den vises i REST-API-et
     */
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(GUEST_NAME, AuthorType.GUEST, review.getRating(),
                review.getComment());
    }
}