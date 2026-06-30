package de.muenchen.oss.refarch.backend.testdaten.api;

import java.util.List;

/**
 * Static selection options for the configuration UI (Zählarten, Zähldauern, Fahrzeugtypen,
 * Himmelsrichtungen).
 */
public record OptionsDTO(
        List<CodeLabel> zaehlarten,
        List<ZaehldauerInfo> zaehldauern,
        List<CodeLabel> fahrzeuge,
        List<String> himmelsrichtungen,
        List<String> statusWerte) {

    public record CodeLabel(String code, String label) {
    }

    public record ZaehldauerInfo(String code, String label, int anzahlZeitintervalle) {
    }
}
