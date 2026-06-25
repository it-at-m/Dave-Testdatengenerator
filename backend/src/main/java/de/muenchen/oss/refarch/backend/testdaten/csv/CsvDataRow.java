package de.muenchen.oss.refarch.backend.testdaten.csv;

/**
 * A parsed data row of a generated CSV file (11 columns of the portal format).
 */
public record CsvDataRow(
        int intervallNummer,
        String nach,
        String strassenseite,
        String richtung,
        Integer pkw,
        Integer lkw,
        Integer lastzuege,
        Integer busse,
        Integer kraftraeder,
        Integer fahrradfahrer,
        Integer fussgaenger) {
}
