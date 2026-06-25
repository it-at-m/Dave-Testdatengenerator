package de.muenchen.oss.refarch.backend.dave.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code POST /zaehlung/updateStatus}. {@code status} must be a value of the DAVe
 * {@code Status} enum name.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusDTO {

    private String zaehlungId;

    private String status;

    private String dienstleisterkennung;

}
