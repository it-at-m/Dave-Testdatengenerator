package de.muenchen.oss.refarch.backend.testdaten.csv;

import de.muenchen.oss.refarch.backend.dave.client.enums.Zaehlart;
import de.muenchen.oss.refarch.backend.dave.client.enums.Zaehldauer;
import de.muenchen.oss.refarch.backend.testdaten.api.CsvDateiDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.DatengenerierungDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.DatengenerierungDTO.Modus;
import de.muenchen.oss.refarch.backend.testdaten.api.DatengenerierungDTO.Spezifikation;
import de.muenchen.oss.refarch.backend.testdaten.api.VerkehrsbeziehungOptionDTO;
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
            final DatengenerierungDTO datengenerierung) {
        final Zaehldauer zaehldauer = Zaehldauer.valueOf(config.zaehldauer());
        final Zaehlart zaehlart = Zaehlart.valueOf(config.zaehlart());
        final List<ZaehldauerIntervalle.Slot> slots = ZaehldauerIntervalle.slotsFor(zaehldauer);
        final Spalten spalten = spaltenFuer(zaehlart, config.kategorien());
        final Generierung gen = new Generierung(datengenerierung, random);

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
            final String content = baueDatei(config, zaehlart, arm, entry.getValue(), slots, spalten, gen);
            final String filename = safe(config.zaehlstelleNummer()) + "_" + datumDatei + "_Knotenarm_" + arm + ".csv";
            dateien.add(new CsvDateiDTO(arm, filename, content));
        }
        return dateien;
    }

    private String baueDatei(final ZaehlungConfigDTO config, final Zaehlart zaehlart, final int arm,
            final List<VerkehrsbeziehungOptionDTO> beziehungen, final List<ZaehldauerIntervalle.Slot> slots,
            final Spalten spalten, final Generierung gen) {
        final StringBuilder sb = new StringBuilder();
        final String zaehlartMeta = zaehlart == Zaehlart.N ? "" : zaehlart.name();
        final String datum = config.datum() == null ? "" : config.datum().format(DATUM_FORMAT);
        sb.append(META_HEADER).append('\n');
        sb.append(safe(config.zaehlstelleNummer())).append(SEP).append(zaehlartMeta).append(SEP)
                .append(datum).append(SEP).append(arm).append(";;;;;").append('\n');
        sb.append(DATA_HEADER).append('\n');

        for (final VerkehrsbeziehungOptionDTO beziehung : beziehungen) {
            final RelationDiskriminator.CsvColumns cols = RelationDiskriminator.csvColumns(beziehung);
            // Der Tagesgang (aufsteigend / realistisch) wird je Verkehrsbeziehung neu durchlaufen.
            for (int i = 0; i < slots.size(); i++) {
                final ZaehldauerIntervalle.Slot slot = slots.get(i);
                final int stunde = stunde(slot.startUhrzeit());
                sb.append(slot.nummer()).append(SEP)
                        .append(cols.nach()).append(SEP)
                        .append(cols.strassenseite()).append(SEP)
                        .append(cols.richtung()).append(SEP)
                        .append(zelle(spalten.pkw(), "PKW", i, stunde, gen)).append(SEP)
                        .append(zelle(spalten.lkw(), "LKW", i, stunde, gen)).append(SEP)
                        .append(zelle(spalten.lastzuege(), "LZ", i, stunde, gen)).append(SEP)
                        .append(zelle(spalten.busse(), "BUS", i, stunde, gen)).append(SEP)
                        .append(zelle(spalten.kraftraeder(), "KRAD", i, stunde, gen)).append(SEP)
                        .append(zelle(spalten.fahrradfahrer(), "RAD", i, stunde, gen)).append(SEP)
                        .append(zelle(spalten.fussgaenger(), "FUSS", i, stunde, gen))
                        .append('\n');
            }
        }
        return sb.toString();
    }

    private String zelle(final boolean fill, final String fahrzeug, final int intervallIndex, final int stunde, final Generierung gen) {
        return fill ? String.valueOf(gen.wert(fahrzeug, intervallIndex, stunde)) : "";
    }

    private static int stunde(final String uhrzeit) {
        return Integer.parseInt(uhrzeit.substring(0, 2));
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

    /**
     * Computes the counting value for a vehicle class in a given interval, depending on the chosen
     * {@link Modus}. Falls back to {@link Modus#ZUFALL} with a per-class default magnitude when no
     * specification is provided.
     */
    private static final class Generierung {

        private static final Map<String, Integer> DEFAULT_WERT = Map.of(
                "PKW", 300, "LKW", 40, "LZ", 10, "BUS", 8, "KRAD", 15, "RAD", 50, "FUSS", 60);

        private final Map<String, Spezifikation> proFahrzeug;
        private final Random random;

        Generierung(final DatengenerierungDTO dto, final Random random) {
            this.proFahrzeug = dto == null || dto.proFahrzeug() == null ? Map.of() : dto.proFahrzeug();
            this.random = random;
        }

        int wert(final String fahrzeug, final int intervallIndex, final int stunde) {
            final Spezifikation spec = proFahrzeug.get(fahrzeug);
            final Modus modus = spec == null || spec.modus() == null ? Modus.ZUFALL : spec.modus();
            final int basis = spec == null || spec.wert() == null
                    ? DEFAULT_WERT.getOrDefault(fahrzeug, 50)
                    : Math.max(0, spec.wert());
            return switch (modus) {
            case KONSTANT -> basis;
            case AUFSTEIGEND -> basis + intervallIndex;
            case REALISTISCH -> (int) Math.round(basis * tagesgangFaktor(stunde));
            case ZUFALL -> random.nextInt(basis + 1);
            };
        }

        /**
         * Daily traffic curve in [0,1] with a morning (~08:00) and an evening (~17:00) peak and a
         * midday dip; nights are low. So Stoßzeiten erhalten mehr Verkehr als die Zeit dazwischen.
         */
        private static double tagesgangFaktor(final int stunde) {
            return switch (stunde) {
            case 6 -> 0.5;
            case 7 -> 0.85;
            case 8 -> 1.0;
            case 9 -> 0.7;
            case 10, 11 -> 0.5;
            case 12 -> 0.55;
            case 13, 14 -> 0.5;
            case 15 -> 0.65;
            case 16 -> 0.85;
            case 17 -> 1.0;
            case 18 -> 0.8;
            case 19 -> 0.6;
            case 20 -> 0.4;
            case 21 -> 0.3;
            case 22 -> 0.2;
            case 5 -> 0.2;
            default -> 0.05; // 23:00 - 04:00 (Nacht)
            };
        }
    }
}
