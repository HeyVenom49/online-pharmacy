package com.pharmacy.common.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class Resilience4jConfig {
    
    private static final Logger log = LoggerFactory.getLogger(Resilience4jConfig.class);

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public Resilience4jConfig(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @PostConstruct
    public void initCircuitBreakerEventListeners() {
        registerCircuitBreakerEventListener("catalogService");
        registerCircuitBreakerEventListener("ordersService");
    }

    private void registerCircuitBreakerEventListener(String circuitBreakerName) {
        try {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(circuitBreakerName);
            
            circuitBreaker.getEventPublisher()
                    .onStateTransition(event -> {
                        log.info("CircuitBreaker [{}] state changed: {} -> {}", 
                                circuitBreakerName,
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState());
                    })
                    .onError(event -> {
                        log.warn("CircuitBreaker [{}] recorded error: {}", 
                                circuitBreakerName, event.getThrowable().getMessage());
                    })
                    .onSuccess(event -> {
                        log.debug("CircuitBreaker [{}] recorded success, duration: {}ms", 
                                circuitBreakerName, event.getElapsedDuration().toMillis());
                    })
                    .onCallNotPermitted(event -> {
                        log.warn("CircuitBreaker [{}] rejected call - circuit is OPEN", circuitBreakerName);
                    });
        } catch (Exception e) {
            log.debug("CircuitBreaker [{}] not found in registry (may not be configured)", circuitBreakerName);
        }
    }
}
