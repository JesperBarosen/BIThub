package file;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Dataoverføringsobjekt for brukerdata.
 *
 * <p>Brukes ved serialisering og deserialisering av brukere med Jackson.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@JsonPropertyOrder({ "name", "email", "passwordHash", "salt" })
public class UserDTO {

    private String name;
    private String email;
    private String passwordHash;
    private String salt;

    /**
     * Oppretter et tomt UserDTO-objekt.
     *
     * <p>Kreves av Jackson ved deserialisering.
     */
    public UserDTO() {
    }

    /**
     * Oppretter et nytt UserDTO-objekt med brukerdata.
     *
     * @param name         navnet til brukeren
     * @param email        e-postadressen til brukeren
     * @param passwordHash passordets hash
     * @param salt         saltet som brukes ved hashing
     */
    public UserDTO(String name, String email, String passwordHash, String salt) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
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
     * Setter navnet til brukeren.
     *
     * @param name navnet
     */
    public void setName(String name) {
        this.name = name;
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
     * Setter e-postadressen til brukeren.
     *
     * @param email e-postadressen
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Henter passordets hash.
     *
     * @return passordets hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Setter passordets hash.
     *
     * @param passwordHash passordets hash
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Henter saltet som brukes ved hashing.
     *
     * @return saltet
     */
    public String getSalt() {
        return salt;
    }

    /**
     * Setter saltet som brukes ved hashing.
     *
     * @param salt saltet
     */
    public void setSalt(String salt) {
        this.salt = salt;
    }
}