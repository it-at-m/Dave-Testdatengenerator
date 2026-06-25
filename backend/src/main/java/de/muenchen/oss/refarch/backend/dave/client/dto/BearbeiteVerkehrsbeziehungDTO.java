package de.muenchen.oss.refarch.backend.dave.client.dto;

import de.muenchen.oss.refarch.backend.dave.client.enums.Himmelsrichtung;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Traffic relation (Verkehrsbeziehung) of an intersection or roundabout, internal edit contract.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BearbeiteVerkehrsbeziehungDTO extends BearbeiteBewegungsbeziehungDTO {

    private Boolean isKreuzung;

    // Kreuzung (intersection)
    private Integer von;

    private Integer nach;

    // Kreisverkehr (roundabout)
    private Integer knotenarm;

    private Boolean hinein;

    private Boolean heraus;

    private Boolean vorbei;

    private Himmelsrichtung strassenseite;

}
