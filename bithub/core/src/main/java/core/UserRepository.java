package core;

import java.util.Optional;

/**
 * Definerer hvordan brukere lagres og hentes.
 */
public interface UserRepository {
    /**
     * Lagrer ny bruker.
     * 
     * @param user brukeren som skal lagres
     */
    void save(User user);

    /**
     * Finner brukeren med den oppgitte e-postadressen.
     * 
     * @param email e-postadressen det skal søkes etter
     * @return brukeren, eller tom hvis den ikke finner noen
     */
    Optional<User> findByEmail(String email);
}
