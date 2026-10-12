package rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.Course;
import core.Review;
import core.ReviewService;
import file.ReviewHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Tester serveroppstart og koblingen til lagring uten å bruke vanlige brukerdata.
 */
class RestApplicationTest {

    /**
     * Starter serveren og kontrollerer at tjenesten skriver til valgt testfil.
     *
     * @param tempDirectory midlertidig mappe som ryddes av JUnit etter testen
     */
    @Test
    void startsServerAndSavesReviewToConfiguredFile(@TempDir Path tempDirectory) {
        Path reviewsFile = tempDirectory.resolve("courses.json");

        // Port 0 lar serveren velge en ledig port, også når appen kjører fra før.
        try (ConfigurableApplicationContext context = SpringApplication.run(
                RestApplication.class,
                "--server.port=0",
                "--bithub.reviews-file=" + reviewsFile)) {
            WebServerApplicationContext serverContext =
                    assertInstanceOf(WebServerApplicationContext.class, context);
            assertNotNull(serverContext.getWebServer());
            assertTrue(serverContext.getWebServer().getPort() > 0);

            ReviewService service = context.getBean(ReviewService.class);
            service.submitReview("IT1901", "Prosjektarbeid", "Testbruker", "4", "Bra fag");

            assertTrue(Files.isRegularFile(reviewsFile));
            // En ny leser bekrefter at anmeldelsen faktisk finnes i testfilen.
            Course savedCourse = new ReviewHandler(reviewsFile)
                    .findCourse("IT1901").orElseThrow();
            assertEquals("Prosjektarbeid", savedCourse.getCourseName());
            assertEquals(1, savedCourse.getReviews().size());
            Review savedReview = savedCourse.getReviews().get(0);
            assertEquals("Testbruker", savedReview.getReviewerName());
            assertEquals(4, savedReview.getRating());
            assertEquals("Bra fag", savedReview.getComment());
        }
    }
}
