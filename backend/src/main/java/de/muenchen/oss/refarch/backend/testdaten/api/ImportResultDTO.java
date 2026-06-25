package de.muenchen.oss.refarch.backend.testdaten.api;

import java.util.List;

/**
 * Outcome of the import workflow, including a step-by-step log for the UI.
 */
public record ImportResultDTO(
        boolean success,
        String zaehlungId,
        String finalStatus,
        String message,
        List<String> steps) {
}
