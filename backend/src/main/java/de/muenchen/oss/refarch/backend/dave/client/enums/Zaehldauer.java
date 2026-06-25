package de.muenchen.oss.refarch.backend.dave.client.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Counting duration (Zähldauer). The enum constant name is the exact value the DAVe backend stores
 * and resolves via {@code Zaehldauer.valueOf(...)}. {@code anzahlZeitintervalle} is the number of
 * 15-minute intervals expected for a full count of this duration.
 */
@Getter
@RequiredArgsConstructor
public enum Zaehldauer {

    DAUER_2_X_4_STUNDEN("Kurzzeiterhebung (2x4h)", 32),
    DAUER_24_STUNDEN("Tageszählung (24h)", 96),
    DAUER_16_STUNDEN("16 Stunden", 64),
    DAUER_13_STUNDEN("13 Stunden (6-19 Uhr)", 52),
    SONSTIGE("Sonstige", 0);

    private final String bezeichnung;

    private final int anzahlZeitintervalle;

}
