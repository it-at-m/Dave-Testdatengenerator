package de.muenchen.oss.refarch.backend.dave.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Geographic point matching the JSON shape of the Elasticsearch {@code GeoPoint} used by the DAVe
 * backend ({@code {"lat": ..., "lon": ...}}).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoPointDTO {

    private double lat;

    private double lon;

}
