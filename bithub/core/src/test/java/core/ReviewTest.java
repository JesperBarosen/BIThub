package core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testklasse for {@link Review}-klassen.
 *
 * <p>Verifiserer at vurderinger opprettes og endres korrekt, samt at validering av
 * navn og rating avviser ugyldige verdier.
 */
class ReviewTest {

    /**
     * Verifiserer at den tomme konstruktøren oppretter et objekt med
     * tomme/standard verdier.
     */
    @Test
    @DisplayName("Skal opprette tomt Review-objekt ved bruk av tom konstruktør")
    void testNoArgsConstructor() {
        Review review = new Review();
        assertNull(review.getReviewerName());
        assertEquals(0, review.getRating());
        assertNull(review.getComment());
    }

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

    /**
     * Verifiserer at alle setter-metodene oppdaterer feltene i objektet
     * korrekt.
     */
    @Test
    @DisplayName("Skal oppdatere felter korrekt ved bruk av settere")
    void testSettersWorkCorrectly() {
        Review review = new Review();

        review.setReviewerName("Kari Nordmann");
        assertEquals("Kari Nordmann", review.getReviewerName());

        review.setRating(4);
        assertEquals(4, review.getRating());

        review.setComment("Ny kommentar");
        assertEquals("Ny kommentar", review.getComment());
    }
}
