package rest.api;

import core.Course;
import core.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rest.model.AuthorType;
import rest.model.NewReviewRequest;
import rest.model.ReviewResponse;

/**
 * Tar imot nye anmeldelser. Alle lagres som gjest frem til innlogging støttes.
 */
@RestController
@RequestMapping("/api/courses")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Oppretter kontrolleren med tjenesten fra Spring.
     *
     * @param reviewService tjenesten som validerer og lagrer anmeldelser
     */
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Lagrer en gjesteanmeldelse for et eksisterende emne.
     *
     * @param code emnekoden
     * @param request anmeldelsen fra klienten
     * @return den opprettede anmeldelsen
     * @throws CourseNotFoundException hvis emnet ikke finnes
     * @throws IllegalArgumentException hvis vurderingen er ugyldig
     */
    @PostMapping("/{code}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(@PathVariable("code") String code,
            @RequestBody NewReviewRequest request) {
        Course course = reviewService.findCourse(code)
                .orElseThrow(() -> new CourseNotFoundException(code));

        reviewService.submitReview(course.getCourseCode(), course.getCourseName(),
                ReviewResponse.GUEST_NAME, String.valueOf(request.rating()), request.comment());

        String comment = request.comment() == null ? "" : request.comment().trim();
        return new ReviewResponse(ReviewResponse.GUEST_NAME, AuthorType.GUEST,
                request.rating(), comment);
    }
}
