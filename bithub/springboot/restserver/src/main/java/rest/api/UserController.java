package rest.api;

import core.ReviewService;
import core.User;
import core.UserService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rest.auth.TokenService;
import rest.model.AuthResponse;
import rest.model.ChangePasswordRequest;
import rest.model.LoginRequest;
import rest.model.OwnReviewResponse;
import rest.model.RegisterRequest;

/**
 * Endepunkter for registrering, innlogging og passordbytte, og for å hente egne anmeldelser.
 */
@RestController
@RequestMapping("/api")
public class UserController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final UserService userService;
    private final ReviewService reviewService;
    private final TokenService tokenService;

    /**
     * Oppretter kontrolleren med tjenestene fra Spring.
     *
     * @param userService   tjenesten som registrerer og logger inn brukere
     * @param reviewService tjenesten som henter anmeldelser
     * @param tokenService  tjenesten som lager og slår opp innloggingstokener
     */
    public UserController(UserService userService, ReviewService reviewService,
            TokenService tokenService) {
        this.userService = userService;
        this.reviewService = reviewService;
        this.tokenService = tokenService;
    }

    /**
     * Registrerer en ny bruker og logger den inn med en gang.
     *
     * @param request navn, e-post og passord fra klienten
     * @return token, navn og e-post til den nye brukeren
     * @throws IllegalArgumentException hvis opplysningene er ugyldige
     * @throws core.DuplicateEmailException hvis e-posten allerede er registrert
     */
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody RegisterRequest request) {
        User user = userService.register(request.name(), request.email(), request.password());
        return AuthResponse.from(user, tokenService.createToken(user.getEmail()));
    }

    /**
     * Logger inn en bruker med e-post og passord.
     *
     * @param request e-post og passord fra klienten
     * @return token, navn og e-post til den innloggede brukeren
     * @throws InvalidCredentialsException hvis e-post eller passord er feil
     */
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        User user = userService.login(request.email(), request.password())
                .orElseThrow(InvalidCredentialsException::new);
        return AuthResponse.from(user, tokenService.createToken(user.getEmail()));
    }

    /**
     * Bytter passordet til en bruker etter å ha sjekket det gamle passordet.
     *
     * <p>Foreløpig sjekkes ikke hvem som sender forespørselen. At bare eieren kan bytte sitt eget
     * passord, kommer i (#176).
     *
     * @param email e-postadressen til brukeren
     * @param request det gamle og det nye passordet
     * @throws IllegalArgumentException hvis det gamle passordet er feil,
     *                                  brukeren ikke finnes eller det nye er ugyldig
     */
    @PutMapping("/users/{email}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable("email") String email,
            @RequestBody ChangePasswordRequest request) {
        userService.changePassword(email, request.oldPassword(), request.newPassword());
    }

    /**
     * Henter anmeldelsene den innloggede brukeren har skrevet, også de anonyme.
     *
     * @param authorization headeren {@code Authorization: Bearer <token>}, eller null
     * @return brukerens egne anmeldelser med emnet de hører til
     * @throws NotLoggedInException hvis tokenet mangler eller er ukjent
     */
    @GetMapping("/me/reviews")
    public List<OwnReviewResponse> getOwnReviews(
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        String email = tokenService.findEmail(bearerToken(authorization))
                .orElseThrow(NotLoggedInException::new);
        return reviewService.findReviewsByOwner(email).stream()
                .flatMap(course -> course.getReviews().stream()
                        .map(review -> OwnReviewResponse.from(course, review)))
                .toList();
    }

    /**
     * Henter selve tokenet fra Authorization-headeren.
     *
     * @param authorization headeren, for eksempel {@code Bearer 3f1c9a2e-...}
     * @return tokenet, eller null hvis headeren mangler eller ikke starter med «Bearer »
     */
    private static String bearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length()).strip();
    }
}