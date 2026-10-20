package rest.model;

/**
 * Felles format for feilsvar fra REST-API-et.
 *
 * @param status HTTP-statuskoden
 * @param error kort beskrivelse av statusen, for eksempel Not Found
 * @param message forklaring på hva som gikk galt
 */
public record ErrorResponse(int status, String error, String message) {
}
