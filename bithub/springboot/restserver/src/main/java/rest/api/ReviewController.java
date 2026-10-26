package rest.api;

import core.Course;
import core.Review;
import core.ReviewService;
import core.User;
import core.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rest.auth.AuthenticationService;
import rest.model.NewReviewRequest;
import rest.model.ReviewResponse;

/**
 * Tar imot nye anmeldelser fra innloggede brukere. Eieren hentes fra tokenet, så gjester kan lese
 * anmeldelser, men ikke skrive dem.
 */
@RestController
@RequestMapping("/api/courses")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final AuthenticationService authenticationService;

    /**
     * Oppretter kontrolleren med tjenesten fra Spring.
     *
     * @param reviewService         tjenesten som validerer og lagrer anmeldelser
     * @param userService           tjenesten som finner den registrerte brukeren
     * @param authenticationService felles kontroll av innlogging
     */
    public ReviewController(ReviewService reviewService, UserService userService,
            AuthenticationService authenticationService) {
        this.reviewService = reviewService;
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    /**
     * Lagrer en anmeldelse for et eksisterende emne med eier fra tokenet.
     *
     * @param code          emnekoden
     * @param request       anmeldelsen fra klienten
     * @param authorization headeren med token
     * @return den opprettede anmeldelsen
     * @throws CourseNotFoundException  hvis emnet ikke finnes
     * @throws IllegalArgumentException hvis vurderingen er ugyldig
     * @throws NotLoggedInException     hvis tokenet mangler eller ikke gir en gyldig innlogging
     */
    @PostMapping("/{code}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(@PathVariable("code") String code,
            @RequestBody NewReviewRequest request,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        String email = authenticationService.requireEmail(authorization);
        User owner = userService.findByEmail(email).orElseThrow(NotLoggedInException::new);
        Course course = reviewService.findCourse(code)
                .orElseThrow(() -> new CourseNotFoundException(code));

        Review review = reviewService.submitReview(course.getCourseCode(), course.getCourseName(),
                owner, request.anonymous(), String.valueOf(request.rating()), request.comment());
        return ReviewResponse.from(review);
    }
}