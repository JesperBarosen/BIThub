package core.repository;

import core.model.Course;
import core.model.Review;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Beskriver lagring av anmeldelser uten at domenelaget trenger å vite hvordan eller hvor de lagres.
 *
 * <p>Grensesnittet ligger i core slik at core ikke blir avhengig av file. File-modulen
 * implementerer det, og App kobler implementasjonen til.
 */
public interface ReviewRepository {

    /**
     * Lagrer en anmeldelse knyttet til et emne.
     *
     * @param courseCode emnekoden til emnet
     * @param courseName navnet på emnet
     * @param review     anmeldelsen som skal lagres
     */
    void addReview(String courseCode, String courseName, Review review);

    /**
     * Henter et lagret emne med alle anmeldelsene som er skrevet om det.
     *
     * @param courseCode emnekoden til emnet, store og små bokstaver skilles ikke
     * @return emnet med anmeldelser, eller tom dersom emnet ikke har noen lagred anmeldelser
     */
    Optional<Course> findCourse(String courseCode);

    /**
     * Henter alle lagrede emner, også de som ikke har anmeldelser.
     *
     * @return alle lagrede emner med anmeldelsene sine
     */
    List<Course> findAllCourses();

    /**
     * Lagrer emnene som ikke finnes fra før. Emner som allerede er lagret, og anmeldelsene deres,
     * endres ikke.
     *
     * @param courses emnene som skal finnes i lagringen
     */
    void addCoursesIfMissing(List<Course> courses);

    /**
     * Henter én anmeldelse ut fra id-en.
     *
     * <p>Standardimplementasjonen leter gjennom alle emnene fra {@link #findAllCourses()}.
     *
     * @param reviewId id-en til anmeldelsen
     * @return anmeldelsen, eller tom hvis den ikke finnes
     */
    default Optional<Review> findReview(String reviewId) {
        if (reviewId == null) {
            return Optional.empty();
        }
        return findAllCourses().stream().flatMap(course -> course.getReviews().stream())
                .filter(review -> reviewId.equals(review.getId())).findFirst();
    }

    /**
     * Henter anmeldelsene en bruker har skrevet, også de anonyme, gruppert etter emne.
     *
     * <p>Standardimplementasjonen leter gjennom alle emnene fra {@link #findAllCourses()} og
     * sammenligner e-posten uten å skille store og små bokstaver. Lagringer som ikke har
     * anmeldelsene i {@link #findAllCourses()}, for eksempel klienten for serveren, overstyrer
     * metoden.
     *
     * @param ownerEmail e-posten til eieren
     * @return emnene der brukeren har skrevet minst én anmeldelse, med bare brukerens egne
     *             anmeldelser
     */
    default List<Course> findReviewsByOwner(String ownerEmail) {
        List<Course> result = new ArrayList<>();
        for (Course course : findAllCourses()) {
            Course ownCourse = new Course(course.getCourseCode(), course.getCourseName());
            course.getReviews().stream()
                    .filter(review -> ownerEmail.equalsIgnoreCase(review.getOwnerEmail()))
                    .forEach(ownCourse::addReview);
            if (!ownCourse.getReviews().isEmpty()) {
                result.add(ownCourse);
            }
        }
        return result;
    }

    /**
     * Endrer vurderingen, kommentaren og anonymiteten til en lagret anmeldelse. Id, eier og navn
     * beholdes. Lagringer som ikke støtter endring, kaster {@link UnsupportedOperationException}.
     *
     * @param reviewId  id-en til anmeldelsen
     * @param rating    den nye vurderingen fra 1 til 5
     * @param comment   den nye kommentaren
     * @param anonymous om anmeldelsen skal vises som anonym
     * @return true hvis anmeldelsen ble endret, false hvis den ikke finnes
     * @throws IllegalArgumentException hvis de nye verdiene er ugyldige
     */
    default boolean updateReview(String reviewId, int rating, String comment, boolean anonymous) {
        throw new UnsupportedOperationException(
                "Denne lagringen støtter ikke endring av anmeldelser.");
    }

    /**
     * Sletter en lagret anmeldelse. Lagringer som ikke støtter sletting, kaster
     * {@link UnsupportedOperationException}.
     *
     * @param reviewId id-en til anmeldelsen
     * @return true hvis anmeldelsen ble slettet, false hvis den ikke finnes
     */
    default boolean deleteReview(String reviewId) {
        throw new UnsupportedOperationException(
                "Denne lagringen støtter ikke sletting av anmeldelser.");
    }
}