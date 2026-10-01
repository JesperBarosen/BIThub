package ui;

import core.Course;
import java.util.List;

/** Emnene som kan velges i den første versjonen av appen. */
public final class CourseOptions {

  private CourseOptions() {
  }

  /**
   * Oppretter domenemodeller for fagene som vises i fagvelgeren.
   *
   * @return en liste over tilgjengelige emner
   */
  public static List<Course> availableCourses() {
    return List.of(
        new Course("BI2081", "Natur, miljø og bærekraft"),
        new Course("EXPH0300", "Examen philosophicum"),
        new Course("HMS0002", "HMS-kurs for 1. årsstudenter"),
        new Course("IT1604", "Digitalt samfunn"),
        new Course("IT1901", "Informatikk prosjektarbeid I")
    );
  }
}
