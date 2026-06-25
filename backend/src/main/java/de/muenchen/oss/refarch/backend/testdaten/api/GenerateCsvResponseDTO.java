package de.muenchen.oss.refarch.backend.testdaten.api;

import java.util.List;

/**
 * Result of CSV generation: one file per Knotenarm for preview and download.
 */
public record GenerateCsvResponseDTO(List<CsvDateiDTO> dateien) {
}
