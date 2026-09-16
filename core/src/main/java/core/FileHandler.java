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

//Gjorde final for å ungå spotbugs feil
public final class FileHandler {
  private final Path path;
  private final String lineSeparator = "\r\n";

  public FileHandler(String path){
    if (path == null || path.isBlank()){
      throw new IllegalArgumentException("Path cannot be empty");
    }
    this.path = Path.of(path);
  }

  //Sjekker om filen finnes fra før
  public boolean isEmpty() {
    if (!Files.exists(this.path)) {
      return true;
    }
    try {
      return Files.size(this.path) == 0;
    } 
    // Hvis det oppstår en I/O-feil, behandler vi den som tom/manglende
    catch (IOException e) {
      return true; 
    }
  }


  public List<String[]> readFile(){
    List<String[]> lines = new ArrayList<>();

    /*Bruker bufferedReader for effektiv lesing fra fil*/    
    try (BufferedReader reader = Files.newBufferedReader(this.path)){
      String line;
      /*Legger til vær linje fra filen inn i lines listen*/
      while ((line=reader.readLine())!=null){
        /*Splitter med "," Ved utak av ,'er inne i "..." som kan skje i Comment
        Comment feltet burde derfor pakkes inn i ""*/
        lines.add(line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")); //Fikk regex fra Gemini
      }
    }

    /*Catcher feil ved lesing av fil */
    /*Bruker unchecked exception for å ungå at alle metoder som kaller denne metoden
    må ha en checked exception */
    catch (IOException e){
      throw new RuntimeException("Feil med lesing av fil",e);
    }

    return lines;
  }


  public void addToFile(String[] line) {
    String lineToWrite = String.join(",", line) + this.lineSeparator;

    try {
      // Oppretter mappen ("data/") dersom den ikke finnes fra før
      if (this.path.getParent() != null) {
        Files.createDirectories(this.path.getParent());
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
