package ui.util;

import org.controlsfx.control.Rating;

/**
 * Lager stjerner som viser en vurdering uten at brukeren kan endre den.
 */
public final class RatingStars {

    private RatingStars() {
    }

    /**
     * Lager fem stjerner med vurderingen fylt inn. Delvise verdier, som 3.5, vises som en delvis
     * fylt stjerne. Stjernene kan ikke klikkes og får ikke fokus.
     *
     * @param rating vurderingen som skal vises, fra 0 til 5
     * @return stjernene med vurderingen
     */
    public static Rating readOnly(double rating) {
        Rating stars = new Rating(5);
        stars.setPartialRating(true);
        stars.setUpdateOnHover(false);
        stars.setRating(rating);
        stars.setMouseTransparent(true);
        stars.setFocusTraversable(false);
        stars.getStyleClass().add("vurdering-stjerner");
        return stars;
    }
}
