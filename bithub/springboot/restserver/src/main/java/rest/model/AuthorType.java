package rest.model;

/**
 * Hvem som skrev en anmeldelse, slik det vises i svarene fra REST-API-et.
 */
public enum AuthorType {

    /** Innlogget bruker som viser navnet sitt. */
    USER,

    /** Innlogget bruker som har valgt å være anonym. */
    ANONYMOUS,

    /** Noen som ikke var logget inn.*/
    GUEST
}