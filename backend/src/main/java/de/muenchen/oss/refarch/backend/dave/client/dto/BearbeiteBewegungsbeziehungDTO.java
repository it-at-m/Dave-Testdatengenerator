package de.muenchen.oss.refarch.backend.dave.client.dto;

import java.util.List;
import lombok.Data;

/**
 * Base type for a movement relation (Bewegungsbeziehung) in the internal edit contract.
 */
@Data
public abstract class BearbeiteBewegungsbeziehungDTO {

    private String id;

    private List<ZeitintervallDTO> zeitintervalle;

    private HochrechnungsfaktorDTO hochrechnungsfaktor;

}
