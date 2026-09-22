package file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import core.User;

class UserHandlerTest {
  /**
   * JUnit lager en midlertidig mappe for hver test og sletter den etter.
   */
  @TempDir
  Path tempDir;

  /**
   * Verifiserer at en bruker som er lagt til kan leses tilbake av en ny UserHandler.
   */
  @Test
  void testUserIsSavedAndCanBeRead() {
    Path file = tempDir.resolve("users.json");
    UserHandler handler = new UserHandler(file);
    handler.addUser(new User("Ola Nordmann", "ola@gmail.com", "jegharmangepassord123"));

    // Ny handler på samme fil som beviser at dataene som faktisk ble lagret på
    // disken
    List<UserDTO> users = new UserHandler(file).readFromFile();

    assertEquals(1, users.size());
    assertEquals("Ola Nordmann", users.get(0).getName());
    assertEquals("ola@gmail.com", users.get(0).getEmail());
    assertEquals("jegharmangepassord123", users.get(0).getPassword());
  }

  /**
   * Verifiserer at flere brukere kan legges til etter hverandre uten at brukere
   * fra tidligere forsvinner.
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
   * Tester isEmpty når filen ikke eksisterer.
   */
  @Test
  void testIsEmptyWithNonExistentFile() {
    Path file = tempDir.resolve("randomfile.json");
    UserHandler handler = new UserHandler(file);
    assertTrue(handler.isEmpty());
  }

  /**
   * Tester isEmpty når stien peker på en mappe og ikke en fil. Files.size() vil
   * da kaste en IOException, som fanges og gjør at isEmpty returnerer true.
   */
  @Test
  void testIsEmptyThrowsIOExceptionWhenPathIsDirectory() throws IOException {
    Path dir = tempDir.resolve("randomDirectory");
    Files.createDirectories(dir);

    UserHandler handler = new UserHandler(dir);
    assertTrue(handler.isEmpty());
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
}