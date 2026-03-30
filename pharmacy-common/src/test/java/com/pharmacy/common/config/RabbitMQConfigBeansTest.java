package com.pharmacy.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Exercises {@link RabbitMQConfig} bean methods without a live broker (pure Java wiring).
 */
class RabbitMQConfigBeansTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    void exchangesAndQueuesMatchNaming() {
        assertEquals(RabbitMQConfig.ORDER_EXCHANGE, config.orderExchange().getName());
        assertEquals(RabbitMQConfig.INVENTORY_EXCHANGE, config.inventoryExchange().getName());
        assertEquals(RabbitMQConfig.PRESCRIPTION_EXCHANGE, config.prescriptionExchange().getName());
        assertEquals(RabbitMQConfig.USER_EXCHANGE, config.userExchange().getName());

        assertEquals(RabbitMQConfig.ORDER_PLACED_QUEUE_CATALOG, config.orderPlacedQueueCatalog().getName());
        assertEquals(RabbitMQConfig.ORDER_PLACED_QUEUE_NOTIFICATIONS, config.orderPlacedQueueNotifications().getName());
        assertEquals(RabbitMQConfig.ORDER_CANCELLED_QUEUE_CATALOG, config.orderCancelledQueueCatalog().getName());
        assertEquals(RabbitMQConfig.ORDER_CANCELLED_QUEUE_NOTIFICATIONS, config.orderCancelledQueueNotifications().getName());
        assertEquals(RabbitMQConfig.INVENTORY_RESERVED_QUEUE, config.inventoryReservedQueue().getName());
        assertEquals(RabbitMQConfig.INVENTORY_RELEASED_QUEUE, config.inventoryReleasedQueue().getName());
        assertEquals(RabbitMQConfig.PRESCRIPTION_APPROVED_QUEUE_CATALOG, config.prescriptionApprovedQueueCatalog().getName());
        assertEquals(RabbitMQConfig.PRESCRIPTION_APPROVED_QUEUE_NOTIFICATIONS, config.prescriptionApprovedQueueNotifications().getName());
        assertEquals(RabbitMQConfig.PRESCRIPTION_REJECTED_QUEUE_CATALOG, config.prescriptionRejectedQueueCatalog().getName());
        assertEquals(RabbitMQConfig.PRESCRIPTION_REJECTED_QUEUE_NOTIFICATIONS, config.prescriptionRejectedQueueNotifications().getName());
        assertEquals(RabbitMQConfig.USER_REGISTERED_QUEUE, config.userRegisteredQueue().getName());
        assertEquals(RabbitMQConfig.USER_LOGGED_IN_QUEUE_NOTIFICATIONS, config.userLoggedInQueueNotifications().getName());
    }

    @Test
    void bindingsUseExpectedRoutingKeys() {
        TopicExchange order = config.orderExchange();
        Queue qCatalog = config.orderPlacedQueueCatalog();
        Binding bCatalog = config.orderPlacedBindingCatalog(qCatalog, order);
        assertEquals(RabbitMQConfig.ORDER_PLACED_ROUTING, bCatalog.getRoutingKey());

        TopicExchange inv = config.inventoryExchange();
        Binding invB = config.inventoryReservedBinding(config.inventoryReservedQueue(), inv);
        assertEquals(RabbitMQConfig.INVENTORY_RESERVED_ROUTING, invB.getRoutingKey());

        TopicExchange pres = config.prescriptionExchange();
        Binding presB = config.prescriptionApprovedBindingCatalog(
                config.prescriptionApprovedQueueCatalog(), pres);
        assertEquals(RabbitMQConfig.PRESCRIPTION_APPROVED_ROUTING, presB.getRoutingKey());

        TopicExchange user = config.userExchange();
        Binding userB = config.userRegisteredBinding(config.userRegisteredQueue(), user);
        assertEquals(RabbitMQConfig.USER_REGISTERED_ROUTING, userB.getRoutingKey());

        Binding userLoginB = config.userLoggedInBindingNotifications(config.userLoggedInQueueNotifications(), user);
        assertEquals(RabbitMQConfig.USER_LOGGED_IN_ROUTING, userLoginB.getRoutingKey());
    }

    @Test
    void jsonMessageConverterAndRabbitTemplate() {
        MessageConverter mc = config.jsonMessageConverter();
        assertNotNull(mc);

        ConnectionFactory cf = mock(ConnectionFactory.class);
        RabbitTemplate rt = config.rabbitTemplate(cf);
        assertNotNull(rt);
        assertInstanceOf(Jackson2JsonMessageConverter.class, rt.getMessageConverter());
    }
}
