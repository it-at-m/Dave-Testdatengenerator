package de.muenchen.oss.refarch.backend.testdaten.csv;

import de.muenchen.oss.refarch.backend.dave.client.dto.ExternalKnotenarmDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.ExternalLaengsverkehrDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.ExternalQuerungsverkehrDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.ExternalVerkehrsbeziehungDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.ExternalZaehlungDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.ZeitintervallDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.CsvDateiDTO;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Parses the generated CSV files and attaches the resulting {@link ZeitintervallDTO}s to the
 * matching movement relations of a (re-read) {@link ExternalZaehlungDTO}, identified via
 * {@link RelationDiskriminator}. The relation ids of the re-read Zählung are the backend-assigned
 * UUIDs that {@code saveExternal} needs.
 */
@Service
public class CsvZeitintervallMapper {

    private static final int COLUMN_COUNT = 11;

    /**
     * Mutates {@code zaehlung} by setting the time intervals on its relations from the CSV files.
     *
     * @param zaehlung the re-read external Zählung (with persisted relation ids)
     * @param dateien the generated CSV files (one per Knotenarm)
     * @param zaehlartName the Zählart enum name
     * @param kreisverkehr whether the Zählung is a roundabout count
     */
    public void mappeZeitintervalle(final ExternalZaehlungDTO zaehlung, final List<CsvDateiDTO> dateien,
            final String zaehlartName, final boolean kreisverkehr) {
        final Map<String, List<CsvDataRow>> rowsByKey = parseAlleDateien(dateien, zaehlartName, kreisverkehr);

        if (zaehlung.getVerkehrsbeziehungen() != null) {
            for (final ExternalVerkehrsbeziehungDTO vb : zaehlung.getVerkehrsbeziehungen()) {
                vb.setZeitintervalle(toZeitintervalle(rowsByKey.get(RelationDiskriminator.keyForExternal(vb))));
            }
        }
        if (zaehlung.getLaengsverkehr() != null) {
            for (final ExternalLaengsverkehrDTO lv : zaehlung.getLaengsverkehr()) {
                lv.setZeitintervalle(toZeitintervalle(rowsByKey.get(RelationDiskriminator.keyForExternal(lv))));
            }
        }
        if (zaehlung.getQuerungsverkehr() != null) {
            for (final ExternalQuerungsverkehrDTO qv : zaehlung.getQuerungsverkehr()) {
                qv.setZeitintervalle(toZeitintervalle(rowsByKey.get(RelationDiskriminator.keyForExternal(qv))));
            }
        }
        setKnotenarmFilenames(zaehlung, dateien);
    }

    private Map<String, List<CsvDataRow>> parseAlleDateien(final List<CsvDateiDTO> dateien, final String zaehlartName,
            final boolean kreisverkehr) {
        final Map<String, List<CsvDataRow>> rowsByKey = new HashMap<>();
        for (final CsvDateiDTO datei : dateien) {
            for (final CsvDataRow row : parseDatei(datei.content())) {
                final String key = RelationDiskriminator.keyForRow(zaehlartName, kreisverkehr, datei.knotenarmNummer(), row);
                rowsByKey.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
            }
        }
        return rowsByKey;
    }

    private List<CsvDataRow> parseDatei(final String content) {
        final List<CsvDataRow> rows = new ArrayList<>();
        if (content == null) {
            return rows;
        }
        final String[] lines = content.split("\\r?\\n");
        // first three lines are header lines (meta header, meta data, data header)
        for (int i = 3; i < lines.length; i++) {
            final String line = lines[i];
            if (line == null || line.isBlank()) {
                continue;
            }
            final String[] cols = line.split(";", -1);
            if (cols.length < COLUMN_COUNT) {
                continue;
            }
            rows.add(new CsvDataRow(
                    parseInt(cols[0], 0),
                    cols[1].trim(),
                    cols[2].trim(),
                    cols[3].trim(),
                    parseIntOrNull(cols[4]),
                    parseIntOrNull(cols[5]),
                    parseIntOrNull(cols[6]),
                    parseIntOrNull(cols[7]),
                    parseIntOrNull(cols[8]),
                    parseIntOrNull(cols[9]),
                    parseIntOrNull(cols[10])));
        }
        return rows;
    }

    private List<ZeitintervallDTO> toZeitintervalle(final List<CsvDataRow> rows) {
        final List<ZeitintervallDTO> intervalle = new ArrayList<>();
        if (rows == null) {
            return intervalle;
        }
        rows.stream().sorted(Comparator.comparingInt(CsvDataRow::intervallNummer)).forEach(row -> {
            final ZeitintervallDTO zi = new ZeitintervallDTO();
            zi.setStartUhrzeit(time((row.intervallNummer() - 1) * 15));
            zi.setEndeUhrzeit(time(row.intervallNummer() * 15));
            zi.setPkw(row.pkw());
            zi.setLkw(row.lkw());
            zi.setLastzuege(row.lastzuege());
            zi.setBusse(row.busse());
            zi.setKraftraeder(row.kraftraeder());
            zi.setFahrradfahrer(row.fahrradfahrer());
            zi.setFussgaenger(row.fussgaenger());
            intervalle.add(zi);
        });
        return intervalle;
    }

    private void setKnotenarmFilenames(final ExternalZaehlungDTO zaehlung, final List<CsvDateiDTO> dateien) {
        if (zaehlung.getKnotenarme() == null) {
            return;
        }
        final Map<Integer, String> filenameByArm = new HashMap<>();
        dateien.forEach(d -> filenameByArm.put(d.knotenarmNummer(), d.filename()));
        for (final ExternalKnotenarmDTO arm : zaehlung.getKnotenarme()) {
            final String filename = filenameByArm.get(arm.getNummer());
            if (filename != null) {
                arm.setFilename(filename);
            }
        }
    }

    private static String time(final int minutesFromMidnight) {
        final int hours = minutesFromMidnight / 60;
        final int minutes = minutesFromMidnight % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    private static int parseInt(final String value, final int fallback) {
        final Integer parsed = parseIntOrNull(value);
        return parsed == null ? fallback : parsed;
    }

    private static Integer parseIntOrNull(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (final NumberFormatException ex) {
            return null;
        }
    }
}
