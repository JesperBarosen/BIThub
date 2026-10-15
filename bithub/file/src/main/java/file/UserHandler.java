package file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import core.User;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Håndterer persistens av brukerdata til og fra JSON.
 */
public final class UserHandler implements DataHandler<UserDTO> {

    private final Path path;
    private final ObjectMapper objectMapper;

    /**
     * Oppretter en UserHandler med standard filsti.
     */
    public UserHandler() {
        this(getDefaultPath("users.json"));
    }

    /**
     * Oppretter en UserHandler med en spesifisert filsti.
     *
     * @param path filstien som skal brukes til lagring og lesing av brukere
     * @throws IllegalArgumentException dersom filstien er null
     */
    public UserHandler(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }

        this.path = path;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Sjekker om JSON-filen enten ikke eksisterer eller er tom.
     *
     * @return true hvis filen ikke finnes eller har en størrelse på 0 bytes, ellers false
     * @throws UncheckedIOException hvis filstørrelsen ikke kan leses
     */
    @Override
    public boolean isEmpty() {
        if (!Files.exists(this.path)) {
            return true;
        }

        try {
            return Files.size(this.path) == 0;
        } catch (IOException e) {
            throw new UncheckedIOException("Kunne ikke lese filstørrelsen: " + this.path, e);
        }
    }

    /**
     * Leser eksisterende brukere fra JSON-filen.
     *
     * @return listen med lagrede brukere
     */
    @Override
    public List<UserDTO> readFromFile() {
        if (isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(this.path.toFile(), objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, UserDTO.class));
        } catch (IOException e) {
            throw new RuntimeException("Feil ved lesing av JSON-fil: " + this.path, e);
        }
    }

    /**
     * Skriver brukerdata til JSON-filen.
     *
     * @param data listen med brukere som skal lagres
     */
    @Override
    public void writeToFile(List<UserDTO> data) {
        try {
            Path parent = this.path.getParent();

            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            objectMapper.writeValue(this.path.toFile(), data);
        } catch (IOException e) {
            throw new RuntimeException("Feil ved skriving til JSON-fil: " + this.path, e);
        }
    }

    /**
     * Returnerer standard filbane for en gitt fil i brukerens hjemmemappe.
     *
     * @param fileName navnet på filen
     * @return filbanen under brukerens .bithub-mappe
     */
    public static Path getDefaultPath(String fileName) {
        return Path.of(System.getProperty("user.home"), ".bithub", fileName);
    }

    /**
     * Legger til en bruker og lagrer den oppdaterte brukerlisten.
     *
     * @param user brukeren som skal legges til
     * @throws IllegalArgumentException dersom brukeren er null
     */
    public void addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        List<UserDTO> users = readFromFile();

        UserDTO userDto = new UserDTO(user.getName(), user.getEmail(), user.getPassword());

        users.add(userDto);
        writeToFile(users);
    }

    /**
     * Henter lagrede brukere som validerte domeneobjekter.
     *
     * @return listen med lagrede brukere
     * @throws IllegalArgumentException hvis lagrede brukeropplysninger er ugyldige
     */
    public List<User> getUsers() {
        return readFromFile().stream().map(UserHandler::toUser).toList();
    }

    /**
     * Gjør om lagrede brukerdata til et domeneobjekt.
     *
     * @param userDto brukeren slik den er lagret i filen
     * @return brukeren som domeneobjekt
     * @throws IllegalArgumentException hvis brukeropplysningene er ugyldige
     */
    private static User toUser(UserDTO userDto) {
        return new User(userDto.getName(), userDto.getEmail(), userDto.getPassword());
    }
}