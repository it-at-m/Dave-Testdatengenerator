package de.muenchen.oss.refarch.backend.testdaten.api;

import java.util.List;

/**
 * Request to compute the possible movement relations for a given Zählart and node arm layout.
 */
public record VerkehrsbeziehungRequestDTO(
        String zaehlart,
        boolean kreisverkehr,
        List<KnotenarmDTO> knotenarme) {
}
