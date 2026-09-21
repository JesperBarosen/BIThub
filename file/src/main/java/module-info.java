module bithub.file {
    requires transitive bithub.core;
    requires com.fasterxml.jackson.databind;

    exports file;

    opens file to com.fasterxml.jackson.databind;
}