package com.pharmacy.gateway.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "12345678901234567890123456789012";

    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter();

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(filter, "secret", SECRET);
        filter.init();
    }

    @Test
    void publicPathPassesWithoutToken() {
        MockServerWebExchange ex = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health"));
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(ex, chain)).verifyComplete();
        verify(chain).filter(any());
    }

    @Test
    void authLoginWithoutApiPrefixPassesWithoutToken() {
        MockServerWebExchange ex = MockServerWebExchange.from(
                MockServerHttpRequest.post("/auth/login"));
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(ex, chain)).verifyComplete();
        verify(chain).filter(any());
    }

    @Test
    void missingTokenReturns401() {
        MockServerWebExchange ex = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders"));
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        StepVerifier.create(filter.filter(ex, chain)).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), ex.getResponse().getStatusCode().value());
    }

    @Test
    void validTokenAddsHeaders() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("user@test.com")
                .claim("userId", 7L)
                .claim("role", "USER")
                .expiration(new Date(System.currentTimeMillis() + 120_000))
                .signWith(key)
                .compact();

        MockServerWebExchange ex = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders").header("Authorization", "Bearer " + token));
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
        StepVerifier.create(filter.filter(ex, chain)).verifyComplete();
        verify(chain).filter(any());
    }

    @Test
    void getOrderIsMinus100() {
        assertEquals(-100, filter.getOrder());
    }
}
