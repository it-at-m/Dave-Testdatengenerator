package de.muenchen.oss.refarch.backend.testdaten.api;

import java.util.List;

/**
 * Request to import the previously generated CSV data into the DAVe backend.
 * {@code zielStatus} is the final status to set (e.g. ACCOMPLISHED, which triggers the backend's
 * value computation); defaults to ACCOMPLISHED when null.
 */
public record ImportRequestDTO(
        ZaehlungConfigDTO config,
        List<VerkehrsbeziehungOptionDTO> ausgewaehlteBeziehungen,
        List<CsvDateiDTO> dateien,
        String zielStatus) {
}
