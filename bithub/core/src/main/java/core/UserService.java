package core;

import java.util.Objects;
import java.util.Optional;

/**
 * Håndterer registrering og innlogging av brukere.
 *
 * <p>Bruker et repository slik at tjenesten er uavhengig av hvordan brukerne lagres.
 */
public final class UserService {

    private final UserRepository userRepository;

    /**
     * Oppretter tjenesten med lagringen den skal bruke.
     *
     * @param userRepository lagring for brukere
     * @throws NullPointerException hvis userRepository er null
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    /**
     * Validerer og registrerer en ny bruker.
     *
     * @param name     navnet til brukeren
     * @param email    e-postadressen til brukeren
     * @param password passordet som skal hashes før lagring
     * @return den registrerte brukeren
     * @throws IllegalArgumentException hvis brukeropplysningene er ugyldige
     * @throws DuplicateEmailException  hvis e-postadressen allerede er registrert
     */
    public User register(String name, String email, String password) {
        User user = new User(name, email, password);

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicateEmailException("E-postadressen er allerede registrert.");
        }

        userRepository.save(user);
        return user;
    }

    /**
     * Logger inn en bruker ved å sjekke passordet mot den lagrede hashen.
     *
     * @param email    e-postadressen til brukeren
     * @param password passordet til brukeren
     * @return brukeren hvis e-post og passord stemmer, ellers tom
     */
    public Optional<User> login(String email, String password) {
        if (password == null || password.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email).filter(user -> user.verifyPassword(password));
    }
}