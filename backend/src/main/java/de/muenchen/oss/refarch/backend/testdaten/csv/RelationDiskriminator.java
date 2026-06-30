package de.muenchen.oss.refarch.backend.testdaten.csv;

import de.muenchen.oss.refarch.backend.dave.client.dto.ExternalLaengsverkehrDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.ExternalQuerungsverkehrDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.ExternalVerkehrsbeziehungDTO;
import de.muenchen.oss.refarch.backend.dave.client.enums.Zaehlart;
import de.muenchen.oss.refarch.backend.testdaten.api.VerkehrsbeziehungOptionDTO;

/**
 * Single source of truth for identifying a movement relation across three representations:
 * the selectable option, the parsed CSV row and the (re-read) external DTO. The produced key lets
 * the mapper attach the time intervals from a CSV row to the matching backend relation (by its
 * persisted UUID).
 */
public final class RelationDiskriminator {

    private RelationDiskriminator() {
    }

    /**
     * The CSV {@code nach}/{@code Strassenseite}/{@code Richtung} discriminator columns of a relation.
     */
    public record CsvColumns(String nach, String strassenseite, String richtung) {
    }

    public static int ownerArm(final VerkehrsbeziehungOptionDTO option) {
        return switch (option.typ()) {
        case VERKEHRSBEZIEHUNG -> isKreisverkehr(option) ? option.knotenarm() : option.von();
        case LAENGSVERKEHR, QUERUNGSVERKEHR -> option.knotenarm();
        };
    }

    public static CsvColumns csvColumns(final VerkehrsbeziehungOptionDTO option) {
        return switch (option.typ()) {
        case VERKEHRSBEZIEHUNG -> isKreisverkehr(option)
                ? new CsvColumns(fahrbewegungToken(option.hinein(), option.heraus(), option.vorbei()), "", "")
                : new CsvColumns(String.valueOf(option.nach()), nullToEmpty(option.strassenseite()), "");
        case LAENGSVERKEHR -> new CsvColumns("", nullToEmpty(option.strassenseite()), nullToEmpty(option.richtung()));
        case QUERUNGSVERKEHR -> new CsvColumns("", "", nullToEmpty(option.richtung()));
        };
    }

    public static String keyForOption(final VerkehrsbeziehungOptionDTO o) {
        return switch (o.typ()) {
        case VERKEHRSBEZIEHUNG -> {
            if (isKreisverkehr(o)) {
                yield kreisverkehrKey(o.knotenarm(), fahrbewegungToken(o.hinein(), o.heraus(), o.vorbei()));
            }
            yield isNotBlank(o.strassenseite())
                    ? qjsKey(o.von(), o.nach(), o.strassenseite())
                    : kreuzungKey(o.von(), o.nach());
        }
        case LAENGSVERKEHR -> laengsKey(o.knotenarm(), o.richtung(), o.strassenseite());
        case QUERUNGSVERKEHR -> querungKey(o.knotenarm(), o.richtung());
        };
    }

    public static String keyForExternal(final ExternalVerkehrsbeziehungDTO vb) {
        if (Boolean.FALSE.equals(vb.getIsKreuzung()) && vb.getKnotenarm() != null) {
            return kreisverkehrKey(vb.getKnotenarm(), fahrbewegungToken(vb.getHinein(), vb.getHeraus(), vb.getVorbei()));
        }
        return vb.getStrassenseite() != null
                ? qjsKey(vb.getVon(), vb.getNach(), vb.getStrassenseite().name())
                : kreuzungKey(vb.getVon(), vb.getNach());
    }

    public static String keyForExternal(final ExternalLaengsverkehrDTO lv) {
        return laengsKey(lv.getKnotenarm(),
                lv.getRichtung() == null ? null : lv.getRichtung().name(),
                lv.getStrassenseite() == null ? null : lv.getStrassenseite().name());
    }

    public static String keyForExternal(final ExternalQuerungsverkehrDTO qv) {
        return querungKey(qv.getKnotenarm(), qv.getRichtung() == null ? null : qv.getRichtung().name());
    }

    public static String keyForRow(final String zaehlartName, final boolean kreisverkehr, final int ownerArm, final CsvDataRow row) {
        final Zaehlart zaehlart = Zaehlart.valueOf(zaehlartName);
        if (zaehlart == Zaehlart.FJS) {
            return laengsKey(ownerArm, row.richtung(), row.strassenseite());
        }
        if (zaehlart == Zaehlart.QU) {
            return querungKey(ownerArm, row.richtung());
        }
        if (kreisverkehr) {
            return kreisverkehrKey(ownerArm, row.nach());
        }
        if (zaehlart == Zaehlart.QJS) {
            return qjsKey(ownerArm, parseIntOrNull(row.nach()), row.strassenseite());
        }
        return kreuzungKey(ownerArm, parseIntOrNull(row.nach()));
    }

    public static String fahrbewegungToken(final Boolean hinein, final Boolean heraus, final Boolean vorbei) {
        if (Boolean.TRUE.equals(hinein)) {
            return "e";
        }
        if (Boolean.TRUE.equals(heraus)) {
            return "a";
        }
        if (Boolean.TRUE.equals(vorbei)) {
            return "v";
        }
        return "";
    }

    private static boolean isKreisverkehr(final VerkehrsbeziehungOptionDTO o) {
        return Boolean.TRUE.equals(o.hinein()) || Boolean.TRUE.equals(o.heraus()) || Boolean.TRUE.equals(o.vorbei());
    }

    private static String kreuzungKey(final Integer von, final Integer nach) {
        return "VB|" + von + "|" + nach;
    }

    private static String qjsKey(final Integer von, final Integer nach, final String strassenseite) {
        return "VB|" + von + "|" + nach + "|" + nullToEmpty(strassenseite);
    }

    private static String kreisverkehrKey(final Integer knotenarm, final String fahrbewegungToken) {
        return "KV|" + knotenarm + "|" + nullToEmpty(fahrbewegungToken);
    }

    private static String laengsKey(final Integer knotenarm, final String richtung, final String strassenseite) {
        return "LV|" + knotenarm + "|" + nullToEmpty(richtung) + "|" + nullToEmpty(strassenseite);
    }

    private static String querungKey(final Integer knotenarm, final String richtung) {
        return "QV|" + knotenarm + "|" + nullToEmpty(richtung);
    }

    private static String nullToEmpty(final String value) {
        return value == null ? "" : value;
    }

    private static boolean isNotBlank(final String value) {
        return value != null && !value.isBlank();
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
