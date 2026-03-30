package com.pharmacy.gateway.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitingFilter implements WebFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${rate-limit.auth.requests-per-minute:5}")
    private int authRequestsPerMinute;

    @Value("${rate-limit.general.requests-per-minute:100}")
    private int generalRequestsPerMinute;

    @Value("${rate-limit.burst-capacity:20}")
    private int burstCapacity;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String clientIp = getClientIp(exchange);
        String bucketKey = clientIp + ":" + getEndpointType(path);

        Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> createBucket(path));

        if (bucket.tryConsume(1)) {
            return chain.filter(exchange);
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().add("X-Rate-Limit-Retry-After-Seconds", "60");
            exchange.getResponse().getHeaders().add("Content-Type", "application/json");
            return exchange.getResponse().setComplete();
        }
    }

    private Bucket createBucket(String path) {
        if (isAuthEndpoint(path)) {
            Bandwidth limit = Bandwidth.classic(
                    authRequestsPerMinute,
                    Refill.greedy(authRequestsPerMinute, Duration.ofMinutes(1))
            );
            return Bucket.builder().addLimit(limit).build();
        } else {
            Bandwidth limit = Bandwidth.classic(
                    generalRequestsPerMinute,
                    Refill.greedy(generalRequestsPerMinute, Duration.ofMinutes(1))
            );
            Bandwidth burst = Bandwidth.classic(
                    burstCapacity,
                    Refill.greedy(burstCapacity, Duration.ofSeconds(10))
            );
            return Bucket.builder().addLimit(limit).addLimit(burst).build();
        }
    }

    private boolean isAuthEndpoint(String path) {
        return path.contains("/api/auth/login") || path.contains("/api/auth/signup")
                || path.endsWith("/auth/login") || path.endsWith("/auth/signup");
    }

    private String getEndpointType(String path) {
        if (isAuthEndpoint(path)) {
            return "auth";
        }
        return "general";
    }

    private String getClientIp(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
    }
}
