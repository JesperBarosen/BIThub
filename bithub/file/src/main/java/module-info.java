module bithub.file {
    requires transitive bithub.core;
    requires com.fasterxml.jackson.databind;
    requires static com.github.spotbugs.annotations;

    exports file;

    // Åpner file-pakken for Jackson, og for JUnit slik at testene får tilgang (som i core)
    opens file to com.fasterxml.jackson.databind, org.junit.platform.commons;
}
