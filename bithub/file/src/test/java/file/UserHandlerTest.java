package file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import core.User;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

/**
 * Testklasse for UserHandler.
 *
 * <p>Verifiserer lagring, lesing og feilhåndtering for brukerdata.
 */
class UserHandlerTest {

    /**
     * JUnit lager en midlertidig mappe for hver test og sletter den etter.
     */
    @TempDir
    Path tempDir;

    /**
     * Verifiserer at standardkonstruktør og getDefaultPath oppretter en gyldig sti.
     */
    @Test
    void testDefaultConstructorAndGetDefaultPath() {
        UserHandler handler = new UserHandler();
        assertNotNull(handler);

        Path defaultPath = UserHandler.getDefaultPath("test.json");

        assertNotNull(defaultPath);
        assertTrue(defaultPath.endsWith("test.json"));
    }

    /**
     * Verifiserer at konstruktøren kaster IllegalArgumentException ved null som sti.
     */
    @Test
    void testConstructorThrowsOnNullPath() {
        assertThrows(IllegalArgumentException.class, () -> new UserHandler(null));
    }

    /**
     * Verifiserer at en lagret bruker kan leses tilbake av en ny UserHandler.
     */
    @Test
    void testUserIsSavedAndCanBeRead() {
        Path file = tempDir.resolve("users.json");
        UserHandler handler = new UserHandler(file);

        handler.addUser(new User("Ola Nordmann", "ola@gmail.com", "jegharmangepassord123"));

        // Ny handler på samme fil viser at dataene ble lagret på disken.
        List<UserDTO> users = new UserHandler(file).readFromFile();

        assertEquals(1, users.size());
        assertEquals("Ola Nordmann", users.get(0).getName());
        assertEquals("ola@gmail.com", users.get(0).getEmail());
        assertEquals("jegharmangepassord123", users.get(0).getPassword());
    }

    /**
     * Verifiserer at flere brukere kan legges til uten at tidligere brukere forsvinner.
     */
    @Test
    void testAddingMultipleUsersKeepsAllOfThem() {
        Path file = tempDir.resolve("users.json");
        UserHandler handler = new UserHandler(file);

        handler.addUser(new User("Ola Nordmann", "ola@gmail.com", "passord1"));
        handler.addUser(new User("Ikke Ola", "ikkeOla@gmail.com", "ikkePassord1"));

        List<UserDTO> users = handler.readFromFile();

        assertEquals(2, users.size());
        assertEquals("Ola Nordmann", users.get(0).getName());
        assertEquals("Ikke Ola", users.get(1).getName());
    }

    /**
     * Verifiserer at addUser kaster IllegalArgumentException hvis brukeren er null.
     */
    @Test
    void testAddUserThrowsOnNull() {
        Path file = tempDir.resolve("users.json");
        UserHandler handler = new UserHandler(file);

        assertThrows(IllegalArgumentException.class, () -> handler.addUser(null));
    }

    /**
     * Tester isEmpty når filen ikke eksisterer.
     */
    @Test
    void testIsEmptyWithNonExistentFile() {
        Path file = tempDir.resolve("randomfile.json");
        UserHandler handler = new UserHandler(file);

        assertTrue(handler.isEmpty());
    }

    /**
     * Tester isEmpty når filen eksisterer og har innhold.
     */
    @Test
    void testIsEmptyWithPopulatedFile() {
        Path file = tempDir.resolve("users.json");
        UserHandler handler = new UserHandler(file);

        handler.addUser(new User("Ola Nordmann", "ola@gmail.com", "passord123"));

        assertFalse(handler.isEmpty());
    }

    /**
     * Tester feilhåndtering når filstørrelsen ikke kan leses.
     */
    @Test
    void testIsEmptyReturnsTrueWhenSizeThrowsIoException() throws IOException {
        Path file = tempDir.resolve("unreadable.json");
        UserHandler handler = new UserHandler(file);

        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.exists(file)).thenReturn(true);
            files.when(() -> Files.size(file))
                    .thenThrow(new IOException("Kan ikke lese filstørrelsen"));

            assertTrue(handler.isEmpty());
            files.verify(() -> Files.size(file));
        }
    }

    /**
     * Tester isEmpty når filen eksisterer, men er tom.
     */
    @Test
    void testIsEmptyWithEmptyFile() throws IOException {
        Path file = tempDir.resolve("empty.json");
        Files.createFile(file);
        UserHandler handler = new UserHandler(file);

        assertTrue(handler.isEmpty());
    }

    /**
     * Verifiserer at writeToFile automatisk oppretter foreldermappen dersom den ikke eksisterer fra
     * før.
     */
    @Test
    void testWriteToFileCreatesParentDirectories() {
        Path nestedFile = tempDir.resolve("subfolder").resolve("nested_users.json");
        UserHandler handler = new UserHandler(nestedFile);

        handler.writeToFile(List.of(new UserDTO("Kari", "kari@gmail.com", "passord")));

        assertTrue(Files.exists(nestedFile));
        assertEquals(1, handler.readFromFile().size());
    }

    /**
     * Verifiserer at readFromFile kaster RuntimeException dersom JSON-filen har ugyldig format
     * eller ikke kan leses.
     */
    @Test
    void testReadFromFileThrowsRuntimeExceptionOnCorruptedJson() throws IOException {
        Path file = tempDir.resolve("corrupted.json");
        Files.writeString(file, "{ korrupt json content }");

        UserHandler handler = new UserHandler(file);

        assertThrows(RuntimeException.class, handler::readFromFile);
    }

    /**
     * Verifiserer at writeToFile kaster RuntimeException dersom det oppstår en feil ved skriving
     * til fil.
     */
    @Test
    void testWriteToFileThrowsRuntimeExceptionOnIoException() throws IOException {
        Path dir = tempDir.resolve("invalid_write_target");
        Files.createDirectories(dir);

        UserHandler handler = new UserHandler(dir);

        assertThrows(RuntimeException.class, () -> handler.writeToFile(List.of()));
    }

    /**
     * Verifiserer at lagrede brukere hentes som domeneobjekter.
     */
    @Test
    void testGetUsersReturnsSavedUsers() {
        Path file = tempDir.resolve("users.json");
        UserHandler handler = new UserHandler(file);

        handler.addUser(new User("Ola Nordmann", "ola@gmail.com", "passord123"));
        handler.addUser(new User("Kari Nordmann", "kari@gmail.com", "passord456"));

        List<User> users = new UserHandler(file).getUsers();

        assertEquals(2, users.size());
        assertEquals("Ola Nordmann", users.get(0).getName());
        assertEquals("ola@gmail.com", users.get(0).getEmail());
        assertEquals("passord123", users.get(0).getPassword());
        assertEquals("Kari Nordmann", users.get(1).getName());
        assertEquals("kari@gmail.com", users.get(1).getEmail());
        assertEquals("passord456", users.get(1).getPassword());
    }

    /**
     * Verifiserer at manglende datafil gir en tom brukerliste.
     */
    @Test
    void testGetUsersReturnsEmptyListWhenFileDoesNotExist() {
        UserHandler handler = new UserHandler(tempDir.resolve("users.json"));

        assertTrue(handler.getUsers().isEmpty());
    }

    /**
     * Verifiserer at ugyldige lagrede opplysninger avvises ved konvertering.
     */
    @Test
    void testGetUsersRejectsInvalidStoredUser() {
        UserHandler handler = new UserHandler(tempDir.resolve("users.json"));
        handler.writeToFile(List.of(new UserDTO("Ola Nordmann", "ugyldig-epost", "passord123")));

        assertThrows(IllegalArgumentException.class, handler::getUsers);
    }
}