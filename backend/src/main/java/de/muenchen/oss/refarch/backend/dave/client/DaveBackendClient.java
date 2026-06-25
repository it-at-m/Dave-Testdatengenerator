package de.muenchen.oss.refarch.backend.dave.client;

import de.muenchen.oss.refarch.backend.dave.client.dto.BackendIdDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.BearbeiteZaehlungDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.ExternalZaehlungDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.LeseZaehlstelleDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.SearchAndFilterOptionsDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.SucheComplexSuggestsDTO;
import de.muenchen.oss.refarch.backend.dave.client.dto.UpdateStatusDTO;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Thin client around the relevant DAVe backend REST endpoints used by the test-data workflow.
 */
@Component
@Slf4j
public class DaveBackendClient {

    private final RestClient restClient;

    public DaveBackendClient(@Qualifier(DaveBackendClientConfig.DAVE_REST_CLIENT) final RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Searches for Zählstellen matching the given query.
     */
    public SucheComplexSuggestsDTO suggestZaehlstellen(final String query) {
        final SearchAndFilterOptionsDTO options = new SearchAndFilterOptionsDTO(false, true, List.of());
        return execute("suggest Zählstellen", () -> restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/suggest").queryParam("query", query).build())
                .body(options)
                .retrieve()
                .body(SucheComplexSuggestsDTO.class));
    }

    /**
     * Loads the detail data of a single Zählstelle.
     */
    public LeseZaehlstelleDTO getZaehlstelle(final String id) {
        return execute("Zählstelle laden", () -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/zaehlstelle/byId").queryParam("id", id).build())
                .retrieve()
                .body(LeseZaehlstelleDTO.class));
    }

    /**
     * Creates (or updates) the Zählung structure internally. Requires FACHADMIN on the backend.
     */
    public BackendIdDTO saveZaehlung(final BearbeiteZaehlungDTO zaehlung, final String zaehlstelleId) {
        return execute("Zählung anlegen", () -> restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/zaehlung/save").queryParam("zaehlstelle_id", zaehlstelleId).build())
                .body(zaehlung)
                .retrieve()
                .body(BackendIdDTO.class));
    }

    /**
     * Updates the lifecycle status of a Zählung.
     */
    public BackendIdDTO updateStatus(final UpdateStatusDTO updateStatus) {
        return execute("Status aktualisieren", () -> restClient.post()
                .uri("/zaehlung/updateStatus")
                .body(updateStatus)
                .retrieve()
                .body(BackendIdDTO.class));
    }

    /**
     * Loads the Zählungen visible to the external service provider, including the persisted ids of
     * their movement relations (needed to attach uploaded time intervals).
     */
    public List<ExternalZaehlungDTO> getZaehlungenForExternal() {
        return execute("Zählungen für Dienstleister laden", () -> restClient.get()
                .uri("/zaehlung/getZaehlungenForExternal")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ExternalZaehlungDTO>>() {
                }));
    }

    /**
     * Uploads count data (time intervals) into an existing Zählung.
     */
    public BackendIdDTO saveExternal(final ExternalZaehlungDTO zaehlung) {
        return execute("Zähldaten hochladen", () -> restClient.post()
                .uri("/zaehlung/saveExternal")
                .body(zaehlung)
                .retrieve()
                .body(BackendIdDTO.class));
    }

    private <T> T execute(final String action, final RestCall<T> call) {
        try {
            return call.run();
        } catch (final RestClientException ex) {
            log.error("DAVe backend call failed: {}", action, ex);
            throw new DaveBackendException("DAVe-Backend-Aufruf fehlgeschlagen (" + action + "): " + ex.getMessage(), ex);
        }
    }

    @FunctionalInterface
    private interface RestCall<T> {
        T run() throws RestClientException;
    }
}
