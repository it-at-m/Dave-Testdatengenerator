package de.muenchen.oss.refarch.backend.testdaten.api;

import java.time.LocalDate;
import java.util.List;

/**
 * Full configuration of the Zählung to create (everything except the time intervals, which are
 * generated as CSV). {@code kategorien} are Fahrzeug codes, {@code zaehlart}/{@code zaehldauer} are
 * the respective enum names of the DAVe backend.
 */
public record ZaehlungConfigDTO(
        String zaehlstelleId,
        String zaehlstelleNummer,
        Double lat,
        Double lng,
        LocalDate datum,
        String zaehlart,
        String zaehldauer,
        Integer zaehlIntervall,
        boolean kreisverkehr,
        boolean sonderzaehlung,
        String projektNummer,
        String projektName,
        String kreuzungsname,
        String kommentar,
        String wetter,
        String tagesTyp,
        String dienstleisterkennung,
        List<String> kategorien,
        List<KnotenarmDTO> knotenarme) {
}
