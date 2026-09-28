package file;

import java.util.List;

public interface DataHandler<T> {
    List<T> readFromFile();
    void writeToFile(List<T> data);
    boolean isEmpty();
}
