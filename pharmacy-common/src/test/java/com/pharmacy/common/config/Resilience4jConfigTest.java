package com.pharmacy.common.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class Resilience4jConfigTest {

    @Test
    void initRegistersListenersWithoutError() {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
        var catalog = registry.circuitBreaker("catalogService");
        var orders = registry.circuitBreaker("ordersService");
        Resilience4jConfig config = new Resilience4jConfig(registry);
        config.initCircuitBreakerEventListeners();
        assertNotNull(catalog.getEventPublisher());
        assertNotNull(orders.getEventPublisher());
    }
}
