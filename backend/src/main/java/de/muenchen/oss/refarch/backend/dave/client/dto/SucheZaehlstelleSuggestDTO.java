package de.muenchen.oss.refarch.backend.dave.client.dto;

import lombok.Data;

/**
 * A single Zählstelle suggestion returned by the DAVe backend search.
 */
@Data
public class SucheZaehlstelleSuggestDTO {

    private String text;

    private String id;

    private Boolean sichtbarDatenportal;

}
