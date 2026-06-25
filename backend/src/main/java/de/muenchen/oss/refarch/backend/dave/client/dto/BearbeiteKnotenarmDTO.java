package de.muenchen.oss.refarch.backend.dave.client.dto;

import lombok.Data;

/**
 * Node arm (Knotenarm) of a Zählung in the internal edit contract.
 * Field name {@code Strassenname} mirrors the DAVe backend DTO exactly to keep the JSON shape equal.
 */
@Data
@SuppressWarnings({ "checkstyle:MemberName", "PMD.FieldNamingConventions" })
public class BearbeiteKnotenarmDTO {

    private int nummer;

    private String Strassenname;

}
