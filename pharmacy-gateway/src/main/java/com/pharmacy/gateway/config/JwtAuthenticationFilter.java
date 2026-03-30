package com.pharmacy.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secret;

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("JwtAuthenticationFilter initialized with secret length: {}",
                secret != null ? secret.length() : "null");
    }

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/signup",
            // Identity uses /auth/login; callers often omit /api/ — must be public or JWT filter blocks before route
            "/auth/login",
            "/auth/signup",
            "/api/catalog/medicines",
            "/api/catalog/categories",
            "/api/catalog/inventory/medicine/",
            "/api/catalog/inventory/stock/",
            "/actuator/health",
            "/actuator/info",
            "/actuator/prometheus",
            // Gateway proxies per-service OpenAPI under /docs/*
            "/docs",
            "/swagger-ui",
            "/api-docs",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/webjars"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isPublicPath(path)) {
            log.debug("Public path accessed: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        log.debug("Processing request to {} with Authorization header present: {}", path, StringUtils.hasText(authHeader));

        if (!StringUtils.hasText(authHeader)) {
            log.warn("Missing Authorization header for path: {}", path);
            return unauthorized(exchange.getResponse(), "Missing Authorization header");
        }

        if (!authHeader.startsWith("Bearer ")) {
            log.warn("Invalid Authorization header format for path: {}. Expected 'Bearer ' prefix", path);
            return unauthorized(exchange.getResponse(), "Invalid Authorization header format");
        }

        String token = authHeader.substring(7);

        try {
            if (validateToken(token)) {
                Claims claims = getClaims(token);

                String userId = String.valueOf(claims.get("userId", Long.class));
                String email = claims.getSubject();
                String role = claims.get("role", String.class);

                log.info("JWT validated successfully for user: {} ({}) with role: {} on path: {}", email, userId, role, path);

                // Set Spring Security authentication context
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, Collections.singletonList(authority));
                SecurityContextImpl securityContext = new SecurityContextImpl(authentication);

                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .header("X-User-Email", email)
                        .header("X-User-Role", role)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build())
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
            } else {
                log.warn("Token validation returned false for path: {}", path);
            }
        } catch (JwtException e) {
            log.error("JWT validation failed for path: {} - JwtException: {}", path, e.getMessage());
            log.debug("JWT validation stack trace:", e);
        } catch (Exception e) {
            log.error("JWT validation failed for path: {} - Exception: {}", path, e.getMessage());
            log.debug("JWT validation stack trace:", e);
        }

        return unauthorized(exchange.getResponse(), "Invalid or expired token");
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            return !isTokenExpired(claims);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration() != null && claims.getExpiration().before(new java.util.Date());
    }

    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        String body = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\"}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
