package de.muenchen.oss.refarch.backend.testdaten.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.muenchen.oss.refarch.backend.testdaten.VerkehrsbeziehungFactory;
import de.muenchen.oss.refarch.backend.testdaten.api.CsvDateiDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.DatengenerierungDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.DatengenerierungDTO.Modus;
import de.muenchen.oss.refarch.backend.testdaten.api.DatengenerierungDTO.Spezifikation;
import de.muenchen.oss.refarch.backend.testdaten.api.KnotenarmDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.VerkehrsbeziehungOptionDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.ZaehlungConfigDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CsvGeneratorServiceTest {

    private static final String NUMMER = "111310";
    private static final LocalDate DATUM = LocalDate.of(2026, 3, 16);

    private final VerkehrsbeziehungFactory factory = new VerkehrsbeziehungFactory();
    private final CsvGeneratorService generator = new CsvGeneratorService();

    @Test
    void givenZaehlung_thenMetaHeaderUndDateinameImIsoFormat() {
        final List<CsvDateiDTO> dateien = generiere("N", List.of("PKW"), List.of(new KnotenarmDTO(1, "Arm 1")));

        final CsvDateiDTO datei = dateien.get(0);
        // Dateiname nach Konvention <Nummer>_<ISO-Datum>_Knotenarm_<Arm>.csv
        assertEquals("111310_2026-03-16_Knotenarm_1.csv", datei.filename());
        // Meta-Datenzeile mit ISO-Datum; Zählart N bleibt leer
        assertTrue(zeile(datei, 1).startsWith("111310;;2026-03-16;1"), zeile(datei, 1));
    }

    @Test
    void givenFjsZaehlung_thenStrassenseiteUndRichtungGesetztNachLeer() {
        final List<CsvDateiDTO> dateien = generiere("FJS", List.of("RAD", "FUSS"), List.of(new KnotenarmDTO(1, "Arm 1")));

        final String[] cols = ersteDatenzeile(dateien.get(0));
        assertEquals("", cols[1], "nach muss bei FjS leer sein");
        assertFalse(cols[2].isBlank(), "Strassenseite muss bei FjS gesetzt sein");
        assertTrue(List.of("EIN", "AUS").contains(cols[3]), "Richtung muss EIN/AUS sein");
        nurRadUndFussGefuellt(cols);
    }

    @Test
    void givenQuZaehlung_thenNurRichtungGesetzt() {
        final List<CsvDateiDTO> dateien = generiere("QU", List.of("RAD", "FUSS"), List.of(new KnotenarmDTO(2, "Arm 2")));

        final String[] cols = ersteDatenzeile(dateien.get(0));
        assertEquals("", cols[1], "nach muss bei Qu leer sein");
        assertEquals("", cols[2], "Strassenseite muss bei Qu leer sein");
        assertFalse(cols[3].isBlank(), "Richtung (Himmelsrichtung) muss bei Qu gesetzt sein");
        nurRadUndFussGefuellt(cols);
    }

    @Test
    void givenQjsZaehlung_thenNachUndStrassenseiteGesetztRichtungLeer() {
        final List<CsvDateiDTO> dateien = generiere("QJS", List.of("RAD", "FUSS"), List.of(new KnotenarmDTO(2, "Arm 2")));

        final String[] cols = ersteDatenzeile(dateien.get(0));
        assertFalse(cols[1].isBlank(), "nach muss bei QjS gesetzt sein");
        assertFalse(cols[2].isBlank(), "Strassenseite muss bei QjS gesetzt sein");
        assertEquals("", cols[3], "Richtung muss bei QjS leer sein");
        nurRadUndFussGefuellt(cols);
    }

    @Test
    void givenNormaleZaehlung_thenNachGesetztUndFahrzeugklassenGefuellt() {
        final List<CsvDateiDTO> dateien = generiere("N", List.of("PKW", "LKW", "LZ", "BUS", "KRAD", "RAD"),
                List.of(new KnotenarmDTO(1, "Arm 1"), new KnotenarmDTO(2, "Arm 2")));

        final String[] cols = ersteDatenzeile(dateien.get(0));
        assertFalse(cols[1].isBlank(), "nach muss bei Kreuzung gesetzt sein");
        assertEquals("", cols[2], "Strassenseite muss bei Kreuzung leer sein");
        assertEquals("", cols[3], "Richtung muss bei Kreuzung leer sein");
        assertFalse(cols[4].isBlank(), "Pkw muss gefüllt sein");
        assertEquals("", cols[10], "Fuss darf bei der Normalzählung nicht gefüllt sein");
    }

    @Test
    void givenKonstanteGenerierung_thenAlleWerteGleich() {
        final DatengenerierungDTO gen = new DatengenerierungDTO(Map.of("PKW", new Spezifikation(Modus.KONSTANT, 42)));
        final CsvDateiDTO datei = generiereMit("N", List.of("PKW"), List.of(new KnotenarmDTO(1, "Arm 1")),
                "DAUER_2_X_4_STUNDEN", gen).get(0);

        final int[] pkw = spaltenwerte(datei, 4);
        assertTrue(pkw.length > 1);
        for (final int v : pkw) {
            assertEquals(42, v);
        }
    }

    @Test
    void givenAufsteigendeGenerierung_thenJeIntervallPlusEins() {
        final DatengenerierungDTO gen = new DatengenerierungDTO(Map.of("PKW", new Spezifikation(Modus.AUFSTEIGEND, 10)));
        // Nur eine Verkehrsbeziehung (Arm 1 -> Arm 1), damit die Reihe nicht zurückgesetzt wird.
        final CsvDateiDTO datei = generiereMit("N", List.of("PKW"), List.of(new KnotenarmDTO(1, "Arm 1")),
                "DAUER_2_X_4_STUNDEN", gen).get(0);

        final int[] pkw = spaltenwerte(datei, 4);
        assertEquals(10, pkw[0], "Startwert");
        for (int i = 1; i < pkw.length; i++) {
            assertEquals(pkw[i - 1] + 1, pkw[i], "jedes Intervall + 1");
        }
    }

    @Test
    void givenZufallsGenerierung_thenWerteImBereichNullBisWert() {
        final DatengenerierungDTO gen = new DatengenerierungDTO(Map.of("PKW", new Spezifikation(Modus.ZUFALL, 5)));
        final CsvDateiDTO datei = generiereMit("N", List.of("PKW"), List.of(new KnotenarmDTO(1, "Arm 1")),
                "DAUER_2_X_4_STUNDEN", gen).get(0);

        for (final int v : spaltenwerte(datei, 4)) {
            assertTrue(v >= 0 && v <= 5, "Wert außerhalb [0,5]: " + v);
        }
    }

    @Test
    void givenRealistischeGenerierung_thenStosszeitGroesserAlsNacht() {
        final DatengenerierungDTO gen = new DatengenerierungDTO(Map.of("PKW", new Spezifikation(Modus.REALISTISCH, 100)));
        final CsvDateiDTO datei = generiereMit("N", List.of("PKW"), List.of(new KnotenarmDTO(1, "Arm 1")),
                "DAUER_24_STUNDEN", gen).get(0);

        // Intervall 33 startet 08:00 (Stoßzeit), Intervall 13 startet 03:00 (Nacht).
        assertTrue(pkwBeiIntervall(datei, 33) > pkwBeiIntervall(datei, 13),
                "Stoßzeit (08:00) muss mehr Verkehr haben als die Nacht (03:00)");
    }

    private void nurRadUndFussGefuellt(final String[] cols) {
        // Spalten 4..8 = Pkw,Lkw,Lz,Bus,Krad leer; 9..10 = Rad,Fuss gefüllt
        for (int i = 4; i <= 8; i++) {
            assertEquals("", cols[i], "Spalte " + i + " muss leer sein");
        }
        assertFalse(cols[9].isBlank(), "Rad muss gefüllt sein");
        assertFalse(cols[10].isBlank(), "Fuss muss gefüllt sein");
    }

    private List<CsvDateiDTO> generiere(final String zaehlart, final List<String> kategorien, final List<KnotenarmDTO> arme) {
        final List<VerkehrsbeziehungOptionDTO> beziehungen = factory.moeglicheBeziehungen(zaehlart, false, arme);
        return generator.generiere(config(zaehlart, kategorien, arme, "DAUER_2_X_4_STUNDEN"), beziehungen, null);
    }

    private List<CsvDateiDTO> generiereMit(final String zaehlart, final List<String> kategorien, final List<KnotenarmDTO> arme,
            final String zaehldauer, final DatengenerierungDTO gen) {
        final List<VerkehrsbeziehungOptionDTO> beziehungen = factory.moeglicheBeziehungen(zaehlart, false, arme);
        return generator.generiere(config(zaehlart, kategorien, arme, zaehldauer), beziehungen, gen);
    }

    private ZaehlungConfigDTO config(final String zaehlart, final List<String> kategorien, final List<KnotenarmDTO> arme,
            final String zaehldauer) {
        return new ZaehlungConfigDTO(
                "id", NUMMER, null, null, DATUM, zaehlart, zaehldauer, 15,
                false, false, null, null, null, "Testdaten", null, null, "testdaten", kategorien, arme);
    }

    /** Liefert die Werte einer Datenspalte (z.B. 4 = Pkw) über alle Datenzeilen. */
    private int[] spaltenwerte(final CsvDateiDTO datei, final int spalte) {
        final String[] zeilen = datei.content().split("\\r?\\n");
        return java.util.Arrays.stream(zeilen, 3, zeilen.length)
                .map(z -> z.split(";", -1)[spalte])
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    /** Liefert den Pkw-Wert der Datenzeile mit der gegebenen Intervallnummer. */
    private int pkwBeiIntervall(final CsvDateiDTO datei, final int intervallnummer) {
        final String[] zeilen = datei.content().split("\\r?\\n");
        return java.util.Arrays.stream(zeilen, 3, zeilen.length)
                .map(z -> z.split(";", -1))
                .filter(c -> Integer.parseInt(c[0]) == intervallnummer)
                .mapToInt(c -> Integer.parseInt(c[4]))
                .findFirst().orElseThrow();
    }

    private String zeile(final CsvDateiDTO datei, final int index) {
        return datei.content().split("\\r?\\n")[index];
    }

    private String[] ersteDatenzeile(final CsvDateiDTO datei) {
        // Zeilen 0..2 sind Header; ab Zeile 3 beginnen die Datenzeilen.
        return zeile(datei, 3).split(";", -1);
    }
}
