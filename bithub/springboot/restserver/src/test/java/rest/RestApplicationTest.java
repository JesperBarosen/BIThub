package rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.Course;
import core.Review;
import core.ReviewService;
import core.User;
import core.UserRepository;
import core.UserService;
import file.ReviewHandler;
import file.UserHandler;
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
        Path usersFile = tempDirectory.resolve("users.json");

        // Port 0 lar serveren velge en ledig port, også når appen kjører fra før.
        try (ConfigurableApplicationContext context = SpringApplication.run(RestApplication.class,
                "--server.port=0", "--bithub.reviews-file=" + reviewsFile,
                "--bithub.users-file=" + usersFile)) {
            WebServerApplicationContext serverContext = assertInstanceOf(
                    WebServerApplicationContext.class, context);
            assertNotNull(serverContext.getWebServer());
            assertTrue(serverContext.getWebServer().getPort() > 0);

            ReviewService service = context.getBean(ReviewService.class);
            service.submitReview("IT1901", "Informatikk prosjektarbeid I", "Testbruker", "4",
                    "Bra fag");

            assertTrue(Files.isRegularFile(reviewsFile));
            // En ny leser bekrefter at anmeldelsen faktisk finnes i testfilen.
            Course savedCourse = new ReviewHandler(reviewsFile).findCourse("IT1901").orElseThrow();
            assertEquals("Informatikk prosjektarbeid I", savedCourse.getCourseName());
            assertEquals(1, savedCourse.getReviews().size());
            Review savedReview = savedCourse.getReviews().get(0);
            assertEquals("Testbruker", savedReview.getReviewerName());
            assertEquals(4, savedReview.getRating());
            assertEquals("Bra fag", savedReview.getComment());
        }

    }

    /**
     * Kontrollerer at brukertjenesten bruker fillagringen og lagrer i valgt testfil.
     *
     * @param tempDirectory midlertidig mappe som ryddes av JUnit etter testen
     */
    @Test
    void registersUserAndSavesToConfiguredFile(@TempDir Path tempDirectory) {
        Path reviewsFile = tempDirectory.resolve("courses.json");
        Path usersFile = tempDirectory.resolve("users.json");

        try (ConfigurableApplicationContext context = SpringApplication.run(RestApplication.class,
                "--server.port=0", "--bithub.reviews-file=" + reviewsFile,
                "--bithub.users-file=" + usersFile)) {
            UserRepository repository = context.getBean(UserRepository.class);
            assertInstanceOf(UserHandler.class, repository);

            UserService service = context.getBean(UserService.class);
            final User registeredUser = service.register("Testbruker", "test@ntnu.no",
                    "Testpassord123");

            assertTrue(Files.isRegularFile(usersFile));

            // En ny leser bekrefter at brukeren faktisk er lagret i testfilen.
            UserRepository savedRepository = new UserHandler(usersFile);
            User savedUser = savedRepository.findByEmail("test@ntnu.no").orElseThrow();

            assertEquals("Testbruker", savedUser.getName());
            assertEquals("test@ntnu.no", savedUser.getEmail());
            assertEquals(registeredUser.getPasswordHash(), savedUser.getPasswordHash());
            assertEquals(registeredUser.getSalt(), savedUser.getSalt());

            // Innlogging gjennom en ny tjeneste kontrollerer de lagrede passorddataene.
            UserService reloadedService = new UserService(savedRepository);
            assertTrue(reloadedService.login("test@ntnu.no", "Testpassord123").isPresent());
        }
    }
}
