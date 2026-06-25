package de.muenchen.oss.refarch.backend.dave.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Builds the {@link RestClient} used to talk to the DAVe backend. When
 * {@code dave.backend.auth.mode=oauth2} a request interceptor adds a bearer token obtained from
 * {@link DaveTokenProvider}; for {@code none} no Authorization header is sent.
 */
@Configuration
@Slf4j
public class DaveBackendClientConfig {

    public static final String DAVE_REST_CLIENT = "daveRestClient";

    @Bean(DAVE_REST_CLIENT)
    public RestClient daveRestClient(final RestClient.Builder builder,
            final DaveBackendProperties properties,
            final DaveTokenProvider tokenProvider) {
        log.info("Configuring DAVe backend client: base-url={}, auth-mode={}", properties.baseUrl(), properties.auth().mode());
        RestClient.Builder configured = builder.baseUrl(properties.baseUrl());
        if (properties.auth().mode() == DaveBackendProperties.AuthMode.OAUTH2) {
            configured = configured.requestInterceptor((request, body, execution) -> {
                request.getHeaders().setBearerAuth(tokenProvider.getToken());
                return execution.execute(request, body);
            });
        }
        return configured.build();
    }
}
