package core.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.model.Course;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tester standardemnene som skal være tilgjengelige i appen.
 */
class DefaultCoursesTest {

    /**
     * Sjekker at emnelisten har innhold, og at ingen emnekode forekommer flere ganger.
     */
    @Test
    void defaultCoursesHaveUniqueCodes() {
        List<Course> courses = DefaultCourses.all();
        assertFalse(courses.isEmpty(), "Listen med standardemner skal ikke være tom.");

        Set<String> courseCodes = new HashSet<>();
        for (Course course : courses) {
            assertTrue(courseCodes.add(course.getCourseCode()),
                    () -> "Emnekoden " + course.getCourseCode() + " finnes flere ganger.");
        }
    }
}
