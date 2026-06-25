package de.muenchen.oss.refarch.backend.configuration.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for local development without an identity provider.
 * <p>
 * Active under the "no-security" profile (which the "local" profile pulls in, see
 * application.yml). It permits every request and disables CSRF so the frontend can call
 * the backend directly without an OAuth2 token. Never activate this profile in production.
 */
@Configuration
@EnableWebSecurity
@Profile("no-security")
@Slf4j
public class NoSecurityConfiguration {

    @Bean
    public SecurityFilterChain permitAllFilterChain(final HttpSecurity http) throws Exception {
        log.warn("Profile 'no-security' is active: all requests are permitted without authentication. Do not use this in production!");
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(requests -> requests.anyRequest().permitAll());
        return http.build();
    }

}
