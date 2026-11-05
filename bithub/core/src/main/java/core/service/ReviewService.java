package core.service;

import core.exception.DuplicateReviewException;
import core.exception.NotReviewOwnerException;
import core.exception.ReviewNotFoundException;
import core.model.Course;
import core.model.Review;
import core.model.User;
import core.repository.ReviewRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Håndterer innsending av anmeldelser.
 *
 * <p>Klassen validerer det brukeren har skrevet inn og sender anmeldelsen videre til lagring. Den
 * kjenner bare grensesnittet {@link ReviewRepository}, slik at core ikke blir avhengig av hvordan
 * anmeldelsene lagres.
 */
public final class ReviewService {

    private final ReviewRepository reviewRepository;

    /**
     * Oppretter tjenesten med lagringen den skal bruke.
     *
     * @param reviewRepository lagring for anmeldelser
     * @throws NullPointerException hvis reviewRepository er null
     */
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = Objects.requireNonNull(reviewRepository);
    }

    /**
     * Validerer og lagrer en anmeldelse med eier bestemt av serveren.
     *
     * <p>Bare innloggede brukere kan skrive anmeldelser, så alle nye anmeldelser får en eier.
     * Anmeldelser uten eier finnes bare fra release 2, og vises som «Gjest».
     *
     * <p>Hver bruker kan skrive én anmeldelse per emne, enten den er anonym eller ikke. Metoden er
     * synkronisert, slik at to forespørsler samtidig ikke kan gi to anmeldelser av samme emne.
     *
     * @param courseCode emnekoden
     * @param courseName navnet på emnet
     * @param owner      den verifiserte brukeren
     * @param anonymous  om brukeren ønsker å være anonym
     * @param ratingText vurderingen som tekst
     * @param comment    kommentaren, null behandles som tom tekst
     * @return den lagrede anmeldelsen med id og eierskap
     * @throws IllegalArgumentException hvis brukeren mangler, eller emnet eller vurderingen er
     *                                      ugyldig
     * @throws DuplicateReviewException hvis brukeren allerede har anmeldt emnet
     */
    public synchronized Review submitReview(String courseCode, String courseName, User owner,
            boolean anonymous, String ratingText, String comment) {
        if (owner == null) {
            throw new IllegalArgumentException("Du må logge inn for å skrive en anmeldelse.");
        }
        int rating = parseRating(ratingText);
        Course course = new Course(courseCode, courseName);
        if (hasReviewed(owner.getEmail(), course.getCourseCode())) {
            throw new DuplicateReviewException();
        }
        Review review = new Review(UUID.randomUUID().toString(), owner.getEmail(), owner.getName(),
                anonymous, rating, comment == null ? "" : comment.trim());
        reviewRepository.addReview(course.getCourseCode(), course.getCourseName(), review);
        return review;
    }

    /**
     * Validerer og lagrer en anmeldelse fra en innlogget bruker i klienten.
     *
     * <p>Klienten kjenner bare e-posten og navnet fra innloggingen, ikke hele brukeren. Serveren
     * bestemmer likevel eieren selv ut fra tokenet, så e-posten og navnet brukes bare til å lage en
     * gyldig anmeldelse. Det som sendes til serveren, er vurderingen, kommentaren og om anmeldelsen
     * skal være anonym.
     *
     * @param courseCode   emnekoden
     * @param courseName   navnet på emnet
     * @param ownerEmail   e-posten til den innloggede brukeren
     * @param reviewerName navnet til den innloggede brukeren
     * @param anonymous    om anmeldelsen skal vises som anonym
     * @param ratingText   vurderingen som tekst
     * @param comment      kommentaren, null behandles som tom tekst
     * @return den lagrede anmeldelsen
     * @throws IllegalArgumentException hvis emnet, e-posten, navnet eller vurderingen er ugyldig
     */
    public Review submitReview(String courseCode, String courseName, String ownerEmail,
            String reviewerName, boolean anonymous, String ratingText, String comment) {
        if (ownerEmail == null || ownerEmail.isBlank()) {
            throw new IllegalArgumentException("E-posten til den innloggede brukeren mangler.");
        }
        int rating = parseRating(ratingText);
        Course course = new Course(courseCode, courseName);
        Review review = new Review(UUID.randomUUID().toString(), ownerEmail, reviewerName,
                anonymous, rating, comment == null ? "" : comment.trim());
        reviewRepository.addReview(course.getCourseCode(), course.getCourseName(), review);
        return review;
    }

    /**
     * Henter emnet med alle lagrede anmeldelser, slik at de kan vises.
     *
     * <p>Dersom ingen har anmeldt emnet ennå, returneres et emne uten anmeldelser, slik at
     * brukergrensesnittet alltid har noe å vise.
     *
     * @param course emnet som skal vises
     * @return emnet med lagrede anmeldelser, eller et emne uten anmeldelser
     * @throws IllegalArgumentException hvis emnet er null
     */
    public Course getCourseWithReviews(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Emnet kan ikke være null.");
        }
        return reviewRepository.findCourse(course.getCourseCode())
                .orElseGet(() -> new Course(course.getCourseCode(), course.getCourseName()));
    }

    /**
     * Henter alle lagrede emner, også de uten anmeldelser.
     *
     * @return alle emnene med anmeldelsene sine
     */
    public List<Course> getAllCourses() {
        return reviewRepository.findAllCourses();
    }

    /**
     * Finner et lagret emne ut fra emnekoden, med alle anmeldelsene.
     *
     * @param courseCode emnekoden, store og små bokstaver skilles ikke
     * @return emnet, eller tom hvis emnet ikke finnes
     */
    public Optional<Course> findCourse(String courseCode) {
        return reviewRepository.findCourse(courseCode);
    }

    /**
     * Henter anmeldelsene en bruker har skrevet, også de anonyme, gruppert etter emne.
     *
     * <p>Bare emner der brukeren har skrevet minst en anmeldelse er med, og hvert emne inneholder
     * bare brukerens egne anmeldelser.
     *
     * @param ownerEmail e-posten til eieren
     * @return emnene med brukerens anmeldelser, eller en tom liste hvis e-posten mangler
     */
    public List<Course> findReviewsByOwner(String ownerEmail) {
        if (ownerEmail == null || ownerEmail.isBlank()) {
            return List.of();
        }
        return reviewRepository.findReviewsByOwner(ownerEmail.trim());
    }

    /**
     * Endrer en egen anmeldelse. Id, eier og navn beholdes.
     *
     * @param reviewId   id-en til anmeldelsen
     * @param ownerEmail e-posten til den innloggede brukeren, hentet fra tokenet på serveren
     * @param ratingText den nye vurderingen som tekst
     * @param comment    den nye kommentaren, null behandles som tom tekst
     * @param anonymous  om anmeldelsen skal vises som anonym
     * @return den oppdaterte anmeldelsen
     * @throws ReviewNotFoundException  hvis anmeldelsen ikke finnes
     * @throws NotReviewOwnerException  hvis anmeldelsen tilhører en annen eller er en
     *                                      gjesteanmeldelse
     * @throws IllegalArgumentException hvis vurderingen er ugyldig
     */
    public Review updateReview(String reviewId, String ownerEmail, String ratingText,
            String comment, boolean anonymous) {
        Review existing = requireOwnReview(reviewId, ownerEmail);
        int rating = parseRating(ratingText);
        String trimmedComment = comment == null ? "" : comment.trim();

        if (!reviewRepository.updateReview(reviewId, rating, trimmedComment, anonymous)) {
            throw new ReviewNotFoundException(reviewId);
        }
        return new Review(existing.getId(), existing.getOwnerEmail(), existing.getReviewerName(),
                anonymous, rating, trimmedComment);
    }

    /**
     * Endrer en av den innloggede brukerens anmeldelser fra klienten.
     *
     * <p>Klienten har hentet anmeldelsen fra «Mine anmeldelser», og serveren sjekker eierskapet ut
     * fra tokenet. Derfor sjekkes ikke eieren her, bare den nye vurderingen. Id, eier og navn
     * beholdes.
     *
     * @param review     anmeldelsen slik den ble hentet
     * @param ratingText den nye vurderingen som tekst
     * @param comment    den nye kommentaren, null behandles som tom tekst
     * @param anonymous  om anmeldelsen skal vises som anonym
     * @return den oppdaterte anmeldelsen
     * @throws NullPointerException     hvis review er null
     * @throws IllegalArgumentException hvis vurderingen er ugyldig
     * @throws ReviewNotFoundException  hvis anmeldelsen ikke finnes lenger
     */
    public Review updateReview(Review review, String ratingText, String comment,
            boolean anonymous) {
        Objects.requireNonNull(review);
        int rating = parseRating(ratingText);
        String trimmedComment = comment == null ? "" : comment.trim();

        if (!reviewRepository.updateReview(review.getId(), rating, trimmedComment, anonymous)) {
            throw new ReviewNotFoundException(review.getId());
        }
        return new Review(review.getId(), review.getOwnerEmail(), review.getReviewerName(),
                anonymous, rating, trimmedComment);
    }

    /**
     * Sletter en egen anmeldelse.
     *
     * @param reviewId   id-en til anmeldelsen
     * @param ownerEmail e-posten til den innloggede brukeren, hentet fra tokenet på serveren
     * @throws ReviewNotFoundException hvis anmeldelsen ikke finnes
     * @throws NotReviewOwnerException hvis anmeldelsen tilhører en annen eller er en
     *                                     gjesteanmeldelse
     */
    public void deleteReview(String reviewId, String ownerEmail) {
        requireOwnReview(reviewId, ownerEmail);
        if (!reviewRepository.deleteReview(reviewId)) {
            throw new ReviewNotFoundException(reviewId);
        }
    }

    /**
     * Sletter en av den innloggede brukerens anmeldelser fra klienten. Serveren sjekker eierskapet
     * ut fra tokenet.
     *
     * @param review anmeldelsen slik den ble hentet
     * @throws NullPointerException    hvis review er null
     * @throws ReviewNotFoundException hvis anmeldelsen ikke finnes lenger
     */
    public void deleteReview(Review review) {
        Objects.requireNonNull(review);
        if (!reviewRepository.deleteReview(review.getId())) {
            throw new ReviewNotFoundException(review.getId());
        }
    }

    /**
     * Gir alle anmeldelsene brukeren eier, det nye navnet. Kalles på serveren etter at brukeren har
     * endret navnet sitt.
     *
     * @param ownerEmail   e-posten til brukeren, hentet fra tokenet på serveren
     * @param reviewerName det nye navnet, som allerede er validert av {@link UserService}
     * @return antall anmeldelser som fikk nytt navn
     * @throws IllegalArgumentException hvis e-posten eller navnet mangler
     */
    public int renameReviewer(String ownerEmail, String reviewerName) {
        if (ownerEmail == null || ownerEmail.isBlank()) {
            throw new IllegalArgumentException("E-posten kan ikke være tom.");
        }
        if (reviewerName == null || reviewerName.isBlank()) {
            throw new IllegalArgumentException("Navnet kan ikke være tomt.");
        }
        return reviewRepository.updateReviewerName(ownerEmail.trim(), reviewerName);
    }

    /**
     * Henter anmeldelsen og sjekker at den tilhører brukeren. Gjesteanmeldelser har ingen eier, og
     * kan derfor ikke endres av noen.
     *
     * <p>Eieren av en anmeldelse endres aldri. Hvis anmeldelsen slettes mellom sjekken og
     * endringen, svarer lagringen {@code false}, og det gir {@link ReviewNotFoundException}.
     *
     * @param reviewId   id-en til anmeldelsen
     * @param ownerEmail e-posten til den innloggede brukeren
     * @return den lagrede anmeldelsen
     * @throws ReviewNotFoundException hvis anmeldelsen ikke finnes
     * @throws NotReviewOwnerException hvis brukeren ikke eier anmeldelsen
     */
    private Review requireOwnReview(String reviewId, String ownerEmail) {
        Review review = reviewRepository.findReview(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        if (ownerEmail == null || review.getOwnerEmail() == null
                || !review.getOwnerEmail().equalsIgnoreCase(ownerEmail.trim())) {
            throw new NotReviewOwnerException();
        }
        return review;
    }

    /**
     * Sjekker om brukeren allerede har en anmeldelse av emnet, anonym eller ikke.
     *
     * @param ownerEmail e-posten til brukeren
     * @param courseCode emnekoden
     * @return true hvis brukeren har anmeldt emnet
     */
    private boolean hasReviewed(String ownerEmail, String courseCode) {
        return reviewRepository.findReviewsByOwner(ownerEmail).stream()
                .anyMatch(course -> course.getCourseCode().equalsIgnoreCase(courseCode));
    }

    /**
     * Gjør om vurderingen fra tekst til et tall fra 1 til 5.
     *
     * @param ratingText vurderingen slik brukeren skrev den inn
     * @return vurderingen som tall
     * @throws IllegalArgumentException hvis teksten ikke er et tall fra 1 til 5
     */
    private static int parseRating(String ratingText) {
        if (ratingText == null || ratingText.isBlank()) {
            throw new IllegalArgumentException("Vurdering må fylles ut.");
        }
        int rating;
        try {
            rating = Integer.parseInt(ratingText.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Vurdering må være et gyldig tall.", e);
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Vurdering må være et tall fra 1 til 5.");
        }
        return rating;
    }
}