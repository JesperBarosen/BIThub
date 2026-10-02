module bithub.core {
    exports core;

    // Åpner core-pakken slik at testrammeverket (JUnit) får tilgang til testene
    opens core to org.junit.platform.commons;
}
