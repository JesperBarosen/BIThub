package file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import core.exception.DuplicateEmailException;
import core.model.User;
import core.repository.UserRepository;
import file.dto.UserDTO;
import file.storage.DataHandler;
import file.storage.JsonFileStorage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Håndterer persistens av brukerdata til og fra JSON.
 */
public final class UserHandler implements DataHandler<UserDTO>, UserRepository {

    private final Path path;
    private final ObjectMapper objectMapper;
    private final JsonFileStorage storage;

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
            throw new IllegalArgumentException("Sti kan ikke være null.");
        }

        this.path = path;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.storage = new JsonFileStorage(path, this.objectMapper);
    }

    /**
     * Sjekker om JSON-filen enten ikke eksisterer eller er tom.
     *
     * @return true hvis filen ikke finnes eller har en størrelse på 0 bytes, ellers false
     * @throws UncheckedIOException hvis filstørrelsen ikke kan leses
     */
    @Override
    public boolean isEmpty() {
        return storage.read(this::isFileEmpty);
    }

    /**
     * Leser eksisterende brukere fra JSON-filen.
     *
     * @return listen med lagrede brukere
     * @throws UncheckedIOException hvis filen ikke kan leses
     */
    @Override
    public List<UserDTO> readFromFile() {
        return storage.read(this::readUsers);
    }

    /**
     * Skriver brukerdata til JSON-filen, via en midlertidig fil.
     *
     * @param data listen med brukere som skal lagres
     * @throws UncheckedIOException hvis filen ikke kan skrives
     */
    @Override
    public void writeToFile(List<UserDTO> data) {
        storage.update(() -> {
            storage.write(data);
            return null;
        });
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
     * Legger til en bruker og lagrer den oppdaterte brukerlisten. Hele lesingen, endringen og
     * skrivingen skjer med skrivelås.
     *
     * @param user brukeren som skal legges til
     * @throws IllegalArgumentException dersom brukeren er null
     */
    public void addUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Brukeren kan ikke være null.");
        }

        storage.update(() -> {
            List<UserDTO> users = readUsers();
            users.add(toDto(user));
            storage.write(users);
            return null;
        });
    }

    /**
     * Legger til brukeren bare hvis ingen bruker har samme e-postadresse fra før. Sjekken og
     * lagringen skjer under samme skrivelås, slik at to samtidige registreringer med samme e-post
     * ikke begge kan lykkes. Store og små bokstaver i e-posten skilles ikke.
     *
     * @param user brukeren som skal legges til
     * @return true hvis brukeren ble lagret, false hvis e-postadressen allerede er registrert
     * @throws IllegalArgumentException dersom brukeren er null
     */
    public boolean addUserIfEmailAvailable(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Brukeren kan ikke være null.");
        }

        return storage.update(() -> {
            List<UserDTO> users = readUsers();
            boolean emailTaken = users.stream()
                    .anyMatch(stored -> user.getEmail().equalsIgnoreCase(stored.getEmail()));
            if (emailTaken) {
                return false;
            }
            users.add(toDto(user));
            storage.write(users);
            return true;
        });
    }

    /**
     * Lagrer en ny bruker med {@link #addUserIfEmailAvailable(User)}, slik at sjekken av
     * e-postadressen og lagringen skjer under samme skrivelås.
     *
     * @param user brukeren som skal lagres
     * @throws DuplicateEmailException hvis e-postadressen allerede er registrert
     * @throws IllegalArgumentException hvis brukeren er null
     */
    @Override
    public void save(User user) {
        if (!addUserIfEmailAvailable(user)) {
            throw new DuplicateEmailException("E-postadressen er allerede registrert.");
        }
    }

    /**
     * Erstatter den lagrede brukeren som har samme e-postadresse. Lesingen, endringen og skrivingen
     * skjer under samme skrivelås, slik at samtidige endringer ikke overskriver hverandre. Store og
     * små bokstaver i e-posten skilles ikke.
     *
     * @param user brukeren med de nye opplysningene
     * @throws IllegalArgumentException hvis brukeren er null, eller ingen bruker med denne
     *                                  e-postadressen er lagret
     */
    @Override
    public void update(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Brukeren kan ikke være null.");
        }

        storage.update(() -> {
            List<UserDTO> users = readUsers();
            boolean updated = false;
            for (int i = 0; i < users.size(); i++) {
                UserDTO stored = users.get(i);
                if (user.getEmail().equalsIgnoreCase(stored.getEmail())) {
                    users.set(i, toDto(user));
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                throw new IllegalArgumentException(
                    "Ingen bruker med denne e-postadressen er lagret.");
            }
            storage.write(users);
            return null;
        });
    }

    /**
     * Finner brukeren med den oppgitte e-postadressen. Ignorerer store og små bokstaver, slik
     * at også brukere som er lagret med store bokstaver blir funnet.
     *
     * @param email e-postadressen det skal søkes etter
     * @return brukeren hvis den finnes, ellers et tomt resultat
     * @throws UncheckedIOException hvis filen ikke kan leses
     * @throws IllegalArgumentException hvis den lagrede brukeren har ugyldige opplysninger
     */
    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return readFromFile().stream()
                .filter(stored -> email.equalsIgnoreCase(stored.getEmail()))
                .findFirst()
                .map(UserHandler::toUser);
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
     * Sjekker om filen mangler eller er tom. Skal kalles mens låsen holdes.
     *
     * @return true hvis filen ikke finnes eller er tom
     * @throws IOException hvis filstørrelsen ikke kan leses
     */
    private boolean isFileEmpty() throws IOException {
        return !Files.exists(this.path) || Files.size(this.path) == 0;
    }

    /**
     * Leser brukerne fra filen. Skal kalles mens låsen holdes.
     *
     * @return listen med lagrede brukere, eller en tom liste hvis filen mangler eller er tom
     * @throws IOException hvis filen ikke kan leses
     */
    private List<UserDTO> readUsers() throws IOException {
        if (isFileEmpty()) {
            return new ArrayList<>();
        }
        return objectMapper.readValue(this.path.toFile(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, UserDTO.class));
    }

    /**
     * Gjør om en bruker til formatet som lagres i filen.
     *
     * @param user brukeren som skal lagres
     * @return brukeren som UserDTO
     */
    private static UserDTO toDto(User user) {
        return new UserDTO(user.getName(), user.getEmail(), user.getPasswordHash(), user.getSalt());
    }

    /**
     * Gjør om lagrede brukerdata til et domeneobjekt.
     *
     * @param userDto brukeren slik den er lagret i filen
     * @return brukeren som domeneobjekt
     * @throws IllegalArgumentException hvis brukeropplysningene er ugyldige
     */
    private static User toUser(UserDTO userDto) {
        return new User(userDto.getName(), userDto.getEmail(), userDto.getPasswordHash(),
                userDto.getSalt());
    }
}