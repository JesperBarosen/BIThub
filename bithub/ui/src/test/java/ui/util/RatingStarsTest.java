package ui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.stage.Stage;
import org.controlsfx.control.Rating;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Tester at stjernene viser vurderingen og ikke kan endres av brukeren.
 */
class RatingStarsTest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        // Starter bare JavaFX, slik at stjernene kan lages i testene.
    }

    @Test
    void showsRatingOutOfFiveStars() {
        Rating stars = RatingStars.readOnly(4);
        assertEquals(5, stars.getMax());
        assertEquals(4.0, stars.getRating());
    }

    @Test
    void showsPartialRating() {
        assertEquals(3.5, RatingStars.readOnly(3.5).getRating());
    }

    @Test
    void starsCannotBeChangedByUser() {
        Rating stars = RatingStars.readOnly(2);
        assertTrue(stars.isMouseTransparent());
        assertFalse(stars.isFocusTraversable());
        assertTrue(stars.getStyleClass().contains("vurdering-stjerner"));
    }
}