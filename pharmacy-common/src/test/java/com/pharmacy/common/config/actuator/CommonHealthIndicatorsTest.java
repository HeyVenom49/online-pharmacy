package com.pharmacy.common.config.actuator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;
import org.springframework.test.util.ReflectionTestUtils;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CommonHealthIndicatorsTest {

    @Test
    void eurekaHealth_whenDisabled() {
        EurekaHealthIndicator indicator = new EurekaHealthIndicator();
        ReflectionTestUtils.setField(indicator, "eurekaEnabled", false);
        assertEquals(Status.UNKNOWN, indicator.health().getStatus());
    }

    @Test
    void eurekaHealth_whenEnabled() {
        EurekaHealthIndicator indicator = new EurekaHealthIndicator();
        ReflectionTestUtils.setField(indicator, "eurekaEnabled", true);
        assertEquals(Status.UP, indicator.health().getStatus());
    }

    @Test
    void rabbitMqHealth_whenTemplateWorks() {
        org.springframework.amqp.rabbit.core.RabbitTemplate template = mock(org.springframework.amqp.rabbit.core.RabbitTemplate.class);
        when(template.execute(any())).thenReturn(null);
        RabbitMQHealthIndicator indicator = new RabbitMQHealthIndicator(template);
        assertEquals(Status.UP, indicator.health().getStatus());
    }

    @Test
    void rabbitMqHealth_whenTemplateFails() {
        org.springframework.amqp.rabbit.core.RabbitTemplate template = mock(org.springframework.amqp.rabbit.core.RabbitTemplate.class);
        when(template.execute(any())).thenThrow(new RuntimeException("amqp down"));
        RabbitMQHealthIndicator indicator = new RabbitMQHealthIndicator(template);
        assertEquals(Status.DOWN, indicator.health().getStatus());
    }

    @Test
    void serviceDependenciesHealthIndicator() {
        ServiceDependenciesHealthIndicator h = new ServiceDependenciesHealthIndicator();
        h.updateServiceHealth("a", true);
        assertEquals(Status.UP, h.health().getStatus());
        h.updateServiceHealth("b", false, "x");
        assertEquals(Status.DOWN, h.health().getStatus());
    }

    @Test
    void redisHealth_upAndDown() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        RedisConnection conn = mock(RedisConnection.class);
        when(factory.getConnection()).thenReturn(conn);
        when(conn.ping()).thenReturn("PONG");
        RedisHealthIndicator up = new RedisHealthIndicator(factory);
        assertEquals(Status.UP, up.health().getStatus());

        when(conn.ping()).thenThrow(new RuntimeException("redis down"));
        assertEquals(Status.DOWN, up.health().getStatus());
    }
}
