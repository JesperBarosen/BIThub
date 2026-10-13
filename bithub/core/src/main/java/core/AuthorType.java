package core;

/**
 * Hvilken type forfatter en anmeldelse har.
 */
public enum AuthorType {

    /** Innlogget bruker som vises med navn. */
    USER,

    /** Innlogget bruker som har valgt å være anonym. */
    ANONYMOUS,

    /** Gjest som ikke er logget inn, og som ikke har en eier. */
    GUEST
}
