package de.muenchen.oss.refarch.backend.dave.client.dto;

import java.util.List;
import lombok.Data;

/**
 * Base type for a movement relation (Bewegungsbeziehung) in the external (service provider) contract.
 */
@Data
public abstract class ExternalBewegungsbeziehungDTO {

    private String id;

    private List<ZeitintervallDTO> zeitintervalle;

    private HochrechnungsfaktorDTO hochrechnungsfaktor;

}
