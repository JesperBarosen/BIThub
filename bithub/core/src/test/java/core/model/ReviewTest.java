package core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testklasse for {@link Review}-klassen.
 *
 * <p>Verifiserer at anmeldelser opprettes korrekt med id, eier og anonymitet, at ugyldige
 * verdier avvises, og at forfattertype og visningsnavn blir riktige.
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
     * Verifiserer at en vurdering under minimumsverdien kaster IllegalArgumentException.
     */
    @Test
    @DisplayName("Skal kaste unntak når rating er under 1")
    void testConstructorThrowsWhenRatingBelowMinimum() {
        assertThrows(IllegalArgumentException.class,
                () -> new Review("Ola Nordmann", 0, "Kommentar"));
    }

    /**
     * Verifiserer at en vurdering over maksimumsverdien kaster IllegalArgumentException.
     */
    @Test
    @DisplayName("Skal kaste unntak når rating er over 5")
    void testConstructorThrowsWhenRatingAboveMax() {
        assertThrows(IllegalArgumentException.class,
                () -> new Review("Ola Nordmann", 6, "Kommentar"));
    }

    /**
     * Verifiserer at null som navn på anmelderen kaster IllegalArgumentException.
     */
    @Test
    @DisplayName("Skal kaste unntak når anmelders navn er null")
    void testConstructorThrowsWhenReviewerNameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Review(null, 3, "Kommentar"));
    }

    /**
     * Verifiserer at et blankt navn på anmelderen kaster IllegalArgumentException.
     */
    @Test
    @DisplayName("Skal kaste unntak når anmelders navn er blankt")
    void testConstructorThrowsWhenReviewerNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Review(" ", 3, "Kommentar"));
    }

    /**
     * Verifiserer at et tomt navn på anmelderen kaster IllegalArgumentException.
     */
    @Test
    @DisplayName("Skal kaste unntak når anmelders navn er tomt")
    void testConstructorThrowsWhenReviewerNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new Review("", 3, "Kommentar"));
    }

    /**
     * Verifiserer at en ny anmeldelse får en id automatisk.
     */
    @Test
    @DisplayName("Skal gi ny anmeldelse en id automatisk")
    void testConstructorGeneratesId() {
        Review review = new Review("Ola Nordmann", 3, "Kommentar");
        assertNotNull(review.getId());
        assertFalse(review.getId().isBlank());
    }

    /**
     * Verifiserer at to nye anmeldelser får forskjellige id-er.
     */
    @Test
    @DisplayName("Skal gi to nye anmeldelser forskjellige id-er")
    void testConstructorGeneratesUniqueIds() {
        Review first = new Review("Ola Nordmann", 3, "Kommentar");
        Review second = new Review("Ola Nordmann", 3, "Kommentar");
        assertNotEquals(first.getId(), second.getId());
    }

    /**
     * Verifiserer at en anmeldelse fra den korte konstruktøren ikke har eier, ikke er anonym og får
     * typen GUEST.
     */
    @Test
    @DisplayName("Skal opprette anmeldelse uten eier som ikke er anonym")
    void testConstructorCreatesReviewWithoutOwner() {
        Review review = new Review("Ola Nordmann", 3, "Kommentar");
        assertNull(review.getOwnerEmail());
        assertFalse(review.isAnonymous());
        assertEquals(AuthorType.GUEST, review.getAuthorType());
    }

    /**
     * Verifiserer at den lange konstruktøren beholder alle verdiene som sendes inn.
     */
    @Test
    @DisplayName("Skal beholde id, eier og anonymitet fra den lange konstruktøren")
    void testFullConstructorKeepsAllValues() {
        Review review = new Review("abc-123", "ola@ntnu.no", "Ola Nordmann", true, 4, "Bra fag");
        assertEquals("abc-123", review.getId());
        assertEquals("ola@ntnu.no", review.getOwnerEmail());
        assertEquals("Ola Nordmann", review.getReviewerName());
        assertTrue(review.isAnonymous());
        assertEquals(4, review.getRating());
        assertEquals("Bra fag", review.getComment());
    }

    /**
     * Verifiserer at null som id kaster IllegalArgumentException.
     */
    @Test
    @DisplayName("Skal kaste unntak når id er null")
    void testFullConstructorThrowsWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new Review(null, "ola@ntnu.no", "Ola Nordmann", false, 3, "Kommentar"));
    }

    /**
     * Verifiserer at en blank id kaster IllegalArgumentException.
     */
    @Test
    @DisplayName("Skal kaste unntak når id er blank")
    void testFullConstructorThrowsWhenIdIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new Review(" ", "ola@ntnu.no", "Ola Nordmann", false, 3, "Kommentar"));
    }

    /**
     * Verifiserer at en anmeldelse uten eier ikke kan være anonym.
     */
    @Test
    @DisplayName("Skal kaste unntak når anmeldelsen er anonym uten eier")
    void testFullConstructorThrowsWhenAnonymousWithoutOwner() {
        assertThrows(IllegalArgumentException.class,
                () -> new Review("123-abc", null, "Ola Nordmann", true, 3, "Kommentar"));
    }

    /**
     * Verifiserer at en anmeldelse med eier som ikke er anonym får typen USER.
     */
    @Test
    @DisplayName("Skal gi typen USER når anmeldelsen har eier og ikke er anonym")
    void testAuthorTypeIsUser() {
        Review review = new Review("abc-123", "ola@ntnu.no", "Ola Nordmann", false, 3, "Kommentar");
        assertEquals(AuthorType.USER, review.getAuthorType());
    }

    /**
     * Verifiserer at en anmeldelse med eier som er anonym får typen ANONYMOUS.
     */
    @Test
    @DisplayName("Skal gi typen ANONYMOUS når anmeldelsen har eier og er anonym")
    void testAuthorTypeIsAnonymous() {
        Review review = new Review("abc-123", "ola@ntnu.no", "Ola Nordmann", true, 3, "Kommentar");
        assertEquals(AuthorType.ANONYMOUS, review.getAuthorType());
    }

    /**
     * Verifiserer at en anonym anmeldelse vises som «Anonym», og ikke med navn eller e-post.
     */
    @Test
    @DisplayName("Skal vise «Anonym» når anmeldelsen er anonym")
    void testDisplayNameIsAnonymousWhenAnonymous() {
        Review review = new Review("abc-123", "ola@ntnu.no", "Ola Nordmann", true, 3, "Kommentar");
        assertEquals("Anonym", review.getDisplayName());
    }

    /**
     * Verifiserer at en anmeldelse med eier som ikke er anonym vises med navnet til anmelderen.
     */
    @Test
    @DisplayName("Skal vise navnet når anmeldelsen har eier og ikke er anonym")
    void testDisplayNameIsReviewerNameForUser() {
        Review review = new Review("abc-123", "ola@ntnu.no", "Ola Nordmann", false, 3, "Kommentar");
        assertEquals("Ola Nordmann", review.getDisplayName());
    }

    /**
     * Verifiserer at en anmeldelse uten eier vises med navnet som er lagret.
     */
    @Test
    @DisplayName("Skal vise lagret navn når anmeldelsen ikke har eier")
    void testDisplayNameIsReviewerNameWithoutOwner() {
        Review review = new Review("Student", 3, "Kommentar");
        assertEquals("Student", review.getDisplayName());
    }

}
