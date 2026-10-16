package core;

import java.util.Optional;

/**
 * Definerer hvordan brukere lagres og hentes.
 */
public interface UserRepository {
    /**
     * Lagrer en ny bruker.
     *
     * <p>Ved registrering gjennom {@link UserService} er brukeropplysningene validert,
     * e-postadressen normalisert og passordet hashet før denne metoden kalles.
     *
     * @param user brukeren som skal lagres
     */
    void save(User user);

    /**
     * Finner brukeren med den oppgitte e-postadressen.
     *
     * <p>{@link UserService} sender inn en normalisert adresse uten mellomrom rundt og med små
     * bokstaver. Implementasjonen må sikre at lagrede adresser kan finnes med denne formen, også
     * dersom eldre data inneholder store bokstaver.
     *
     * @param email den normaliserte e-postadressen det skal søkes etter
     * @return brukeren hvis adressen finnes, ellers et tomt resultat
     */
    Optional<User> findByEmail(String email);
}