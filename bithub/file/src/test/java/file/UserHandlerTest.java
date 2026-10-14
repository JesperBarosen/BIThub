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
     * Verifiserer at en bruker lagres med hash og salt og kan leses tilbake.
     */
    @Test
    void testUserIsSavedAndCanBeRead() throws IOException {
        Path file = tempDir.resolve("users.json");
        UserHandler handler = new UserHandler(file);

        String password = "jegharmangepassord123";
        User user = new User("Ola Nordmann", "ola@gmail.com", password);
        handler.addUser(user);

        // Ny handler på samme fil viser at dataene ble lagret på disken.
        List<UserDTO> users = new UserHandler(file).readFromFile();

        assertEquals(1, users.size());
        assertEquals("Ola Nordmann", users.get(0).getName());
        assertEquals("ola@gmail.com", users.get(0).getEmail());
        assertEquals(user.getPasswordHash(), users.get(0).getPasswordHash());
        assertEquals(user.getSalt(), users.get(0).getSalt());

        String json = Files.readString(file);
        assertTrue(json.contains("\"passwordHash\""));
        assertTrue(json.contains("\"salt\""));
        assertFalse(json.contains("\"password\""));
        assertFalse(json.contains(password));
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

        User user = new User("Kari", "kari@gmail.com", "passord123");
        UserDTO userDto = new UserDTO(user.getName(), user.getEmail(), user.getPasswordHash(),
                user.getSalt());

        handler.writeToFile(List.of(userDto));

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
     * Verifiserer at lagrede brukere gjenopprettes med riktig hash og salt.
     */
    @Test
    void testGetUsersReturnsSavedUsers() {
        Path file = tempDir.resolve("users.json");
        UserHandler handler = new UserHandler(file);

        User ola = new User("Ola Nordmann", "ola@gmail.com", "passord123");
        User kari = new User("Kari Nordmann", "kari@gmail.com", "passord456");

        handler.addUser(ola);
        handler.addUser(kari);

        List<User> users = new UserHandler(file).getUsers();

        assertEquals(2, users.size());

        assertEquals(ola.getName(), users.get(0).getName());
        assertEquals(ola.getEmail(), users.get(0).getEmail());
        assertEquals(ola.getPasswordHash(), users.get(0).getPasswordHash());
        assertEquals(ola.getSalt(), users.get(0).getSalt());
        assertTrue(users.get(0).verifyPassword("passord123"));
        assertFalse(users.get(0).verifyPassword("feilpassord"));

        assertEquals(kari.getName(), users.get(1).getName());
        assertEquals(kari.getEmail(), users.get(1).getEmail());
        assertEquals(kari.getPasswordHash(), users.get(1).getPasswordHash());
        assertEquals(kari.getSalt(), users.get(1).getSalt());
        assertTrue(users.get(1).verifyPassword("passord456"));
        assertFalse(users.get(1).verifyPassword("feilpassord"));
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

        User user = new User("Ola Nordmann", "ola@gmail.com", "passord123");
        UserDTO invalidUser = new UserDTO(user.getName(), "ugyldig-epost", user.getPasswordHash(),
                user.getSalt());

        handler.writeToFile(List.of(invalidUser));

        assertThrows(IllegalArgumentException.class, handler::getUsers);
    }

    /**
     * Verifiserer at en ugyldig lagret passordhash avvises.
     */
    @Test
    void testGetUsersRejectsInvalidStoredHash() {
        UserHandler handler = new UserHandler(tempDir.resolve("users.json"));

        User user = new User("Ola Nordmann", "ola@gmail.com", "passord123");
        UserDTO invalidUser = new UserDTO(user.getName(), user.getEmail(), "ugyldig-hash",
                user.getSalt());

        handler.writeToFile(List.of(invalidUser));

        assertThrows(IllegalArgumentException.class, handler::getUsers);
    }

    /**
     * Verifiserer at et ugyldig lagret salt avvises.
     */
    @Test
    void testGetUsersRejectsInvalidStoredSalt() {
        UserHandler handler = new UserHandler(tempDir.resolve("users.json"));

        User user = new User("Ola Nordmann", "ola@gmail.com", "passord123");
        UserDTO invalidUser = new UserDTO(user.getName(), user.getEmail(), user.getPasswordHash(),
                "ugyldig-salt");

        handler.writeToFile(List.of(invalidUser));

        assertThrows(IllegalArgumentException.class, handler::getUsers);
    }

    /**
     * Verifiserer at eldre brukerdata ikke overskrives ved lagring.
     */
    @Test
    void testAddUserDoesNotOverwriteLegacyPasswordData() throws IOException {
        Path file = tempDir.resolve("users.json");

        String legacyJson = """
                [
                  {
                    "name": "Ola Nordmann",
                    "email": "ola@gmail.com",
                    "password": "gammeltpassord123"
                  }
                ]
                """;

        Files.writeString(file, legacyJson);

        UserHandler handler = new UserHandler(file);
        User user = new User("Kari Nordmann", "kari@gmail.com", "passord456");

        assertThrows(RuntimeException.class, () -> handler.addUser(user));
        assertEquals(legacyJson, Files.readString(file));
    }
}