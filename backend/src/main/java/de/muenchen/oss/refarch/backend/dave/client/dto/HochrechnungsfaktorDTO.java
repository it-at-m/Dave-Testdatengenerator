package de.muenchen.oss.refarch.backend.dave.client.dto;

import java.util.UUID;
import lombok.Data;

/**
 * Extrapolation factor (Hochrechnungsfaktor) attached to a movement relation. Optional; may be left
 * empty for plain test data.
 */
@Data
public class HochrechnungsfaktorDTO {

    private UUID id;

    private Long entityVersion;

    private String matrix;

    private Double kfz;

    private Double sv;

    private Double gv;

    private boolean active;

    private boolean defaultFaktor;

}
