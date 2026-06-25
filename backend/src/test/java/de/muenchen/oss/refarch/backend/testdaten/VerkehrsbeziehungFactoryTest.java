package de.muenchen.oss.refarch.backend.testdaten;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.muenchen.oss.refarch.backend.testdaten.api.KnotenarmDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.VerkehrsbeziehungOptionDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.VerkehrsbeziehungOptionDTO.RelationTyp;
import java.util.List;
import org.junit.jupiter.api.Test;

class VerkehrsbeziehungFactoryTest {

    private final VerkehrsbeziehungFactory factory = new VerkehrsbeziehungFactory();

    @Test
    void givenFjsMitEinemArm_thenBeideStrassenseitenMitEinUndAus() {
        final List<VerkehrsbeziehungOptionDTO> options = factory.moeglicheBeziehungen(
                "FJS", false, List.of(new KnotenarmDTO(1, "Arm 1")));

        // Arm 1 (ungerade) -> Achse O/W, je Seite EIN und AUS = 4 Beziehungen
        assertEquals(4, options.size());
        assertTrue(options.stream().allMatch(o -> o.typ() == RelationTyp.LAENGSVERKEHR));
        assertTrue(options.stream().allMatch(o -> o.knotenarm() == 1));
        assertTrue(enthaelt(options, "O", "EIN"));
        assertTrue(enthaelt(options, "O", "AUS"));
        assertTrue(enthaelt(options, "W", "EIN"));
        assertTrue(enthaelt(options, "W", "AUS"));
    }

    @Test
    void givenQuMitGerademArm_thenBeideGegenueberliegendenHimmelsrichtungen() {
        final List<VerkehrsbeziehungOptionDTO> options = factory.moeglicheBeziehungen(
                "QU", false, List.of(new KnotenarmDTO(2, "Arm 2")));

        // Arm 2 (gerade) -> Achse N/S
        assertEquals(2, options.size());
        assertTrue(options.stream().allMatch(o -> o.typ() == RelationTyp.QUERUNGSVERKEHR));
        assertTrue(options.stream().allMatch(o -> o.nach() == null && o.strassenseite() == null));
        assertTrue(options.stream().anyMatch(o -> "N".equals(o.richtung())));
        assertTrue(options.stream().anyMatch(o -> "S".equals(o.richtung())));
    }

    @Test
    void givenQjsMitEinemArm_thenBeideStrassenseitenUndSelbstbeziehungErlaubt() {
        final List<VerkehrsbeziehungOptionDTO> options = factory.moeglicheBeziehungen(
                "QJS", false, List.of(new KnotenarmDTO(1, "Arm 1")));

        // Arm 1 -> Arm 1 (Selbstbeziehung) mit beiden Straßenseiten O/W
        assertEquals(2, options.size());
        assertTrue(options.stream().allMatch(o -> o.typ() == RelationTyp.VERKEHRSBEZIEHUNG));
        assertTrue(options.stream().allMatch(o -> o.von() == 1 && o.nach() == 1));
        assertTrue(options.stream().anyMatch(o -> "O".equals(o.strassenseite())));
        assertTrue(options.stream().anyMatch(o -> "W".equals(o.strassenseite())));
    }

    @Test
    void givenFjsMitDiagonalemArmFuenf_thenStrassenseitenSoUndNw() {
        final List<VerkehrsbeziehungOptionDTO> options = factory.moeglicheBeziehungen(
                "FJS", false, List.of(new KnotenarmDTO(5, "Arm 5")));

        // Arm 5 zeigt diagonal -> Achse SO/NW (vgl. KnotenLageForm.vue: Arm 5/7 -> NW/SO)
        assertEquals(4, options.size());
        assertTrue(enthaelt(options, "SO", "EIN"));
        assertTrue(enthaelt(options, "SO", "AUS"));
        assertTrue(enthaelt(options, "NW", "EIN"));
        assertTrue(enthaelt(options, "NW", "AUS"));
    }

    @Test
    void givenQuMitDiagonalemArmSechs_thenRichtungenNoUndSw() {
        final List<VerkehrsbeziehungOptionDTO> options = factory.moeglicheBeziehungen(
                "QU", false, List.of(new KnotenarmDTO(6, "Arm 6")));

        // Arm 6 zeigt diagonal -> Achse NO/SW (vgl. KnotenLageForm.vue: Arm 6/8 -> NO/SW)
        assertEquals(2, options.size());
        assertTrue(options.stream().anyMatch(o -> "NO".equals(o.richtung())));
        assertTrue(options.stream().anyMatch(o -> "SW".equals(o.richtung())));
    }

    @Test
    void givenQjsMitDiagonalemArmSieben_thenStrassenseitenSoUndNw() {
        final List<VerkehrsbeziehungOptionDTO> options = factory.moeglicheBeziehungen(
                "QJS", false, List.of(new KnotenarmDTO(7, "Arm 7")));

        // Arm 7 -> Arm 7 (Selbstbeziehung) mit Achse SO/NW
        assertEquals(2, options.size());
        assertTrue(options.stream().anyMatch(o -> "SO".equals(o.strassenseite())));
        assertTrue(options.stream().anyMatch(o -> "NW".equals(o.strassenseite())));
    }

    @Test
    void givenKreuzungMitEinemArm_thenSelbstbeziehungWaehlbar() {
        final List<VerkehrsbeziehungOptionDTO> options = factory.moeglicheBeziehungen(
                "N", false, List.of(new KnotenarmDTO(1, "Arm 1")));

        assertEquals(1, options.size());
        assertEquals(1, options.get(0).von());
        assertEquals(1, options.get(0).nach());
    }

    private boolean enthaelt(final List<VerkehrsbeziehungOptionDTO> options, final String seite, final String richtung) {
        return options.stream().anyMatch(o -> seite.equals(o.strassenseite()) && richtung.equals(o.richtung()));
    }
}
