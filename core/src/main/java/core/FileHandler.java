package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Håndterer lesing fra og skriving til fil.
 */
public final class FileHandler {

  private final Path path;
  private static final String LINE_SEPARATOR = "\r\n";

  /**
   * Oppretter en filhåndterer for den angitte filstien.
   *
   * @param path filstien som skal brukes
   * @throws IllegalArgumentException dersom filstien er null eller tom
   */
  public FileHandler(String path) {
    if (path == null || path.isBlank()) {
      throw new IllegalArgumentException("Path cannot be empty");
    }
    this.path = Path.of(path);
  }

  /**
   * Leser alle linjene fra filen og deler dem opp i felter.
   *
   * @return en liste med feltene fra hver linje
   */
  public List<String[]> readFile() {
    List<String[]> lines = new ArrayList<>();

    /* Bruker BufferedReader for effektiv lesing fra fil. */
    /* Fanger feil ved lesing av fil. */
    /* Bruker unchecked exception for å unngå at alle metoder som kaller
       denne metoden må ha en checked exception. */
    try (BufferedReader reader = Files.newBufferedReader(this.path)) {
      String line;

      /* Legger til hver linje fra filen i lines-listen. */
      while ((line = reader.readLine()) != null) {
        lines.add(line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"));
      }
    } catch (IOException e) {
      throw new RuntimeException("Feil med lesing av fil", e);
    }

    return lines;
  }

  /**
   * Legger til en ny linje i filen.
   *
   * @param line feltene som skal skrives til filen
   */
  public void addToFile(String[] line) {
    /* Linjen som skal skrives. */
    String lineToWrite = String.join(",", line) + LINE_SEPARATOR;

    /* Skriver til fil. */
    try (BufferedWriter writer = Files.newBufferedWriter(
        this.path,
        StandardCharsets.UTF_8,
        StandardOpenOption.APPEND
    )) {
      writer.write(lineToWrite);
    } catch (IOException e) {
      throw new RuntimeException("Feil med skriving til fil", e);
    }
  }
}