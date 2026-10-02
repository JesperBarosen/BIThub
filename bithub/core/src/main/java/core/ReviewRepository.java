package core;

import java.util.Optional;

/**
 * Beskriver lagring av anmeldelser uten at domenelaget trenger å vite hvordan
 * eller hvor de lagres.
 *
 * <p>Grensesnittet ligger i core slik at core ikke blir avhengig av file.
 * File-modulen implementerer det, og App kobler implementasjonen til.
 */
public interface ReviewRepository {

    /**
     * Lagrer en anmeldelse knyttet til et emne.
     *
     * @param courseCode emnekoden til emnet
     * @param courseName navnet på emnet
     * @param review anmeldelsen som skal lagres
     */
    void addReview(String courseCode, String courseName, Review review);

    /**
     * Henter et lagret emne med alle anmeldelsene som er skrevet om det.
     *
     * @param courseCode emnekoden til emnet, store og små bokstaver skilles ikke
     * @return emnet med anmeldelser, eller tom dersom emnet ikke har noen lagred anmeldelser
     */
    Optional<Course> findCourse(String courseCode);
}