package rest;

import core.DefaultCourses;
import core.ReviewRepository;
import core.ReviewService;
import file.ReviewHandler;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Starter REST-serveren for BIThub.
 */
@SpringBootApplication
public class RestApplication {

    /**
     * Starter Spring Boot med innebygd webserver.
     *
     * @param args argumenter fra kommandolinjen
     */
    public static void main(String[] args) {
        SpringApplication.run(RestApplication.class, args);
    }

    /**
     * Oppretter lagringen med konfigurert filsti og legger inn standardemnene som mangler. Lagrede
     * emner og anmeldelser endres ikke, så dette er trygt ved hver oppstart.
     *
     * @param filePath filen som inneholder emner og anmeldelser
     * @return lagringen serveren skal bruke
     */
    @Bean
    public ReviewRepository reviewRepository(
            @Value("${bithub.reviews-file:${user.home}/.bithub/courses.json}") String filePath) {
        ReviewRepository repository = new ReviewHandler(Path.of(filePath));
        repository.addCoursesIfMissing(DefaultCourses.all());
        return repository;
    }

    /**
     * Oppretter tjenesten med serverens lagring.
     *
     * @param repository lagring for anmeldelser
     * @return tjenesten som håndterer anmeldelser
     */
    @Bean
    public ReviewService reviewService(ReviewRepository repository) {
        return new ReviewService(repository);
    }
}