package de.muenchen.oss.refarch.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Application class for starting the microservice.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@Import(RestClientAutoConfiguration.class)
@SuppressWarnings("PMD.UseUtilityClass")
public class MicroServiceApplication {
    public static void main(final String[] args) {
        SpringApplication.run(MicroServiceApplication.class, args);
    }
}
