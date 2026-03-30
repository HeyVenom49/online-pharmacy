package com.pharmacy.common.config.actuator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("serviceDependencies")
public class ServiceDependenciesHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(ServiceDependenciesHealthIndicator.class);
    private final Map<String, HealthStatus> serviceHealth = new ConcurrentHashMap<>();

    @Override
    public Health health() {
        boolean allHealthy = serviceHealth.values().stream()
                .allMatch(HealthStatus::isHealthy);

        Map<String, Object> details = new ConcurrentHashMap<>();
        serviceHealth.forEach((service, status) -> 
                details.put(service, status.isHealthy() ? "UP" : "DOWN"));

        if (allHealthy) {
            return Health.up()
                    .withDetail("services", details)
                    .build();
        } else {
            return Health.down()
                    .withDetail("services", details)
                    .build();
        }
    }

    public void updateServiceHealth(String serviceName, boolean isHealthy) {
        serviceHealth.put(serviceName, new HealthStatus(isHealthy));
        log.debug("Updated health status for {}: {}", serviceName, isHealthy ? "UP" : "DOWN");
    }

    public void updateServiceHealth(String serviceName, boolean isHealthy, String message) {
        serviceHealth.put(serviceName, new HealthStatus(isHealthy, message));
        log.debug("Updated health status for {}: {} - {}", serviceName, isHealthy ? "UP" : "DOWN", message);
    }

    private static class HealthStatus {
        private final boolean healthy;
        private final String message;

        public HealthStatus(boolean healthy) {
            this(healthy, null);
        }

        public HealthStatus(boolean healthy, String message) {
            this.healthy = healthy;
            this.message = message;
        }

        public boolean isHealthy() {
            return healthy;
        }

        public String getMessage() {
            return message;
        }
    }
}
