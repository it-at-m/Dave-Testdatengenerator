package de.muenchen.oss.refarch.backend.dave.client.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Counting type (Zählart). The enum constant name is the exact value expected by the DAVe backend.
 */
@Getter
@RequiredArgsConstructor
public enum Zaehlart {

    N("Standardzählung"),
    H("Hauptverkehrsrichtung/Oberfläche/Hoch"),
    Q("Querschnitt"),
    Q_("Querschnitt/Sonderzählung"),
    QB("Bahnschnitt"),
    QH("Querschnitt/Hauptverkehrsrichtung"),
    QI("Isarschnitt"),
    QS("Stadtgrenzenzählung"),
    QT("Querschnitt Tunnel/Unterführung/Tief"),
    QR("Querschnitt Radverkehr"),
    R("Radverkehrszählung"),
    QJS("Querschnitt je Straßenseite"),
    FJS("Fuß & Rad je Straßenseite"),
    QU("Querung"),
    T("Tunnel/Unterführung/Tief"),
    TK("Teilknoten");

    private final String bezeichnung;

}
