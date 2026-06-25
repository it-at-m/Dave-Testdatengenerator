package de.muenchen.oss.refarch.backend.testdaten.api;

/**
 * Detail data of a selected Zählstelle, used to pre-fill the Zählung configuration.
 */
public record ZaehlstelleDetailDTO(
        String id,
        String nummer,
        String name,
        String stadtbezirk,
        Double lat,
        Double lng,
        String kommentar) {
}
