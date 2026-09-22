package file;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import core.User;

class UserHandlerTest {
  /**
   * JUnit lager en midlertidig mappe for hver test og sletter den etter.
   */
  @TempDir
  Path tempDir;

  @Test
  void userIsSavedAndCanBeRead() {
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

  @Test
  void addingMultipleUsersKeepsAllOfThem() {
    Path file = tempDir.resolve("users.json");
    UserHandler handler = new UserHandler(file);

    handler.addUser(new User("Ola Nordmann", "ola@gmail.com", "passord1"));
    handler.addUser(new User("Ikke Ola", "ikkeOla@gmail.com", "ikkePassord1"));

    List<UserDTO> users = handler.readFromFile();
    assertEquals(2, users.size());
    assertEquals("Ola Nordmann", users.get(0).getName());
    assertEquals("Ikke Ola", users.get(1).getName());

  }
}