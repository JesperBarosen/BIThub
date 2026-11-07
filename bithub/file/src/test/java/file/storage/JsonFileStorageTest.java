package file.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// Laget med hjelp av KI, gjennomgått og tilpasset av gruppen.
/**
 * Tester at JsonFileStorage skriver via en midlertidig fil og bevarer eksisterende data ved
 * skrivefeil.
 */
class JsonFileStorageTest {

    @TempDir
    Path tempDir;

    /**
     * Teller de midlertidige filene som ligger igjen i mappen.
     *
     * @return antall filer som slutter på .tmp
     * @throws IOException hvis mappen ikke kan leses
     */
    private long countTemporaryFiles() throws IOException {
        try (Stream<Path> files = Files.list(tempDir)) {
            return files.filter(file -> file.toString().endsWith(".tmp")).count();
        }
    }

    /**
     * Verifiserer at skrivingen erstatter innholdet og ikke etterlater midlertidige filer.
     *
     * @throws IOException hvis filen ikke kan leses
     */
    @Test
    void testWriteReplacesFileWithoutLeavingTemporaryFile() throws IOException {
        Path file = tempDir.resolve("data.json");
        Files.writeString(file, "[\"gammel\"]");
        JsonFileStorage storage = new JsonFileStorage(file, new ObjectMapper());

        storage.update(() -> {
            storage.write(List.of("ny"));
            return null;
        });

        assertEquals("[\"ny\"]", Files.readString(file));
        assertEquals(0, countTemporaryFiles());
    }

    /**
     * Verifiserer at den opprinnelige filen er uendret når JSON-skrivingen feiler, og at den
     * midlertidige filen slettes. Et tomt Object kan ikke gjøres om til JSON, så Jackson feiler
     * mens den midlertidige filen skrives.
     *
     * @throws IOException hvis filen ikke kan leses
     */
    @Test
    void testFailedWriteKeepsOriginalFile() throws IOException {
        Path file = tempDir.resolve("data.json");
        Files.writeString(file, "[\"gammel\"]");
        JsonFileStorage storage = new JsonFileStorage(file, new ObjectMapper());

        assertThrows(UncheckedIOException.class, () -> storage.update(() -> {
            storage.write(new Object());
            return null;
        }));

        assertEquals("[\"gammel\"]", Files.readString(file));
        assertEquals(0, countTemporaryFiles());
    }

    /**
     * Verifiserer at skriving til en sti som er en mappe gir feil, og at mappen ikke erstattes.
     *
     * @throws IOException hvis mappen ikke kan opprettes
     */
    @Test
    void testWriteToDirectoryFails() throws IOException {
        Path directory = Files.createDirectories(tempDir.resolve("mappe"));
        JsonFileStorage storage = new JsonFileStorage(directory, new ObjectMapper());

        assertThrows(UncheckedIOException.class, () -> storage.write(List.of()));
        assertTrue(Files.isDirectory(directory));
    }

    /**
     * Verifiserer at en sti uten overordnet mappe avvises.
     */
    @Test
    void testRootPathIsRejected() {
        Path root = tempDir.getRoot();

        assertThrows(IllegalArgumentException.class,
                () -> new JsonFileStorage(root, new ObjectMapper()));
    }

    /**
     * Verifiserer at null som sti eller ObjectMapper avvises.
     */
    @Test
    void testNullArgumentsAreRejected() {
        Path file = tempDir.resolve("data.json");

        assertThrows(IllegalArgumentException.class,
                () -> new JsonFileStorage(null, new ObjectMapper()));
        assertThrows(IllegalArgumentException.class, () -> new JsonFileStorage(file, null));
    }
}
