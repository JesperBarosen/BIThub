package file;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Håndterer sikker lesing og skriving av JSON-filer.
 *
 * <p>Klassen bruker låsing for å beskytte data mot operasjoner som kjøres samtidig, og skriver til
 * en midlertidig fil før den opprinnelige filen erstattes. Alle instanser med samme fil deler samme
 * lås.
 *
 * <p>Låsingen gjelder bare innenfor én prosess. To prosesser som skriver til samme fil, er ikke
 * beskyttet mot hverandre.
 */
final class JsonFileStorage {

    private static final ConcurrentHashMap<Path, ReentrantReadWriteLock> LOCKS =
        new ConcurrentHashMap<>();

    private final Path path;
    private final Path directory;
    private final String fileName;
    private final ObjectMapper objectMapper;
    private final ReentrantReadWriteLock lock;

    /**
     * Oppretter lagringshjelper for en JSON-fil.
     *
     * @param path         stien til JSON-filen
     * @param objectMapper brukes til å lese og skrive JSON
     * @throws IllegalArgumentException hvis stien eller ObjectMapper er null, eller stien ikke
     *                                      peker på en fil i en mappe
     */
    JsonFileStorage(Path path, ObjectMapper objectMapper) {
        if (path == null || objectMapper == null) {
            throw new IllegalArgumentException("Path og ObjectMapper kan ikke være null");
        }

        Path normalized = path.toAbsolutePath().normalize();
        Path parent = normalized.getParent();
        Path name = normalized.getFileName();
        if (parent == null || name == null) {
            throw new IllegalArgumentException("Stien må peke på en fil i en mappe: " + path);
        }

        this.path = normalized;
        this.directory = parent;
        this.fileName = name.toString();
        this.objectMapper = objectMapper;
        this.lock = LOCKS.computeIfAbsent(this.path, ignored -> new ReentrantReadWriteLock());
    }

    /**
     * Leser data fra filen med en leselås. Flere lesinger kan skje samtidig, men ikke mens en
     * oppdatering pågår.
     *
     * <p>Operasjonen kan ikke kalle {@link #update(IoCallable)}, fordi en leselås ikke kan gjøres
     * om til en skrivelås.
     *
     * @param operation operasjonen som skal utføres under låsing
     * @param <T>       typen data operasjonen returnerer
     * @return resultatet av leseoperasjonen
     * @throws UncheckedIOException hvis lesingen feiler
     */
    <T> T read(IoCallable<T> operation) {
        lock.readLock().lock();
        try {
            return operation.call();
        } catch (IOException e) {
            throw new UncheckedIOException("Kunne ikke lese JSON-fil: " + path, e);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gjør en hel les–endre–skriv-operasjon med skrivelås.
     *
     * <p>Ingen andre kan lese eller skrive filen mens operasjonen pågår. Samme tråd kan ta låsen
     * flere ganger, så operasjonen kan kalle andre metoder som også bruker låsen.
     *
     * @param operation operasjonen som skal utføres under låsing
     * @param <T>       typen data operasjonen returnerer
     * @return resultatet av oppdateringen
     * @throws UncheckedIOException hvis oppdateringen feiler
     */
    <T> T update(IoCallable<T> operation) {
        lock.writeLock().lock();
        try {
            return operation.call();
        } catch (IOException e) {
            throw new UncheckedIOException("Kunne ikke oppdatere JSON-fil: " + path, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Skriver data til en midlertidig fil i samme mappe, og erstatter deretter den opprinnelige
     * filen. Feiler skrivingen, er den opprinnelige filen uendret, og den midlertidige filen
     * slettes.
     *
     * <p>Metoden skal kalles inne i {@link #update(IoCallable)}.
     *
     * @param data dataene som skal lagres som JSON
     * @throws UncheckedIOException hvis skrivingen eller filbyttet feiler
     */
    void write(Object data) {
        Path temporaryFile = null;
        try {
            if (Files.isDirectory(path)) {
                throw new IOException("Stien er en mappe, ikke en fil: " + path);
            }
            Files.createDirectories(directory);
            temporaryFile = Files.createTempFile(directory, fileName, ".tmp");

            objectMapper.writeValue(temporaryFile.toFile(), data);

            try {
                Files.move(temporaryFile, path, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Kunne ikke skrive JSON-fil på en sikker måte: " + path,
                    e);
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException ignored) {
                    // Ignorerer feilen ved sletting av den midlertidige filen,
                    // slik at den opprinnelige feilen ikke overskrives.
                }
            }
        }
    }

    /**
     * Representerer en operasjon som kan kaste IOException.
     *
     * @param <T> typen data operasjonen returnerer
     */
    @FunctionalInterface
    interface IoCallable<T> {

        /**
         * Utfører operasjonen.
         *
         * @return resultatet av operasjonen
         * @throws IOException hvis operasjonen feiler
         */
        T call() throws IOException;
    }
}
