package core.model;

import core.service.PasswordHasher;
import java.util.Base64;

/**
 * Representerer en uforanderlig bruker med navn, e-postadresse, passordhash og salt.
 */
public final class User {

    private final String name;
    private final String email;
    private final String passwordHash;
    private final String salt;

    /**
     * Oppretter en bruker med validerte brukeropplysninger.
     *
     * @param name     navn på 3 til 30 tegn med bokstaver og tillatte skilletegn
     * @param email    e-postadresse som oppfyller formatkravet
     * @param password minst åtte tegn, kan ikke være null eller blankt
     * @throws IllegalArgumentException hvis en brukeropplysning er ugyldig
     * @throws IllegalStateException    hvis hashing ikke kan utføres
     */
    public User(String name, String email, String password) {
        validateName(name);
        validateEmail(email);

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Passordet må fylles ut.");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Passord må være minst 8 tegn.");
        }

        this.name = name;
        this.email = email;
        this.salt = PasswordHasher.generateSalt();
        this.passwordHash = PasswordHasher.hashPassword(password, this.salt);
    }

    /**
     * Oppretter en bruker fra lagrede brukeropplysninger.
     *
     * @param name         navn som oppfyller navnekravene
     * @param email        e-postadresse som oppfyller formatkravet
     * @param passwordHash en passordhash på 32 byte kodet som Base64
     * @param salt         et salt på 16 byte kodet som Base64
     * @throws IllegalArgumentException hvis en brukeropplysning er ugyldig
     */
    public User(String name, String email, String passwordHash, String salt) {
        validateName(name);
        validateEmail(email);

        if (passwordHash == null) {
            throw new IllegalArgumentException("Hash kan ikke være null.");
        }
        if (salt == null) {
            throw new IllegalArgumentException("Salt kan ikke være null.");
        }
        if (Base64.getDecoder().decode(passwordHash).length != 32) {
            throw new IllegalArgumentException("Hash må inneholde 32 byte.");
        }
        if (Base64.getDecoder().decode(salt).length != 16) {
            throw new IllegalArgumentException("Salt må inneholde 16 byte.");
        }

        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Navn kan ikke være null eller tomt.");
        }
        if (name.length() < 3 || name.length() > 30) {
            throw new IllegalArgumentException("Navn må være mellom 3 og 30 tegn.");
        }
        // Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
        // Tillater bokstaver fra alle språk, og mellomrom, bindestrek og apostrof mellom
        // navnedeler.
        if (!name.matches("\\p{L}+(?:[ '-]\\p{L}+)*")) {
            throw new IllegalArgumentException(
                    "Navn kan kun inneholde bokstaver, mellomrom, bindestrek og apostrof.");
        }
        // Slutt på kode laget med hjelp av KI.
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-postadressen kan ikke være null eller tom.");
        }
        // Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
        // Enkel sjekk av e-postformatet.
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("E-post må ha et gyldig format.");
        }
        // Slutt på kode laget med hjelp av KI.
    }

    /**
     * Henter navnet til brukeren.
     *
     * @return navnet
     */
    public String getName() {
        return name;
    }

    /**
     * Henter e-postadressen til brukeren.
     *
     * @return e-postadressen
     */
    public String getEmail() {
        return email;
    }

    /**
     * Henter passordhashen til brukeren.
     *
     * @return passordhashen kodet som Base64
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Henter saltet som ble brukt ved hashing av passordet.
     *
     * @return saltet kodet som Base64
     */
    public String getSalt() {
        return salt;
    }

    /**
     * Kontrollerer et passord mot brukerens lagrede hash.
     *
     * @param password passordet som skal kontrolleres
     * @return true hvis passordet stemmer, ellers false
     * @throws IllegalArgumentException hvis passordet er null eller blankt
     * @throws IllegalStateException    hvis hashing ikke kan utføres
     */
    public boolean verifyPassword(String password) {
        return PasswordHasher.verifyPassword(password, salt, passwordHash);
    }
}