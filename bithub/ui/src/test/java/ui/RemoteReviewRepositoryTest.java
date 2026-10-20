package ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Testklasse for {@link RemoteReviewRepository}.
 */
class RemoteReviewRepositoryTest {

    /**
     * Verifiserer at standardkonstruktøren bruker standardadressen.
     */
    @Test
    void testDefaultConstructorUsesDefaultBaseUrl() {
        RemoteReviewRepository repository = new RemoteReviewRepository();
        assertEquals("http://localhost:8080", repository.getBaseUrl());
    }

    /**
     * Verifiserer at en oppgitt adresse beholdes.
     */
    @Test
    void testConstructorKeepsGivenBaseUrl() {
        RemoteReviewRepository repository = new RemoteReviewRepository("http://localhost:1234");
        assertEquals("http://localhost:1234", repository.getBaseUrl());
    }

    /**
     * Verifiserer at skråstrek på slutten av adressen fjernes.
     */
    @Test
    void testConstructorRemovesTrailingSlash() {
        RemoteReviewRepository repository = new RemoteReviewRepository("http://localhost:1234/");
        assertEquals("http://localhost:1234", repository.getBaseUrl());
    }

    /**
     * Verifiserer at null som adresse kaster IllegalArgumentException.
     */
    @Test
    void testConstructorThrowsWhenBaseUrlIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new RemoteReviewRepository(null));
    }

    /**
     * Verifiserer at en blank adresse kaster IllegalArgumentException.
     */
    @Test
    void testConstructorThrowsWhenBaseUrlIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new RemoteReviewRepository(" "));
    }
}
