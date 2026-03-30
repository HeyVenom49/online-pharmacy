package com.pharmacy.common.config.actuator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("eureka")
public class EurekaHealthIndicator implements HealthIndicator {
    
    private static final Logger log = LoggerFactory.getLogger(EurekaHealthIndicator.class);

    @Value("${eureka.client.enabled:true}")
    private boolean eurekaEnabled;

    @Override
    public Health health() {
        if (!eurekaEnabled) {
            return Health.unknown()
                    .withDetail("message", "Eureka client is disabled")
                    .build();
        }

        try {
            return Health.up()
                    .withDetail("message", "Eureka client is enabled")
                    .build();
        } catch (Exception e) {
            log.warn("Eureka health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
