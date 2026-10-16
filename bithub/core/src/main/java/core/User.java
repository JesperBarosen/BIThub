package core;

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
     */
    public User(String name, String email, String password) {
        validateName(name);
        validateEmail(email);

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
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
            throw new IllegalArgumentException("Hash cannot be null.");
        }
        if (salt == null) {
            throw new IllegalArgumentException("Salt cannot be null.");
        }
        if (Base64.getDecoder().decode(passwordHash).length != 32) {
            throw new IllegalArgumentException("Hash must contain 32 bytes.");
        }
        if (Base64.getDecoder().decode(salt).length != 16) {
            throw new IllegalArgumentException("Salt must contain 16 bytes.");
        }

        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty.");
        }
        if (name.length() < 3 || name.length() > 30) {
            throw new IllegalArgumentException("Name must be between 3 and 30 characters.");
        }
        // Regex foreslått av ChatGPT for å tillate bokstaver fra alle språk,
        // samt mellomrom, bindestrek og apostrof mellom navnedeler.
        if (!name.matches("\\p{L}+(?:[ '-]\\p{L}+)*")) {
            throw new IllegalArgumentException(
                    "Name can only contain letters, spaces, hyphens and apostrophes.");
        }
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty.");
        }
        // Regex foreslått av ChatGPT for enkel validering av e-postformat.
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Email must have a valid format.");
        }
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
