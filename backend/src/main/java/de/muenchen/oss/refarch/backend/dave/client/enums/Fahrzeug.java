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

    PKW("Pkw"),
    LKW("Lkw"),
    LZ("Lastzug"),
    BUS("Bus"),
    KRAD("Kraftrad"),
    RAD("Rad"),
    FUSS("Fuß");

    private final String bezeichnung;

}
