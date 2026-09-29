package core;

public final class User {

    private String name;
    private String email;
    private String password;

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

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
