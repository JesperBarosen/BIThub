package file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import core.User;

/*
Håndterer persistens av brukerdata til og fra JSON.
 */
public final class UserHandler implements DataHandler<UserDTO>{

    private final Path path;
    private final ObjectMapper objectMapper;

    public UserHandler() {
        this(getDefaultPath("users.json"));
    }

    public UserHandler(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        this.path = path;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public boolean isEmpty() {
        if (!Files.exists(this.path)) {
            return true;
        }

        try {
            return Files.size(this.path) == 0;
        } catch (IOException e) {
            return true;
        }
    }

    /*
    Leser eksisterende brukere fra JSON-filen.
     */
    @Override
    public List<UserDTO> readFromFile() {
        if (isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(
                    this.path.toFile(),
                    objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, UserDTO.class)
            );
        } catch (IOException e) {
            throw new RuntimeException(
                    "Feil ved lesing av JSON-fil: " + this.path, e
            );
        }
    }

    @Override
    public void writeToFile(List<UserDTO> data) {
        try {
            Path parent = this.path.getParent();

            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            objectMapper.writeValue(this.path.toFile(), data);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Feil ved skriving til JSON-fil: " + this.path, e
            );
        }
    }

    public static Path getDefaultPath(String fileName) {
        return Path.of(
                System.getProperty("user.home"),
                ".bithub",
                fileName
        );
    }

    public void addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        List<UserDTO> users = readFromFile();

        UserDTO userDto = new UserDTO(
                user.getName(),
                user.getEmail(),
                user.getPassword()
        );

        users.add(userDto);
        writeToFile(users);
    }
}
