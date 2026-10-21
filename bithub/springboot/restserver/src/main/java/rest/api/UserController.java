package rest.api;

import core.User;
import core.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rest.auth.TokenService;
import rest.model.AuthResponse;
import rest.model.LoginRequest;
import rest.model.RegisterRequest;

/**
 * Endepunkter for å registrere og logge inn brukere.
 */
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;

    /**
     * Oppretter kontrolleren med tjenestene fra Spring.
     *
     * @param userService  tjenesten som registrerer og logger inn brukere
     * @param tokenService tjenesten som lager innloggingstokener
     */
    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
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
}
