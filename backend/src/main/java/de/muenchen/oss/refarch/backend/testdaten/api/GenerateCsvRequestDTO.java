package de.muenchen.oss.refarch.backend.testdaten.api;

import java.util.List;

/**
 * Request to generate the CSV test data for the configured Zählung and selected relations.
 */
public record GenerateCsvRequestDTO(
        ZaehlungConfigDTO config,
        List<VerkehrsbeziehungOptionDTO> ausgewaehlteBeziehungen,
        DatengenerierungDTO datengenerierung) {
}
