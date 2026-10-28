package core.repository;

import core.exception.DuplicateEmailException;
import core.model.User;
import core.service.UserService;
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
     * <p>Sjekken av e-postadressen og lagringen må skje samlet, slik at samtidige registreringer
     * ikke gir duplikater.
     *
     * @param user brukeren som skal lagres
     * @throws DuplicateEmailException hvis e-postadressen allerede er registrert
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

    /**
     * Erstatter den lagrede brukeren som har samme e-postadresse, for eksempel etter at passordet
     * er byttet.
     *
     * @param user brukeren med de nye opplysningene
     * @throws IllegalArgumentException hvis ingen bruker med denne e-postadressen er lagret
     */
    void update(User user);
}