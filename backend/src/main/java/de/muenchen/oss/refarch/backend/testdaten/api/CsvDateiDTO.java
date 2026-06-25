package de.muenchen.oss.refarch.backend.testdaten.api;

/**
 * One generated CSV file (one per Knotenarm, matching the selfservice portal upload format).
 */
public record CsvDateiDTO(int knotenarmNummer, String filename, String content) {
}
