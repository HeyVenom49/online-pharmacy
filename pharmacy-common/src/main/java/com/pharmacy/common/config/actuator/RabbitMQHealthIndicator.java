package com.pharmacy.common.config.actuator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component("rabbitMQ")
@ConditionalOnBean(RabbitTemplate.class)
public class RabbitMQHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQHealthIndicator.class);
    private final RabbitTemplate rabbitTemplate;
    
    public RabbitMQHealthIndicator(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public Health health() {
        try {
            rabbitTemplate.execute(channel -> {
                return null;
            });
            return Health.up()
                    .withDetail("message", "RabbitMQ connection is healthy")
                    .build();
        } catch (Exception e) {
            log.warn("RabbitMQ health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
