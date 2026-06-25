package de.muenchen.oss.refarch.backend.dave.client.dto;

import de.muenchen.oss.refarch.backend.dave.client.enums.Himmelsrichtung;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Crossing traffic (Querungsverkehr) movement relation for Zählart QU, external contract.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExternalQuerungsverkehrDTO extends ExternalBewegungsbeziehungDTO {

    private Integer knotenarm;

    private Himmelsrichtung richtung;

}
