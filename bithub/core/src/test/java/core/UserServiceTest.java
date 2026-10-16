package core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Tester UserService med lagring i minnet uten filer eller brukergrensesnitt.
 */
class UserServiceTest {

    private final List<User> savedUsers = new ArrayList<>();

    private final UserRepository fakeRepository = new UserRepository() {
        @Override
        public void save(User user) {
            savedUsers.add(user);
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return savedUsers.stream().filter(user -> user.getEmail().equals(email)).findFirst();
        }
    };

    private final UserService service = new UserService(fakeRepository);

    /**
     * Verifiserer at registrering lagrer og returnerer brukeren med riktige opplysninger og hashet
     * passord.
     */
    @Test
    void registerSavesAndReturnsUser() {
        String password = "Passord123";

        User user = service.register("Ola Nordmann", "ola@example.com", password);

        assertEquals(1, savedUsers.size());
        assertSame(user, savedUsers.get(0));
        assertEquals("Ola Nordmann", user.getName());
        assertEquals("ola@example.com", user.getEmail());
        assertNotEquals(password, user.getPasswordHash());
        assertTrue(user.verifyPassword(password));
    }

    /**
     * Verifiserer at en registrert e-post avvises uten å endre lagringen.
     */
    @Test
    void registerRejectsDuplicateEmail() {
        User original = service.register("Ola Nordmann", "ola@example.com", "Passord123");

        DuplicateEmailException exception = assertThrows(DuplicateEmailException.class,
                () -> service.register("Kari Nordmann", "ola@example.com", "AnnetPassord123"));

        assertEquals("E-postadressen er allerede registrert.", exception.getMessage());
        assertEquals(1, savedUsers.size());
        assertSame(original, savedUsers.get(0));
    }

    /**
     * Verifiserer at ulike e-postadresser kan registreres.
     */
    @Test
    void registerAllowsDifferentEmails() {
        User first = service.register("Ola Nordmann", "ola@example.com", "Passord123");
        User second = service.register("Kari Nordmann", "kari@example.com", "AnnetPassord123");

        assertEquals(2, savedUsers.size());
        assertSame(first, savedUsers.get(0));
        assertSame(second, savedUsers.get(1));
    }

    /**
     * Verifiserer at ugyldige navn avvises uten lagring.
     */
    @Test
    void registerRejectsInvalidName() {
        for (String name : new String[] { null, "", "   ", "Al", "Ola123" }) {
            assertThrows(IllegalArgumentException.class,
                    () -> service.register(name, "ola@example.com", "Passord123"));
        }

        assertTrue(savedUsers.isEmpty());
    }

    /**
     * Verifiserer at ugyldige e-postadresser avvises uten lagring.
     */
    @Test
    void registerRejectsInvalidEmail() {
        for (String email : new String[] { null, "", "   ", "ola", "ola@example",
            "ola nordmann@example.com" }) {
            assertThrows(IllegalArgumentException.class,
                    () -> service.register("Ola Nordmann", email, "Passord123"));
        }

        assertTrue(savedUsers.isEmpty());
    }

    /**
     * Verifiserer at ugyldige passord avvises uten lagring.
     */
    @Test
    void registerRejectsInvalidPassword() {
        for (String password : new String[] { null, "", "        ", "1234567" }) {
            assertThrows(IllegalArgumentException.class,
                    () -> service.register("Ola Nordmann", "ola@example.com", password));
        }

        assertTrue(savedUsers.isEmpty());
    }

    /**
     * Verifiserer at lagringsfeil sendes videre slik at registreringen ikke fremstår som vellykket
     * når lagring feiler.
     */
    @Test
    void registerPassesOnStorageFailure() {
        RuntimeException failure = new RuntimeException("Lagring feilet");

        UserRepository failingRepository = new UserRepository() {
            @Override
            public void save(User user) {
                throw failure;
            }

            @Override
            public Optional<User> findByEmail(String email) {
                return Optional.empty();
            }
        };

        UserService failingService = new UserService(failingRepository);

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> failingService.register("Ola Nordmann", "ola@example.com", "Passord123"));

        assertSame(failure, actual);
    }

    /**
     * Verifiserer at tjenesten ikke kan opprettes uten lagring.
     */
    @Test
    void constructorRejectsNullRepository() {
        assertThrows(NullPointerException.class, () -> new UserService(null));
    }

    /**
     * Verifiserer at innlogging med riktig e-post og passord returnerer brukeren.
     */
    @Test
    void loginReturnsUserWithCorrectPassword() {
        User user = service.register("Ola Nordmann", "ola@example.com", "Passord123");

        assertEquals(Optional.of(user), service.login("ola@example.com", "Passord123"));
    }

    /**
     * Verifiserer at innlogging med feil passord gir tomt resultat.
     */
    @Test
    void loginRejectsWrongPassword() {
        service.register("Ola Nordmann", "ola@example.com", "Passord123");

        assertTrue(service.login("ola@example.com", "FeilPassord").isEmpty());
    }

    /**
     * Verifiserer at innlogging med ukjent e-post gir tomt resultat.
     */
    @Test
    void loginRejectsUnknownEmail() {
        assertTrue(service.login("ukjent@example.com", "Passord123").isEmpty());
    }

    /**
     * Verifiserer at innlogging uten passord gir tomt resultat i stedet for unntak.
     */
    @Test
    void loginRejectsMissingPassword() {
        service.register("Ola Nordmann", "ola@example.com", "Passord123");

        for (String password : new String[] { null, "", " " }) {
            assertTrue(service.login("ola@example.com", password).isEmpty());
        }
    }

    /**
     * Verifiserer at e-post med store bokstaver regnes som samme adresse ved registrering.
     */
    @Test
    void registerRejectsDuplicateEmailWithDifferentCase() {
        service.register("Ola Nordmann", "Ola@Example.com", "Passord123");

        assertThrows(DuplicateEmailException.class,
                () -> service.register("Kari Nordmann", " ola@example.com ", "AnnetPassord123"));
        assertEquals(1, savedUsers.size());
    }

    /**
     * Verifiserer at innlogging virker uansett store og små bokstaver i e-posten.
     */
    @Test
    void loginIgnoresCaseInEmail() {
        service.register("Ola Nordmann", "ola@example.com", "Passord123");

        assertTrue(service.login("OLA@Example.com", "Passord123").isPresent());
    }

    /**
     * Verifiserer at manglende e-post avvises uten oppslag i lagringen.
     */
    @Test
    void loginRejectsMissingEmailWithoutRepositoryAccess() {
        UserRepository unusedRepository = new UserRepository() {
            @Override
            public void save(User user) {
                throw new AssertionError("Innlogging skal ikke lagre brukere.");
            }

            @Override
            public Optional<User> findByEmail(String email) {
                throw new AssertionError("Manglende e-post skal avvises før oppslag.");
            }
        };

        UserService loginService = new UserService(unusedRepository);

        for (String email : new String[] { null, "", "   " }) {
            assertTrue(loginService.login(email, "Passord123").isEmpty());
        }
    }
}