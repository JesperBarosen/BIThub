package file;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;

/**
 * Dataoverføringsobjekt for et emne med tilhørende anmeldelser.
 *
 * <p>Brukes ved serialisering og deserialisering av emnedata med Jackson.
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@JsonPropertyOrder({"courseCode", "courseName", "reviews"})
public class CourseDTO {

    private String courseCode;
    private String courseName;
    private List<ReviewDTO> reviews = new ArrayList<>();

    /**
     * Oppretter et tomt CourseDTO-objekt.
     *
     * <p>Kreves av serialiseringsbiblioteker som Jackson for å kunne opprette
     * objekter fra JSON.
     */
    public CourseDTO() {
    }

    /**
     * Oppretter et nytt CourseDTO-objekt med gitt emnekode og emnenavn.
     *
     * @param courseCode emnekoden for kurset, for eksempel "TDT4100"
     * @param courseName fullt navn på kurset, for eksempel
     *     "Objektorientert programmering"
     */
    public CourseDTO(String courseCode, String courseName) {
        this.courseCode = courseCode;
        this.courseName = courseName;
    }

    /**
     * Henter emnekoden for kurset.
     *
     * @return emnekoden
     */
    public String getCourseCode() {
        return courseCode;
    }

    /**
     * Setter emnekoden for kurset.
     *
     * @param courseCode emnekoden som skal settes
     */
    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    /**
     * Henter emnenavnet for kurset.
     *
     * @return emnenavnet
     */
    public String getCourseName() {
        return courseName;
    }

    /**
     * Setter emnenavnet for kurset.
     *
     * @param courseName emnenavnet som skal settes
     */
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    /**
     * Henter listen over anmeldelser knyttet til kurset.
     *
     * @return listen over anmeldelser
     */
    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP",
            justification =
                    "DTO trenger direkte tilgang til listen for "
                            + "serialisering og deserialisering med Jackson.")
    public List<ReviewDTO> getReviews() {
        return reviews;
    }

    /**
     * Setter listen over anmeldelser for kurset.
     *
     * <p>Dersom listen er null, opprettes en tom liste for å unngå
     * NullPointerException.
     *
     * @param reviews listen med anmeldelser som skal settes
     */
    public void setReviews(List<ReviewDTO> reviews) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
    }
}
