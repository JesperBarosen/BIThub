package core.service;

import core.model.Course;
import java.util.List;

/**
 * Standar emner som alltid skal finnes i BIThub.
 */
public final class DefaultCourses {

    private DefaultCourses() {
    }

    /**
     * Lager standardemnene uten anmeldelser.
     *
     * @return en ny liste med standardemnene
     */
    public static List<Course> all() {
        return List.of(new Course("BI2081", "Natur, miljø og bærekraft"),
                new Course("EXPH0300", "Examen philosophicum"),
                new Course("HMS0002", "HMS-kurs for 1. årsstudenter"),
                new Course("IT1604", "Digitalt samfunn"),
                new Course("IT1901", "Informatikk prosjektarbeid I"));
    }
}