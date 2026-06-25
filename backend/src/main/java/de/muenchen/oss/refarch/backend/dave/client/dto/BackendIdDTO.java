package de.muenchen.oss.refarch.backend.dave.client.dto;

import lombok.Data;

/**
 * Generic id response returned by the DAVe backend's save/update endpoints.
 */
@Data
public class BackendIdDTO {

    private String id;

}
