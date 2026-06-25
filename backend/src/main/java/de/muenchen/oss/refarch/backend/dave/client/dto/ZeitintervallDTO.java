package de.muenchen.oss.refarch.backend.dave.client.dto;

import lombok.Data;

/**
 * A single 15-minute counting interval with the counted values per vehicle class.
 */
@Data
public class ZeitintervallDTO {

    private String startUhrzeit;

    private String endeUhrzeit;

    private Integer pkw;

    private Integer lkw;

    private Integer lastzuege;

    private Integer busse;

    private Integer kraftraeder;

    private Integer fahrradfahrer;

    private Integer fussgaenger;

}
