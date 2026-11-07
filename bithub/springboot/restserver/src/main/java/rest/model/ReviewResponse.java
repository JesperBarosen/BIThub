package rest.model;

import core.model.AuthorType;
import core.model.Review;

// Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
/**
 * En anmeldelse slik den vises offentlig i REST-API-et. Eieren er aldri med i svaret, slik at ingen
 * kan se hvem som skrev en anonym anmeldelse.
 *
 * @param id          den unike id-en til anmeldelsen
 * @param displayName navnet som vises: brukerens navn, «Anonym» eller «Gjest»
 * @param authorType  hvem som skrev anmeldelsen
 * @param rating      vurderingen fra 1 til 5
 * @param comment     kommentaren, kan være tom
 */
public record ReviewResponse(String id, String displayName, AuthorType authorType, int rating,
        String comment) {

    /**
     * Navnet som vises på gjesteanmeldelser.
     */
    public static final String GUEST_NAME = "Gjest";

    /**
     * Lager svaret for en anmeldelse. Gjesteanmeldelser vises alltid med navnet «Gjest», siden
     * ingen innlogget bruker står bak dem.
     *
     * @param review anmeldelsen fra core
     * @return anmeldelsen slik den vises i REST-API-et
     */
    public static ReviewResponse from(Review review) {
        AuthorType authorType = review.getAuthorType();
        String displayName = authorType == AuthorType.GUEST ? GUEST_NAME : review.getDisplayName();
        return new ReviewResponse(review.getId(), displayName, authorType, review.getRating(),
                review.getComment());
    }
}