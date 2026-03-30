package com.pharmacy.identity.config;

import com.pharmacy.identity.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Smoke-test that {@link SecurityConfig} registers a {@link SecurityFilterChain} and crypto beans.
 */
class SecurityConfigContextTest {

    @Test
    void securityBeansLoad() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(SecurityAutoConfiguration.class))
                .withBean(JwtAuthenticationFilter.class, () -> mock(JwtAuthenticationFilter.class))
                .withUserConfiguration(SecurityConfig.class)
                .run(ctx -> assertThat(ctx)
                        .hasSingleBean(SecurityFilterChain.class)
                        .hasBean("passwordEncoder")
                        .hasBean("authenticationManager"));
    }
}
