package file;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import core.Review;


@JsonPropertyOrder({ "courseCode", "courseName", "reviews" })
public class CourseDTO {
    public String courseCode;
    public String courseName;
    public List<Review> reviews = new ArrayList<>();

    /**
     * Standard tom konstruktør.
     * Kreves av serialiseringsbiblioteker som Jackson for å kunne 
     * opprette objekter fra JSON.
     */

    public CourseDTO() {
    }


    /**
     * Oppretter en ny CourseDTO med gitt emnekode og emnenavn.
     * 
     * @param courseCode Emnekoden for kurset (f.eks. "TDT4100").
     * @param courseName Fullt navn på kurset (f.eks. "Objektorientert programmering").
     */

    public CourseDTO(String courseCode, String courseName) {
        this.courseCode = courseCode;
        this.courseName = courseName;
    }


    /**
     * Henter emnekoden for kurset.
     * 
     * @return Emnekoden som en String.
     */

    public String getCourseCode() {
        return courseCode;
    }


    /**
     * Setter emnekoden for kurset.
     * 
     * @param courseCode Emnekoden som skal settes.
     */

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }


    /**
     * Henter emnenavnet for kurset.
     * 
     * @return Emnenavnet som en String.
     */

    public String getCourseName() {
        return courseName;
    }


    /**
     * Setter emnenavnet for kurset.
     * 
     * @param courseName Emnenavnet som skal settes.
     */

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }


    /**
     * Henter listen over anmeldelser (reviews) knyttet til kurset.
     * 
     * @return En liste av Review-objekter.
     */

    public List<Review> getReviews() {
        return reviews;
    }


    /**
     * Setter listen over anmeldelser for kurset. 
     * Dersom innsendt liste er null, opprettes en tom liste i stedet for å unngå NullPointerException.
     * 
     * @param reviews Listen med anmeldelser som skal settes.
     */
    
    public void setReviews(List<Review> reviews) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
    }
}