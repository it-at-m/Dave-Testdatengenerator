package de.muenchen.oss.refarch.backend.configuration.filter;

import static de.muenchen.oss.refarch.backend.TestConstants.SPRING_TEST_PROFILE;

import de.muenchen.oss.refarch.backend.MicroServiceApplication;
import de.muenchen.oss.refarch.backend.TestSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(
        classes = { MicroServiceApplication.class },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureRestTestClient
@ActiveProfiles(profiles = { SPRING_TEST_PROFILE })
@Import(TestSecurityConfiguration.class)
class CacheControlFilterTest {

    // public, always reachable endpoint that does not set its own Cache-Control header
    private static final String PUBLIC_ENDPOINT_URL = "/actuator/health";

    private static final String EXPECTED_CACHE_CONTROL_HEADER_VALUES = "no-cache, no-store, must-revalidate";

    @Autowired
    private RestTestClient restTestClient;

    @Test
    void givenAnyEndpoint_thenCacheControlHeadersPresent() {
        restTestClient.get()
                .uri(PUBLIC_ENDPOINT_URL)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(HttpHeaders.CACHE_CONTROL)
                .expectHeader().valueEquals(HttpHeaders.CACHE_CONTROL, EXPECTED_CACHE_CONTROL_HEADER_VALUES);
    }

}
