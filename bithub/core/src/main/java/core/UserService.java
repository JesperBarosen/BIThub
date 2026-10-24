package core;

import java.util.Locale;
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
     * <p>E-postadressen normaliseres ved å fjerne mellomrom rundt adressen og gjøre bokstavene små
     * med {@link Locale#ROOT} før validering, oppslag og lagring. Passordet hashes
     * før brukeren lagres.
     *
     * @param name     navnet til brukeren
     * @param email    e-postadressen til brukeren
     * @param password passordet som skal hashes før lagring
     * @return den registrerte brukeren
     * @throws IllegalArgumentException hvis brukeropplysningene er ugyldige
     * @throws DuplicateEmailException  hvis e-postadressen allerede er registrert
     */
    public User register(String name, String email, String password) {
        User user = new User(name, normalizeEmail(email), password);

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicateEmailException("E-postadressen er allerede registrert.");
        }

        userRepository.save(user);
        return user;
    }

    /**
     * Logger inn en bruker ved å sjekke passordet mot den lagrede hashen.
     *
     * <p>E-postadressen normaliseres ved å fjerne mellomrom rundt adressen og gjøre bokstavene små
     * med {@link Locale#ROOT} før oppslag. Passordet brukes uendret ved kontroll mot hashen.
     *
     * <p>Hvis e-post eller passord er null eller blankt, returneres et tomt resultat
     * uten oppslag i lagringen.
     *
     * @param email    e-postadressen til brukeren
     * @param password passordet til brukeren
     * @return brukeren hvis opplysningene stemmer, ellers et tomt resultat
     */
    public Optional<User> login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByEmail(normalizeEmail(email))
                .filter(user -> user.verifyPassword(password));
    }

    /**
     * Henter en registrert bruker. Oppslaget gir ikke i seg selv rett til å opptre som brukeren.
     *
     * @param email e-postadressen, som normaliseres før oppslag
     * @return brukeren, eller tomt hvis adressen mangler eller ikke finnes
     */
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByEmail(normalizeEmail(email));
    }

    /**
     * Bytter passordet til en bruker etter å ha sjekket det gamle passordet.
     *
     * <p>Hvem som har lov til å bytte passordet, sjekkes på serveren og ikke her.
     *
     * @param email       e-postadressen til brukeren
     * @param oldPassword det nåværende passordet
     * @param newPassword det nye passordet, som valideres som ved registrering
     * @throws IllegalArgumentException hvis det gamle passordet er feil, brukeren ikke finnes,
     *                                  eller det nye passordet er ugyldig
     */
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = login(email, oldPassword)
                .orElseThrow(() -> new IllegalArgumentException("Det gamle passordet er feil."));
        userRepository.update(new User(user.getName(), user.getEmail(), newPassword));
    }

    /**
     * Fjerner mellomrom rundt e-postadressen og gjør den om til små bokstaver, slik
     * at {@code Ola@ntnu.no} og {@code ola@ntnu.no} regnes som samme adresse.
     *
     * @param email e-postadressen som skal normaliseres
     * @return den normaliserte e-postadressen, eller null hvis email er null
     */
    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
