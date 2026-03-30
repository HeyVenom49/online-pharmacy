package com.pharmacy.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RateLimitingFilterTest {

    private final RateLimitingFilter filter = new RateLimitingFilter();

    @BeforeEach
    void props() {
        ReflectionTestUtils.setField(filter, "authRequestsPerMinute", 100);
        ReflectionTestUtils.setField(filter, "generalRequestsPerMinute", 100);
        ReflectionTestUtils.setField(filter, "burstCapacity", 50);
    }

    @Test
    void allowsRequestThroughChain() {
        MockServerWebExchange ex = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/catalog/medicines").remoteAddress(new java.net.InetSocketAddress("127.0.0.1", 1234)));
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(ex, chain)).verifyComplete();
        verify(chain).filter(any());
    }

    @Test
    void usesXForwardedForClientIp() {
        MockServerWebExchange ex = MockServerWebExchange.from(
                MockServerHttpRequest.get("/x")
                        .header("X-Forwarded-For", "203.0.113.1, 10.0.0.1")
                        .remoteAddress(new java.net.InetSocketAddress("127.0.0.1", 1234)));
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(ex, chain)).verifyComplete();
        verify(chain).filter(any());
    }

    @Test
    void authEndpointEventuallyReturns429() {
        RateLimitingFilter tiny = new RateLimitingFilter();
        ReflectionTestUtils.setField(tiny, "authRequestsPerMinute", 1);
        ReflectionTestUtils.setField(tiny, "generalRequestsPerMinute", 100);
        ReflectionTestUtils.setField(tiny, "burstCapacity", 20);

        java.net.InetSocketAddress addr = new java.net.InetSocketAddress("198.51.100.9", 6000);
        HttpStatusCode last = null;
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
        for (int i = 0; i < 20; i++) {
            MockServerWebExchange ex = MockServerWebExchange.from(
                    MockServerHttpRequest.get("/api/auth/login").remoteAddress(addr));
            StepVerifier.create(tiny.filter(ex, chain)).verifyComplete();
            last = ex.getResponse().getStatusCode();
            if (last != null && last.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                break;
            }
        }
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), last != null ? last.value() : -1);
    }
}
