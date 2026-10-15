package rest.api;

import core.ReviewService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rest.model.CourseDetailsResponse;
import rest.model.CourseResponse;

/**
 * Endepunkter for å hente emner og anmeldelsene deres.
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final ReviewService reviewService;

    /**
     * Oppretter kontrolleren med tjenesten fra Spring.
     *
     * @param reviewService tjenesten som henter emner og anmeldelser
     */
    public CourseController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Henter alle emner, også de uten anmeldelser.
     *
     * @return alle emnene med gjennomsnitt og antall anmeldelser
     */
    @GetMapping
    public List<CourseResponse> getAllCourses() {
        return reviewService.getAllCourses().stream()
                .map(CourseResponse::from)
                .toList();
    }

    /**
     * Henter ett emne med alle anmeldelsene.
     *
     * @param code emnekoden, store og små bokstaver skilles ikke
     * @return emnet med gjennomsnitt og anmeldelser
     * @throws CourseNotFoundException hvis emnet ikke finnes
     */
    @GetMapping("/{code}")
    public CourseDetailsResponse getCourse(@PathVariable("code") String code) {
        return reviewService.findCourse(code)
                .map(CourseDetailsResponse::from)
                .orElseThrow(() -> new CourseNotFoundException(code));
    }
}