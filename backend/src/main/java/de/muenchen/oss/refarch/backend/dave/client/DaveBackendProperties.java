package de.muenchen.oss.refarch.backend.dave.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration for the connection to the DAVe backend that generated counts are imported into.
 *
 * <pre>
 * dave:
 *   backend:
 *     base-url: http://localhost:8080
 *     auth:
 *       mode: none            # none | oauth2
 *       token-uri: ...        # only for oauth2 (client-credentials token endpoint)
 *       client-id: ...
 *       client-secret: ...
 *       scope: ...
 * </pre>
 */
@ConfigurationProperties(prefix = "dave.backend")
public record DaveBackendProperties(

        @DefaultValue("http://localhost:8080") String baseUrl,

        @DefaultValue Auth auth) {

    public enum AuthMode {
        NONE,
        OAUTH2
    }

    public record Auth(
            @DefaultValue("NONE") AuthMode mode,
            @DefaultValue("") String tokenUri,
            @DefaultValue("") String clientId,
            @DefaultValue("") String clientSecret,
            @DefaultValue("") String scope) {
    }
}
