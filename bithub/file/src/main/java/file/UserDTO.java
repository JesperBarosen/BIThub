package file;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Dataoverføringsobjekt for brukerdata.
 *
 * <p>Brukes ved serialisering og deserialisering av brukere med Jackson.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@JsonPropertyOrder({"name", "email", "password"})
public class UserDTO {

    private String name;
    private String email;
    private String password;

    /**
     * Oppretter et tomt UserDTO-objekt.
     *
     * <p>Kreves av Jackson ved deserialisering.
     */
    public UserDTO() {
    }

    /**
     * Oppretter et nytt UserDTO-objekt med navn, e-post og passord.
     *
     * @param name navnet til brukeren
     * @param email e-postadressen til brukeren
     * @param password passordet til brukeren
     */
    public UserDTO(String name, String email, String password) {
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
     * Henter passordet til brukeren.
     *
     * @return passordet
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter passordet til brukeren.
     *
     * @param password passordet
     */
    public void setPassword(String password) {
        this.password = password;
    }
}