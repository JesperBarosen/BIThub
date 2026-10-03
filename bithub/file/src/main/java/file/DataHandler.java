package file;

import java.util.List;

/**
 * Definerer operasjoner for lesing, skriving og kontroll av lagrede data.
 *
 * @param <T> typen data som håndteres
 */
public interface DataHandler<T> {

    /**
     * Leser data fra lagring.
     *
     * @return listen med lagrede data
     */
    List<T> readFromFile();

    /**
     * Skriver data til lagring.
     *
     * @param data dataene som skal lagres
     */
    void writeToFile(List<T> data);

    /**
     * Sjekker om lagringen er tom.
     *
     * @return true dersom lagringen er tom, ellers false
     */
    boolean isEmpty();
}