package core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

//Unit tester for Review
public class ReviewTest {

    //Verifiser at konstruktøren setter alle feltene korrekt
    @Test 
    void constructorSetsFieldsCorrectly(){ 
        Review review = new Review("Ola Nordmann", 3, "Fantastisk");
        assertEquals("William Reitan", review.getReviewerName());
        assertEquals(3, review.getRating());
        assertEquals("Amazing", review.getComment());
    }

    //Verifiser at null kommentar gir tom streng fra getComment
    @Test 
    void constructorAllowsNoComment(){
        Review review = new Review("Ola Nordmann", 3, null);
        assertEquals("", review.getComment());
     }

     //Verifiserer at grenseverdiene 1 og 5 er gyldige

     @Test 
    void constructorAllowsBoundaryRatings(){
        Review lowest = new Review("Ola Nordmann", 1, null);
        Review highest = new Review("Ola Nordmann", 5, null);
        assertEquals(1, lowest.getRating());
        assertEquals(5, highest.getRating());
     }

     //Verifiserer at en rating under 1 kaster IllegalArgumentexception

     @Test 
     void constructorThrowsWhenRatingBelowMinimum(){ 
        assertThrows(IllegalArgumentException.class, () -> new Review("Ola Nordmann", 0, "Kommentar"));
     }

     //Verifiserer at en rating over 5 kaster IllegalArgumentexception

     @Test 
     void constructorThrowsWhenRatingAboveMax(){ 
        assertThrows(IllegalArgumentException.class, () -> new Review("Ola Nordmann", 6, "Kommentar"));
     }

     //Verifiserer at null som reviewname kaster IllegalArgumentException

     @Test 
     void constructorThrowsWhenReviewerNameIsNull(){ 
        assertThrows(IllegalArgumentException.class, () -> new Review(null, 3, "Kommentar"));
     }

     //Verifiserer at blankt reviewname kaster IllegalArgumentException

     @Test 
     void constructorThrowsWhenReviewerNameIsBlank(){ 
        assertThrows(IllegalArgumentException.class, () -> new Review(" ", 3, "Kommentar"));
     }
}
