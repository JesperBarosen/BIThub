package core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 * Testklasse for Review-klassen. Verifiserer at vurderinger opprettes korrekt
 * og at ugyldige navn og vurderinger blir avvist.
 */
class ReviewTest {

    /**
     * Verifiserer at konstruktøren setter alle feltene korrekt.
     */
    @Test
    void constructorSetsFieldsCorrectly() {
        Review review = new Review("Ola Nordmann", 3, "Fantastisk");
        assertEquals("Ola Nordmann", review.getReviewerName());
        assertEquals(3, review.getRating());
        assertEquals("Fantastisk", review.getComment());
    }

    /**
     * Verifiserer at null som kommentar gir en tom streng.
     */
    @Test
    void constructorAllowsNoComment() {
        Review review = new Review("Ola Nordmann", 3, null);
        assertEquals("", review.getComment());
    }

    /**
     * Verifiserer at grenseverdiene 1 og 5 er gyldige vurderinger.
     */
    @Test
    void constructorAllowsBoundaryRatings() {
        Review lowest = new Review("Ola Nordmann", 1, null);
        Review highest = new Review("Ola Nordmann", 5, null);
        assertEquals(1, lowest.getRating());
        assertEquals(5, highest.getRating());
    }

    /**
     * Verifiserer at en vurdering under minimumsverdien kaster
     * IllegalArgumentException.
     */
    @Test
    void constructorThrowsWhenRatingBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new Review("Ola Nordmann", 0, "Kommentar"));
    }

    /**
     * Verifiserer at en vurdering over maksimumsverdien kaster
     * IllegalArgumentException.
     */
    @Test
    void constructorThrowsWhenRatingAboveMax() {
        assertThrows(IllegalArgumentException.class, () -> new Review("Ola Nordmann", 6, "Kommentar"));
    }

    /**
     * Verifiserer at null som navn på anmelderen kaster
     * IllegalArgumentException.
     */
    @Test
    void constructorThrowsWhenReviewerNameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Review(null, 3, "Kommentar"));
    }

    /**
     * Verifiserer at et blankt navn på anmelderen kaster
     * IllegalArgumentException.
     */
    @Test
    void constructorThrowsWhenReviewerNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Review(" ", 3, "Kommentar"));
    }

    /**
     * Verifiserer at et tomt navn på anmelderen kaster
     * IllegalArgumentException.
     */
    @Test
    void constructorThrowsWhenReviewerNameIsEmpty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Review("", 3, "Kommentar"));
    }
}
