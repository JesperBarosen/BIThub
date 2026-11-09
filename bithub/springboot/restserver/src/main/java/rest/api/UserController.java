package rest.api;

import core.model.User;
import core.service.ReviewService;
import core.service.UserService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import rest.auth.AuthenticationService;
import rest.auth.TokenService;
import rest.exception.ForbiddenException;
import rest.exception.InvalidCredentialsException;
import rest.exception.NotLoggedInException;
import rest.model.AuthResponse;
import rest.model.ChangeNameRequest;
import rest.model.ChangePasswordRequest;
import rest.model.LoginRequest;
import rest.model.NameResponse;
import rest.model.OwnReviewResponse;
import rest.model.RegisterRequest;

/**
 * Endepunkter for registrering, innlogging, utlogging, passordbytte, navnebytte og egne
 * anmeldelser.
 */
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final ReviewService reviewService;
    private final TokenService tokenService;
    private final AuthenticationService authenticationService;

    /**
     * Oppretter kontrolleren med tjenestene fra Spring.
     *
     * @param userService           tjenesten som registrerer og logger inn brukere
     * @param reviewService         tjenesten som henter anmeldelser
     * @param tokenService          tjenesten som lager og slår opp innloggingstokener
     * @param authenticationService felles kontroll av innlogging
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "Spring deler tokentjenesten slik at utlogging gjelder alle kall.")
    public UserController(UserService userService, ReviewService reviewService,
            TokenService tokenService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.reviewService = reviewService;
        this.tokenService = tokenService;
        this.authenticationService = authenticationService;
    }

    /**
     * Registrerer en ny bruker og logger den inn med en gang.
     *
     * @param request navn, e-post og passord fra klienten
     * @return token, navn og e-post til den nye brukeren
     * @throws IllegalArgumentException               hvis opplysningene er ugyldige
     * @throws core.exception.DuplicateEmailException hvis e-posten allerede er registrert
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
     * Avslutter innloggingen som tokenet tilhører.
     *
     * @param authorization headeren {@code Authorization: Bearer <token>}, eller null
     * @throws NotLoggedInException hvis tokenet mangler, er ukjent eller allerede er fjernet
     */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        authenticationService.logout(authorization);
    }

    /**
     * Bytter eget passord etter å ha kontrollert tokenet og det gamle passordet.
     *
     * @param email         e-postadressen til brukeren
     * @param request       det gamle og det nye passordet
     * @param authorization headeren med innloggingstokenet, eller null
     * @throws NotLoggedInException     hvis innloggingen mangler eller er ugyldig
     * @throws ForbiddenException       hvis e-posten i stien tilhører en annen bruker
     * @throws IllegalArgumentException hvis det gamle passordet er feil, brukeren ikke finnes eller
     *                                      det nye er ugyldig
     */
    @PutMapping("/users/{email}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable("email") String email,
            @RequestBody ChangePasswordRequest request,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        String authenticatedEmail = authenticationService.requireEmail(authorization);
        if (!authenticatedEmail.equalsIgnoreCase(email.trim())) {
            throw new ForbiddenException();
        }
        userService.changePassword(authenticatedEmail, request.oldPassword(),
                request.newPassword());
    }

    /**
     * Endrer eget navn etter å ha kontrollert tokenet. Anmeldelsene brukeren eier, får også det nye
     * navnet.
     *
     * @param email         e-postadressen til brukeren
     * @param request       det nye navnet
     * @param authorization headeren med innloggingstokenet, eller null
     * @return navnet slik det ble lagret
     * @throws NotLoggedInException     hvis innloggingen mangler eller er ugyldig
     * @throws ForbiddenException       hvis e-posten i stien tilhører en annen bruker
     * @throws IllegalArgumentException hvis navnet er ugyldig eller brukeren ikke finnes
     */
    @PutMapping("/users/{email}/name")
    public NameResponse changeName(@PathVariable("email") String email,
            @RequestBody ChangeNameRequest request,
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        String authenticatedEmail = authenticationService.requireEmail(authorization);
        if (!authenticatedEmail.equalsIgnoreCase(email.trim())) {
            throw new ForbiddenException();
        }
        synchronized (userService) {
            User previous = userService.findByEmail(authenticatedEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Brukeren finnes ikke."));
            User renamed = userService.changeName(authenticatedEmail, request.name());
            try {
                reviewService.renameReviewer(renamed.getEmail(), renamed.getName());
            } catch (RuntimeException error) {
                try {
                    userService.changeName(authenticatedEmail, previous.getName());
                } catch (RuntimeException rollbackError) {
                    error.addSuppressed(rollbackError);
                }
                throw error;
            }
            return new NameResponse(renamed.getName());
        }
    }

    // Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
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
        String email = authenticationService.requireEmail(authorization);
        return reviewService.findReviewsByOwner(email).stream().flatMap(course -> course
                .getReviews().stream().map(review -> OwnReviewResponse.from(course, review)))
                .toList();
    }

}