package de.muenchen.oss.refarch.backend.dave.client.dto;

import de.muenchen.oss.refarch.backend.dave.client.enums.Bewegungsrichtung;
import de.muenchen.oss.refarch.backend.dave.client.enums.Himmelsrichtung;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Longitudinal traffic (Längsverkehr) movement relation for Zählart FJS, external contract.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExternalLaengsverkehrDTO extends ExternalBewegungsbeziehungDTO {

    private Integer knotenarm;

    private Bewegungsrichtung richtung;

    private Himmelsrichtung strassenseite;

}
