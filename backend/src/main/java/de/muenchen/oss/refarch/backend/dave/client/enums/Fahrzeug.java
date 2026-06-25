package de.muenchen.oss.refarch.backend.dave.client.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Vehicle categories and classes (Fahrzeugkategorien/-klassen). The enum constant name matches the
 * value expected by the DAVe backend (Jackson serializes enums by their constant name).
 */
@Getter
@RequiredArgsConstructor
public enum Fahrzeug {

    // Kategorien
    KFZ("Kfz"),
    SV("Schwerverkehr"),
    GV("Güterverkehr"),
    SV_P("Schwerverkehrsanteil"),
    GV_P("Güterverkehrsanteil"),
    // Klassen
    PKW("Pkw"),
    LKW("Lkw"),
    LZ("Lastzug"),
    BUS("Bus"),
    KRAD("Kraftrad"),
    RAD("Rad"),
    FUSS("Fuß"),
    PKW_EINHEIT("Pkw-Einheiten");

    private final String bezeichnung;

}
