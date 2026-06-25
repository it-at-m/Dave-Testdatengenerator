package de.muenchen.oss.refarch.backend.dave.client.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * Search result of the DAVe backend. Only the Zählstelle suggestions are relevant for this tool;
 * other suggestion lists are ignored on deserialization.
 */
@Data
public class SucheComplexSuggestsDTO {

    private List<SucheZaehlstelleSuggestDTO> zaehlstellenSuggests = new ArrayList<>();

}
