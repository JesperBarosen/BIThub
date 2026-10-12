package core;

/**
 * Representerer en uforanderlig bruker med navn, e-postadresse og passord.
 */
public final class User {

    private final String name;
    private final String email;
    private final String password;

    /**
     * Oppretter en bruker med validerte brukeropplysninger.
     *
     * @param name navn på 3 til 30 tegn med bokstaver og tillatte skilletegn
     * @param email e-postadresse som oppfyller formatkravet
     * @param password minst åtte tegn, kan ikke være null eller blankt
     * @throws IllegalArgumentException hvis en brukeropplysning er ugyldig
     */
    public User(String name, String email, String password) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty.");
        }
        if (name.length() < 3 || name.length() > 30) {
            throw new IllegalArgumentException(
                    "Name must be between 3 and 30 characters."
            );
        }
        // Regex foreslått av ChatGPT for å tillate bokstaver fra alle språk,
        // samt mellomrom, bindestrek og apostrof mellom navnedeler.
        if (!name.matches("\\p{L}+(?:[ '-]\\p{L}+)*")) {
            throw new IllegalArgumentException(
                    "Name can only contain letters, spaces, hyphens and apostrophes."
            );
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty.");
        }
        // Regex foreslått av ChatGPT for enkel validering av e-postformat.
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Email must have a valid format.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters."
            );
        }

        this.name = name;
        this.email = email;
        this.password = password;
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
     * Henter passordet til brukeren.
     *
     * @return passordet
     */
    public String getPassword() {
        return password;
    }
}
