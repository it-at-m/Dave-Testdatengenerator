package de.muenchen.oss.refarch.backend.dave.client.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for the DAVe backend search endpoints ({@code POST /suggest}).
 * {@code messstelleVerkehrsart} contains values of the DAVe {@code Verkehrsart} enum (KFZ, RAD, UNBEKANNT).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchAndFilterOptionsDTO {

    private boolean searchInMessstellen;

    private boolean searchInZaehlstellen;

    private List<String> messstelleVerkehrsart;

}
