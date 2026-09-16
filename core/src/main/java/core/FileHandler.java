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

// Gjorde final for å unngå SpotBugs-feil
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

  // Sjekker om filen finnes fra før
  public boolean isEmpty() {
    if (!Files.exists(this.path)) {
      return true;
    }

    try {
      return Files.size(this.path) == 0;
    } catch (IOException e) {
      // Hvis det oppstår en I/O-feil, behandler vi den som tom/manglende
      return true;
    }
  }

  public List<String[]> readFile() {
    List<String[]> lines = new ArrayList<>();

    /* Bruker BufferedReader for effektiv lesing fra fil. */
    /* Fanger feil ved lesing av fil. */
    /* Bruker unchecked exception for å unngå at alle metoder som kaller
       denne metoden må ha en checked exception. */
    try (BufferedReader reader = Files.newBufferedReader(this.path)) {
      String line;

      /* Legger til hver linje fra filen inn i lines-listen. */
      while ((line = reader.readLine()) != null) {
        /*
         * Splitter med "," med unntak av komma inne i "..." som kan
         * forekomme i comment-feltet. Comment-feltet bør derfor pakkes
         * inn i "".
         */
        // Regex fra Gemini
        lines.add(line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"));
      }
    } catch (IOException e) {
      throw new RuntimeException("Feil med lesing av fil", e);
    }

    return lines;
  }

  public void addToFile(String[] line) {
    String lineToWrite = String.join(",", line) + LINE_SEPARATOR;

    try {
      // Oppretter mappen ("data/") dersom den ikke finnes fra før
      Path parent = this.path.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }

      // Skriving til fil
      try (BufferedWriter writer = Files.newBufferedWriter(
          this.path,
          StandardCharsets.UTF_8,
          StandardOpenOption.APPEND,
          StandardOpenOption.CREATE
      )) {
        writer.write(lineToWrite);
      }
    } catch (IOException e) {
      throw new RuntimeException("Feil med skriving til fil", e);
    }
  }
}