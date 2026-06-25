package de.muenchen.oss.refarch.backend.dave.client.dto;

import lombok.Data;

/**
 * Node arm (Knotenarm) of a Zählung in the external (service provider) contract, including the
 * uploaded {@code filename}. Field name {@code Strassenname} mirrors the DAVe backend DTO exactly.
 */
@Data
@SuppressWarnings({ "checkstyle:MemberName", "PMD.FieldNamingConventions" })
public class ExternalKnotenarmDTO {

    private int nummer;

    private String Strassenname;

    private String filename;

}
