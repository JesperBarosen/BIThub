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
 * Håndterer sikker lesing og skriving av JSON- filer.
 * 
 * Klassen benytter seg av låsing for å beskytte data
 * mot operasjoner som kjøres samtidig, og skriver en
 * midlertidig fil før den opprinnelige filen erstattes.
 * 
 * Låsingen gjelder mellom instanser i samme serverprosess.
 */
final class JsonFileStorage {

    private static final ConcurrentHashMap<Path, ReentrantReadWriteLock> LOCKS = new ConcurrentHashMap<>();

    private final Path path;
    private final ObjectMapper objectMapper;
    private final ReentrantReadWriteLock lock;

    /**
     * Oppretter lagringshjelper for en JSON- fil.
     * 
     * @param path stien til JSON- filen
     * @param objectMapper brukes til å lese og skrive JSON
     * @throws IllegalArgumentException hvis stien eller ObjectMapper er null
     */
    JsonFileStorage(Path path, ObjectMapper objectMapper) {
        if (path == null || objectMapper == null) {
            throw new IllegalArgumentException("Path og ObjectMapper kan ikke være null");
        }

        this.path = path.toAbsolutePath().normalize();
        this.objectMapper = objectMapper;
        this.lock = LOCKS.computeIfAbsent(this.path, ignored -> new ReentrantReadWriteLock());
    }

    /**
     * Leser data fra filen med en leselås.
     * 
     * @param operation operasjonen som skal utføres under låsing
     * @param <T> typen data operasojnen returnerer
     * @return resultatet av leseoperasjonen
     * @throws UncheckedIOException hvis lesningen feiler 
     */
    <T> T read(IOCallable<T> operation) {
        lock.readLock().lock();
        try {
            return operation.call();
        } catch (IOException e) {
            throw new UncheckedIOException("Kunne ikke lese JSON fil: " + path, e);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gjør en oppdatering med skrivelås.
     * 
     * Beskytter operasjonen mot oppdateringer som skjer samtidig
     * fra andre instanser som bruker samme filstil og lås.
     * 
     * @param operation operasjonen som skal utføres under låsing
     * @param <T> typen data operasjonen returnerer
     * @return resultatet av oppdateringen
     * @throws UncheckedIOException hvis oppdateringen feiler
     */
    <T> T update(IOCallable<T> operation) {
        lock.writeLock().lock();
        try {
            return operation.call();
        } catch (IOException e) {
            throw new UncheckedIOException("Kunne ikke oppdatere JSON fil: " + path, e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Skriver data til en midlertidig fil før den erstatter den opprinnelige filen.
     * 
     * @param data dataene som skal lagres som JSON
     * @throws UncheckedIOException hvis skrivingen eller filbyttet feiler
     */
    void write(Object data) {
        Path temporaryFile = null;
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            temporaryFile = Files.createTempFile(parent, path.getFileName().toString(), ".tmp");

            objectMapper.writeValue(temporaryFile.toFile(), data);

            try {
                Files.move(temporaryFile, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Kunne ikke skrive JSON fil på en sikker måte: " + path, e);
        } finally {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException ignored) {
                    // Ignorerer feilen ved sletting av den midlertidig filen
                    // sånn at den opprinnelige feilen ikke overskrives.
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
    interface IOCallable<T> {
        T call() throws IOException;
    }

}
