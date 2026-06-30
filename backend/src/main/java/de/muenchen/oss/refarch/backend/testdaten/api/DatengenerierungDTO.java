package de.muenchen.oss.refarch.backend.testdaten.api;

import java.util.Map;

/**
 * Selection of how the counting values are generated, per vehicle class (PKW, LKW, ...).
 * If no specification is given for a vehicle class, the generator falls back to
 * {@link Modus#ZUFALL}
 * with a sensible per-class default magnitude.
 */
public record DatengenerierungDTO(Map<String, Spezifikation> proFahrzeug) {

    /**
     * @param modus how the value series is generated
     * @param wert meaning depends on the modus: constant value (KONSTANT), start value (AUFSTEIGEND),
     *            peak/rush-hour value (REALISTISCH) or maximum (ZUFALL). May be null -> per-class
     *            default.
     */
    public record Spezifikation(Modus modus, Integer wert) {
    }

    /**
     * Generation strategy for a vehicle class.
     */
    public enum Modus {
        /** Always the same value (durchgehend selbe Zahl). */
        KONSTANT,
        /** Starts at the chosen value and increments by 1 per interval (aufsteigend, +1). */
        AUFSTEIGEND,
        /** Realistic daily curve with morning and evening peaks (realistisch verteilt). */
        REALISTISCH,
        /** Uniformly random between 0 and the chosen value (komplett zufällig). */
        ZUFALL
    }
}
