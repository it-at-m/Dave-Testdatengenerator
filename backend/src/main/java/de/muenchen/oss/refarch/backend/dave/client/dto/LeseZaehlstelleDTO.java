package de.muenchen.oss.refarch.backend.dave.client.dto;

import lombok.Data;

/**
 * Detail data of a Zählstelle returned by {@code GET /zaehlstelle/byId}. Only the fields needed to
 * create a new Zählung are modeled; further fields are ignored on deserialization.
 */
@Data
public class LeseZaehlstelleDTO {

    private String id;

    private String nummer;

    private String name;

    private String stadtbezirk;

    private Integer stadtbezirkNummer;

    private Double lat;

    private Double lng;

    private String kommentar;

}
