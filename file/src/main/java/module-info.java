module bithub.file {
    requires transitive bithub.core;
    requires com.fasterxml.jackson.databind;
    requires static com.github.spotbugs.annotations;

    exports file;

    opens file to com.fasterxml.jackson.databind;
}