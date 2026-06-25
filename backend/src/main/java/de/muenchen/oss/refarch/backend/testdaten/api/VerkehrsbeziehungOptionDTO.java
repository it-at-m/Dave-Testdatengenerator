package de.muenchen.oss.refarch.backend.testdaten.api;

/**
 * A selectable movement relation ("Pfeil") for a Zählung. Depending on {@code typ} only a subset of
 * the attributes is populated:
 * <ul>
 * <li>VERKEHRSBEZIEHUNG (Kreuzung): {@code von}, {@code nach}</li>
 * <li>VERKEHRSBEZIEHUNG (Kreisverkehr): {@code knotenarm}, {@code hinein|heraus|vorbei}</li>
 * <li>VERKEHRSBEZIEHUNG (QJS): {@code von}, {@code nach}, {@code strassenseite}</li>
 * <li>LAENGSVERKEHR (FJS): {@code knotenarm}, {@code richtung} (EIN/AUS), {@code strassenseite}</li>
 * <li>QUERUNGSVERKEHR (QU): {@code knotenarm}, {@code richtung} (Himmelsrichtung)</li>
 * </ul>
 * {@code key} is a stable identifier and {@code label} a human readable description for the UI.
 */
public record VerkehrsbeziehungOptionDTO(
        String key,
        String label,
        RelationTyp typ,
        Integer von,
        Integer nach,
        Integer knotenarm,
        Boolean hinein,
        Boolean heraus,
        Boolean vorbei,
        String strassenseite,
        String richtung) {

    public enum RelationTyp {
        VERKEHRSBEZIEHUNG,
        LAENGSVERKEHR,
        QUERUNGSVERKEHR
    }
}
