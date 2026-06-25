package de.muenchen.oss.refarch.backend.testdaten.csv;

import de.muenchen.oss.refarch.backend.dave.client.enums.Zaehlart;
import de.muenchen.oss.refarch.backend.dave.client.enums.Zaehldauer;
import de.muenchen.oss.refarch.backend.testdaten.api.CsvDateiDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.VerkehrsbeziehungOptionDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.WertebereicheDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.ZaehlungConfigDTO;
import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import org.springframework.stereotype.Service;

/**
 * Generates CSV test data in the format used by the selfservice portal upload (3 header lines and
 * 11 columns). One file is produced per Knotenarm. Each selected relation receives exactly
 * {@link Zaehldauer#getAnzahlZeitintervalle()} data rows so the count passes the backend's
 * plausibility check when the Zählung is later set to ACCOMPLISHED.
 */
@Service
public class CsvGeneratorService {

    public static final String META_HEADER = "Zählstellennummer;Zählart;Datum;Knotenarmnummer;;;;;;;";
    public static final String DATA_HEADER = "Intervallnummer;nach;Strassenseite;Richtung;Pkw;Lkw;Lz;Bus;Krad;Rad;Fuss";
    private static final String SEP = ";";
    // Datum im ISO-Format (YYYY-MM-DD), wie in der CSV-Upload-Dokumentation und den Beispiel-Testdaten gefordert.
    private static final DateTimeFormatter DATUM_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final Random random = new SecureRandom();

    public List<CsvDateiDTO> generiere(final ZaehlungConfigDTO config, final List<VerkehrsbeziehungOptionDTO> ausgewaehlteBeziehungen,
            final WertebereicheDTO wertebereiche) {
        final Zaehldauer zaehldauer = Zaehldauer.valueOf(config.zaehldauer());
        final Zaehlart zaehlart = Zaehlart.valueOf(config.zaehlart());
        final List<ZaehldauerIntervalle.Slot> slots = ZaehldauerIntervalle.slotsFor(zaehldauer);
        final Spalten spalten = spaltenFuer(zaehlart, config.kategorien());
        final Wertebereiche ranges = Wertebereiche.aus(wertebereiche);

        // group selected relations by their owning Knotenarm
        final Map<Integer, List<VerkehrsbeziehungOptionDTO>> proArm = new TreeMap<>();
        for (final VerkehrsbeziehungOptionDTO option : ausgewaehlteBeziehungen) {
            proArm.computeIfAbsent(RelationDiskriminator.ownerArm(option), k -> new ArrayList<>()).add(option);
        }

        // Dateinamenskonvention laut Dokumentation: <ZAEHLSTELLENNUMMER>_<DATUM>_Knotenarm_<KNOTENARMNUMMER>.csv
        final String datumDatei = config.datum() == null ? "" : config.datum().format(DATUM_FORMAT);

        final List<CsvDateiDTO> dateien = new ArrayList<>();
        for (final Map.Entry<Integer, List<VerkehrsbeziehungOptionDTO>> entry : proArm.entrySet()) {
            final int arm = entry.getKey();
            final String content = baueDatei(config, zaehlart, arm, entry.getValue(), slots, spalten, ranges);
            final String filename = safe(config.zaehlstelleNummer()) + "_" + datumDatei + "_Knotenarm_" + arm + ".csv";
            dateien.add(new CsvDateiDTO(arm, filename, content));
        }
        return dateien;
    }

    private String baueDatei(final ZaehlungConfigDTO config, final Zaehlart zaehlart, final int arm,
            final List<VerkehrsbeziehungOptionDTO> beziehungen, final List<ZaehldauerIntervalle.Slot> slots,
            final Spalten spalten, final Wertebereiche ranges) {
        final StringBuilder sb = new StringBuilder();
        final String zaehlartMeta = zaehlart == Zaehlart.N ? "" : zaehlart.name();
        final String datum = config.datum() == null ? "" : config.datum().format(DATUM_FORMAT);
        sb.append(META_HEADER).append('\n');
        sb.append(safe(config.zaehlstelleNummer())).append(SEP).append(zaehlartMeta).append(SEP)
                .append(datum).append(SEP).append(arm).append(";;;;;").append('\n');
        sb.append(DATA_HEADER).append('\n');

        for (final VerkehrsbeziehungOptionDTO beziehung : beziehungen) {
            final RelationDiskriminator.CsvColumns cols = RelationDiskriminator.csvColumns(beziehung);
            for (final ZaehldauerIntervalle.Slot slot : slots) {
                sb.append(slot.nummer()).append(SEP)
                        .append(cols.nach()).append(SEP)
                        .append(cols.strassenseite()).append(SEP)
                        .append(cols.richtung()).append(SEP)
                        .append(wert(spalten.pkw(), ranges.pkw())).append(SEP)
                        .append(wert(spalten.lkw(), ranges.lkw())).append(SEP)
                        .append(wert(spalten.lastzuege(), ranges.lastzuege())).append(SEP)
                        .append(wert(spalten.busse(), ranges.busse())).append(SEP)
                        .append(wert(spalten.kraftraeder(), ranges.kraftraeder())).append(SEP)
                        .append(wert(spalten.fahrradfahrer(), ranges.fahrradfahrer())).append(SEP)
                        .append(wert(spalten.fussgaenger(), ranges.fussgaenger()))
                        .append('\n');
            }
        }
        return sb.toString();
    }

    private String wert(final boolean fill, final int[] range) {
        if (!fill) {
            return "";
        }
        final int min = range[0];
        final int max = Math.max(range[0], range[1]);
        return String.valueOf(min + random.nextInt((max - min) + 1));
    }

    private static String safe(final String value) {
        return value == null ? "" : value;
    }

    private static Spalten spaltenFuer(final Zaehlart zaehlart, final List<String> kategorien) {
        final List<String> k = kategorien == null ? List.of() : kategorien;
        final boolean fussRadOnly = zaehlart == Zaehlart.FJS || zaehlart == Zaehlart.QU || zaehlart == Zaehlart.QJS;
        if (fussRadOnly) {
            boolean rad = k.contains("RAD");
            boolean fuss = k.contains("FUSS");
            if (!rad && !fuss) {
                rad = true;
                fuss = true;
            }
            return new Spalten(false, false, false, false, false, rad, fuss);
        }
        boolean pkw = k.contains("PKW");
        final boolean lkw = k.contains("LKW");
        final boolean lz = k.contains("LZ");
        final boolean bus = k.contains("BUS");
        final boolean krad = k.contains("KRAD");
        final boolean rad = k.contains("RAD");
        final boolean fuss = k.contains("FUSS");
        if (!pkw && !lkw && !lz && !bus && !krad && !rad && !fuss) {
            pkw = true;
        }
        return new Spalten(pkw, lkw, lz, bus, krad, rad, fuss);
    }

    private record Spalten(boolean pkw, boolean lkw, boolean lastzuege, boolean busse, boolean kraftraeder,
            boolean fahrradfahrer, boolean fussgaenger) {
    }

    private record Wertebereiche(int[] pkw, int[] lkw, int[] lastzuege, int[] busse, int[] kraftraeder,
            int[] fahrradfahrer, int[] fussgaenger) {

        static Wertebereiche aus(final WertebereicheDTO dto) {
            return new Wertebereiche(
                    range(dto == null ? null : dto.pkw(), 50, 300),
                    range(dto == null ? null : dto.lkw(), 5, 40),
                    range(dto == null ? null : dto.lastzuege(), 0, 10),
                    range(dto == null ? null : dto.busse(), 0, 8),
                    range(dto == null ? null : dto.kraftraeder(), 0, 15),
                    range(dto == null ? null : dto.fahrradfahrer(), 0, 50),
                    range(dto == null ? null : dto.fussgaenger(), 0, 60));
        }

        private static int[] range(final WertebereicheDTO.Range r, final int defMin, final int defMax) {
            final int min = r == null || r.min() == null ? defMin : r.min();
            final int max = r == null || r.max() == null ? defMax : r.max();
            return new int[] { Math.min(min, max), Math.max(min, max) };
        }
    }
}
