package de.muenchen.oss.refarch.backend.dave.client.dto;

import de.muenchen.oss.refarch.backend.dave.client.enums.Himmelsrichtung;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Crossing traffic (Querungsverkehr) movement relation for Zählart QU, internal edit contract.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BearbeiteQuerungsverkehrDTO extends BearbeiteBewegungsbeziehungDTO {

    private Integer knotenarm;

    private Himmelsrichtung richtung;

}
