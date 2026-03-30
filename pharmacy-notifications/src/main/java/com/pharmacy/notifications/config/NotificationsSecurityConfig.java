package com.pharmacy.notifications.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class NotificationsSecurityConfig {

    /**
     * Spring Security’s CSRF protection stays enabled (default). Unsafe methods (POST, PUT, PATCH, DELETE)
     * require a valid CSRF token; safe methods (GET, HEAD, OPTIONS) do not. Permitted paths are actuator
     * read-only endpoints; use GET only for those. All other requests are denied, so there is no browser form
     * flow on this service—state is {@linkplain org.springframework.security.config.http.SessionCreationPolicy#STATELESS stateless}.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                        .anyRequest().denyAll());
        return http.build();
    }
}
