package de.muenchen.oss.refarch.backend.testdaten;

import de.muenchen.oss.refarch.backend.dave.client.enums.Bewegungsrichtung;
import de.muenchen.oss.refarch.backend.dave.client.enums.Himmelsrichtung;
import de.muenchen.oss.refarch.backend.dave.client.enums.Zaehlart;
import de.muenchen.oss.refarch.backend.testdaten.api.KnotenarmDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.VerkehrsbeziehungOptionDTO;
import de.muenchen.oss.refarch.backend.testdaten.api.VerkehrsbeziehungOptionDTO.RelationTyp;
import de.muenchen.oss.refarch.backend.testdaten.csv.RelationDiskriminator;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Computes the possible movement relations ("Pfeile") that can be selected for a Zählung, depending
 * on its Zählart, the roundabout flag and the configured node arms.
 */
@Component
public class VerkehrsbeziehungFactory {

    public List<VerkehrsbeziehungOptionDTO> moeglicheBeziehungen(final String zaehlartName, final boolean kreisverkehr,
            final List<KnotenarmDTO> knotenarme) {
        final Zaehlart zaehlart = Zaehlart.valueOf(zaehlartName);
        final List<Integer> arme = knotenarme.stream().map(KnotenarmDTO::nummer).sorted().toList();
        return switch (zaehlart) {
        case FJS -> laengsverkehr(arme);
        case QU -> querungsverkehr(arme);
        case QJS -> querschnittJeStrassenseite(arme);
        default -> kreisverkehr ? kreisverkehr(arme) : kreuzung(arme);
        };
    }

    private List<VerkehrsbeziehungOptionDTO> kreuzung(final List<Integer> arme) {
        final List<VerkehrsbeziehungOptionDTO> options = new ArrayList<>();
        // Alle Kombinationen sind wählbar - inklusive Arm -> sich selbst (z.B. Arm 1 -> Arm 1).
        for (final Integer von : arme) {
            for (final Integer nach : arme) {
                options.add(finish(new VerkehrsbeziehungOptionDTO(
                        null, "Arm " + von + " → Arm " + nach, RelationTyp.VERKEHRSBEZIEHUNG,
                        von, nach, null, null, null, null, null, null)));
            }
        }
        return options;
    }

    private List<VerkehrsbeziehungOptionDTO> kreisverkehr(final List<Integer> arme) {
        final List<VerkehrsbeziehungOptionDTO> options = new ArrayList<>();
        for (final Integer arm : arme) {
            options.add(finish(new VerkehrsbeziehungOptionDTO(
                    null, "Arm " + arm + ": hinein", RelationTyp.VERKEHRSBEZIEHUNG,
                    null, null, arm, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null, null)));
            options.add(finish(new VerkehrsbeziehungOptionDTO(
                    null, "Arm " + arm + ": heraus", RelationTyp.VERKEHRSBEZIEHUNG,
                    null, null, arm, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, null, null)));
            options.add(finish(new VerkehrsbeziehungOptionDTO(
                    null, "Arm " + arm + ": vorbei", RelationTyp.VERKEHRSBEZIEHUNG,
                    null, null, arm, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, null, null)));
        }
        return options;
    }

    private List<VerkehrsbeziehungOptionDTO> querschnittJeStrassenseite(final List<Integer> arme) {
        final List<VerkehrsbeziehungOptionDTO> options = new ArrayList<>();
        // QjS: je Verkehrsbeziehung beide gegenüberliegenden Straßenseiten; alle Kombinationen wählbar
        // (inkl. Arm -> sich selbst). Vgl. examples/testdata/QJS (nach gefüllt, Strassenseite N+S bzw. O+W).
        for (final Integer von : arme) {
            for (final Integer nach : arme) {
                for (final Himmelsrichtung seite : strassenseitenFuerArm(von)) {
                    options.add(finish(new VerkehrsbeziehungOptionDTO(
                            null, "Arm " + von + " → Arm " + nach + " (Seite " + seite.name() + ")", RelationTyp.VERKEHRSBEZIEHUNG,
                            von, nach, null, null, null, null, seite.name(), null)));
                }
            }
        }
        return options;
    }

    private List<VerkehrsbeziehungOptionDTO> laengsverkehr(final List<Integer> arme) {
        final List<VerkehrsbeziehungOptionDTO> options = new ArrayList<>();
        // FjS: je Knotenarm beide gegenüberliegenden Straßenseiten, jeweils EIN und AUS.
        // Vgl. examples/testdata/FJS (Strassenseite O+W bzw. N+S, Richtung EIN/AUS).
        for (final Integer arm : arme) {
            for (final Himmelsrichtung seite : strassenseitenFuerArm(arm)) {
                for (final Bewegungsrichtung richtung : Bewegungsrichtung.values()) {
                    options.add(finish(new VerkehrsbeziehungOptionDTO(
                            null, "Arm " + arm + ": " + richtung.name() + " (Seite " + seite.name() + ")", RelationTyp.LAENGSVERKEHR,
                            null, null, arm, null, null, null, seite.name(), richtung.name())));
                }
            }
        }
        return options;
    }

    private List<VerkehrsbeziehungOptionDTO> querungsverkehr(final List<Integer> arme) {
        final List<VerkehrsbeziehungOptionDTO> options = new ArrayList<>();
        // Qu: je Knotenarm beide gegenüberliegenden Himmelsrichtungen als Richtung.
        // Vgl. examples/testdata/QU (Richtung N+S, nach und Strassenseite leer).
        for (final Integer arm : arme) {
            for (final Himmelsrichtung richtung : strassenseitenFuerArm(arm)) {
                options.add(finish(new VerkehrsbeziehungOptionDTO(
                        null, "Arm " + arm + ": Richtung " + richtung.name(), RelationTyp.QUERUNGSVERKEHR,
                        null, null, arm, null, null, null, null, richtung.name())));
            }
        }
        return options;
    }

    private VerkehrsbeziehungOptionDTO finish(final VerkehrsbeziehungOptionDTO option) {
        return new VerkehrsbeziehungOptionDTO(
                RelationDiskriminator.keyForOption(option), option.label(), option.typ(),
                option.von(), option.nach(), option.knotenarm(),
                option.hinein(), option.heraus(), option.vorbei(),
                option.strassenseite(), option.richtung());
    }

    /**
     * Liefert die beiden gegenüberliegenden Straßenseiten/Himmelsrichtungen eines Knotenarms.
     * Die Arme 1-4 zeigen in die Haupt-, die Arme 5-8 in die kombinierten Himmelsrichtungen.
     * Die Zuordnung entspricht der CSV-Upload-Validierung des Selfservice-Portals (KnotenLageForm.vue)
     * und dem Belastungsplan des dave-frontend (BelastungsplanMethods.ts):
     * Arm 1/3 -> O/W, Arm 2/4 -> N/S, Arm 5/7 -> SO/NW, Arm 6/8 -> NO/SW.
     * Siehe docs/knotenarm-himmelsrichtung.md.
     */
    private List<Himmelsrichtung> strassenseitenFuerArm(final int arm) {
        return switch (arm) {
        case 2, 4 -> List.of(Himmelsrichtung.N, Himmelsrichtung.S);
        case 5, 7 -> List.of(Himmelsrichtung.SO, Himmelsrichtung.NW);
        case 6, 8 -> List.of(Himmelsrichtung.NO, Himmelsrichtung.SW);
        // Arm 1/3 sowie alle nicht abgedeckten (ungeraden) Arme: Ost-West-Achse.
        default -> List.of(Himmelsrichtung.O, Himmelsrichtung.W);
        };
    }
}
