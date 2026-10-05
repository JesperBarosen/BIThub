package core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testklasse for {@link Review}-klassen.
 *
 * <p>Verifiserer at anmeldelser opprettes korrekt, og at validering av
 * navn og rating avviser ugyldige verdier.
 */
class ReviewTest {
    /**
     * Verifiserer at parametrisert konstruktør setter alle feltene korrekt.
     */
    @Test
    @DisplayName("Skal opprette Review korrekt med gyldige parametere")
    void testConstructorSetsFieldsCorrectly() {
        Review review = new Review("Ola Nordmann", 3, "Fantastisk");
        assertEquals("Ola Nordmann", review.getReviewerName());
        assertEquals(3, review.getRating());
        assertEquals("Fantastisk", review.getComment());
    }

    /**
     * Verifiserer at null som kommentar blir omgjort til en tom streng.
     */
    @Test
    @DisplayName("Skal erstatte null-kommentar med tom streng i konstruktør")
    void testConstructorAllowsNoComment() {
        Review review = new Review("Ola Nordmann", 3, null);
        assertEquals("", review.getComment());
    }

    /**
     * Verifiserer at grenseverdiene 1 og 5 er gyldige vurderinger.
     */
    @Test
    @DisplayName("Skal godkjenne rating på grenseverdiene 1 og 5")
    void testConstructorAllowsBoundaryRatings() {
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
    @DisplayName("Skal kaste unntak når rating er under 1")
    void testConstructorThrowsWhenRatingBelowMinimum() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Review("Ola Nordmann", 0, "Kommentar"));
    }

    /**
     * Verifiserer at en vurdering over maksimumsverdien kaster
     * IllegalArgumentException.
     */
    @Test
    @DisplayName("Skal kaste unntak når rating er over 5")
    void testConstructorThrowsWhenRatingAboveMax() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Review("Ola Nordmann", 6, "Kommentar"));
    }

    /**
     * Verifiserer at null som navn på anmelderen kaster
     * IllegalArgumentException.
     */
    @Test
    @DisplayName("Skal kaste unntak når anmelders navn er null")
    void testConstructorThrowsWhenReviewerNameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Review(null, 3, "Kommentar"));
    }

    /**
     * Verifiserer at et blankt navn på anmelderen kaster
     * IllegalArgumentException.
     */
    @Test
    @DisplayName("Skal kaste unntak når anmelders navn er blankt")
    void testConstructorThrowsWhenReviewerNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Review(" ", 3, "Kommentar"));
    }

    /**
     * Verifiserer at et tomt navn på anmelderen kaster
     * IllegalArgumentException.
     */
    @Test
    @DisplayName("Skal kaste unntak når anmelders navn er tomt")
    void testConstructorThrowsWhenReviewerNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new Review("", 3, "Kommentar"));
    }
}
