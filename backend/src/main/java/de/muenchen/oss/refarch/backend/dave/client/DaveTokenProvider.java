package de.muenchen.oss.refarch.backend.dave.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * Obtains and caches an OAuth2 bearer token via the client-credentials grant. Only used when
 * {@code dave.backend.auth.mode=oauth2}. The token is refreshed shortly before it expires.
 */
@Component
@Slf4j
public class DaveTokenProvider {

    private static final int EXPIRY_SAFETY_SECONDS = 30;

    private final DaveBackendProperties properties;
    private final RestClient tokenRestClient;
    private final ReentrantLock lock = new ReentrantLock();

    private String cachedToken;
    private Instant expiresAt = Instant.EPOCH;

    public DaveTokenProvider(final DaveBackendProperties properties) {
        this.properties = properties;
        this.tokenRestClient = RestClient.create();
    }

    public String getToken() {
        lock.lock();
        try {
            if (cachedToken == null || Instant.now().isAfter(expiresAt)) {
                refreshToken();
            }
            return cachedToken;
        } finally {
            lock.unlock();
        }
    }

    private void refreshToken() {
        final DaveBackendProperties.Auth auth = properties.auth();
        if (!StringUtils.hasText(auth.tokenUri())) {
            throw new DaveBackendException("OAuth2 is enabled but dave.backend.auth.token-uri is not configured.");
        }
        final MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", auth.clientId());
        form.add("client_secret", auth.clientSecret());
        if (StringUtils.hasText(auth.scope())) {
            form.add("scope", auth.scope());
        }
        try {
            final TokenResponse response = tokenRestClient.post()
                    .uri(auth.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(TokenResponse.class);
            if (response == null || !StringUtils.hasText(response.accessToken())) {
                throw new DaveBackendException("Token endpoint returned no access_token.");
            }
            cachedToken = response.accessToken();
            final long expiresIn = response.expiresIn() == null ? 60L : response.expiresIn();
            expiresAt = Instant.now().plusSeconds(Math.max(1, expiresIn - EXPIRY_SAFETY_SECONDS));
            log.debug("Obtained new DAVe backend access token, valid for {}s", expiresIn);
        } catch (final DaveBackendException ex) {
            throw ex;
        } catch (final RuntimeException ex) {
            throw new DaveBackendException("Could not obtain OAuth2 token from " + auth.tokenUri(), ex);
        }
    }

    private record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") Long expiresIn,
            @JsonProperty("token_type") String tokenType) {
    }
}
