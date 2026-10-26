package rest.api;

import core.Review;
import core.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rest.auth.AuthenticationService;
import rest.model.ReviewResponse;
import rest.model.UpdateReviewRequest;

/**
 * Lar innloggede brukere endre og slette sine egne anmeldelser.
 */
@RestController
@RequestMapping("/api/reviews")
public class OwnReviewController {

    private final ReviewService reviewService;
    private final AuthenticationService authenticationService;

    /**
     * Oppretter kontrolleren med tjenestene fra Spring.
     *
     * @param reviewService tjenesten som sjekker eierskap og lagrer endringer
     * @param authenticationService felles kontroll av innlogging
     */
    public OwnReviewController(ReviewService reviewService,
            AuthenticationService authenticationService) {
        this.reviewService = reviewService;
        this.authenticationService = authenticationService;
    }

    /**
     * Endrer en anmeldelse som tilhører den innloggede brukeren.
     *
     * @param id id-en til anmeldelsen
     * @param request de nye verdiene fra klienten
     * @param authorization headeren med token
     * @return den oppdaterte anmeldelsen slik den vises offentlig
     * @throws NotLoggedInException hvis tokenet mangler eller er ukjent
     */
    @PutMapping("/{id}")
    public ReviewResponse updateReview(@PathVariable("id") String id,
            @RequestBody UpdateReviewRequest request,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        String email = authenticationService.requireEmail(authorization);
        Review review = reviewService.updateReview(id, email,
                String.valueOf(request.rating()), request.comment(), request.anonymous());
        return ReviewResponse.from(review);
    }

    /**
     * Sletter en anmeldelse som tilhører den innloggede brukeren.
     *
     * @param id id-en til anmeldelsen
     * @param authorization headeren med token
     * @throws NotLoggedInException hvis tokenet mangler eller er ukjent
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable("id") String id,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        String email = authenticationService.requireEmail(authorization);
        reviewService.deleteReview(id, email);
    }
}