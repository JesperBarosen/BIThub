module bithub.file {
    requires transitive bithub.core;
    requires com.fasterxml.jackson.databind;
    requires static com.github.spotbugs.annotations;

    exports file;
    exports file.dto;
    exports file.storage;

    opens file.dto to com.fasterxml.jackson.databind;
}
