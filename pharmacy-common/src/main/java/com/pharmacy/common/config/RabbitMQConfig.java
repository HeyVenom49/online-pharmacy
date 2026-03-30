package com.pharmacy.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.host")
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "pharmacy.order.exchange";
    public static final String INVENTORY_EXCHANGE = "pharmacy.inventory.exchange";
    public static final String PRESCRIPTION_EXCHANGE = "pharmacy.prescription.exchange";
    public static final String USER_EXCHANGE = "pharmacy.user.exchange";

    /**
     * Catalog and the notification service must not share one queue: load-balancing would drop events.
     * Same routing key → separate queues so catalog (inventory) and pharmacy-notifications (email + in-app dispatch) both receive each message.
     */
    public static final String ORDER_PLACED_QUEUE_CATALOG = "pharmacy.order.placed.queue.catalog";
    public static final String ORDER_PLACED_QUEUE_NOTIFICATIONS = "pharmacy.order.placed.queue.notifications";
    public static final String ORDER_CANCELLED_QUEUE_CATALOG = "pharmacy.order.cancelled.queue.catalog";
    public static final String ORDER_CANCELLED_QUEUE_NOTIFICATIONS = "pharmacy.order.cancelled.queue.notifications";
    public static final String INVENTORY_RESERVED_QUEUE = "pharmacy.inventory.reserved.queue";
    public static final String INVENTORY_RELEASED_QUEUE = "pharmacy.inventory.released.queue";
    public static final String PRESCRIPTION_APPROVED_QUEUE_CATALOG = "pharmacy.prescription.approved.queue.catalog";
    public static final String PRESCRIPTION_APPROVED_QUEUE_NOTIFICATIONS = "pharmacy.prescription.approved.queue.notifications";
    public static final String PRESCRIPTION_REJECTED_QUEUE_CATALOG = "pharmacy.prescription.rejected.queue.catalog";
    public static final String PRESCRIPTION_REJECTED_QUEUE_NOTIFICATIONS = "pharmacy.prescription.rejected.queue.notifications";
    public static final String USER_REGISTERED_QUEUE = "pharmacy.user.registered.queue";
    public static final String USER_LOGGED_IN_QUEUE_NOTIFICATIONS = "pharmacy.user.logged.in.queue.notifications";

    public static final String ORDER_PLACED_ROUTING = "order.placed";
    public static final String ORDER_CANCELLED_ROUTING = "order.cancelled";
    public static final String INVENTORY_RESERVED_ROUTING = "inventory.reserved";
    public static final String INVENTORY_RELEASED_ROUTING = "inventory.released";
    public static final String PRESCRIPTION_APPROVED_ROUTING = "prescription.approved";
    public static final String PRESCRIPTION_REJECTED_ROUTING = "prescription.rejected";
    public static final String USER_REGISTERED_ROUTING = "user.registered";
    public static final String USER_LOGGED_IN_ROUTING = "user.logged.in";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange(INVENTORY_EXCHANGE);
    }

    @Bean
    public TopicExchange prescriptionExchange() {
        return new TopicExchange(PRESCRIPTION_EXCHANGE);
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    public Queue orderPlacedQueueCatalog() {
        return QueueBuilder.durable(ORDER_PLACED_QUEUE_CATALOG).build();
    }

    @Bean
    public Queue orderPlacedQueueNotifications() {
        return QueueBuilder.durable(ORDER_PLACED_QUEUE_NOTIFICATIONS).build();
    }

    @Bean
    public Queue orderCancelledQueueCatalog() {
        return QueueBuilder.durable(ORDER_CANCELLED_QUEUE_CATALOG).build();
    }

    @Bean
    public Queue orderCancelledQueueNotifications() {
        return QueueBuilder.durable(ORDER_CANCELLED_QUEUE_NOTIFICATIONS).build();
    }

    @Bean
    public Queue inventoryReservedQueue() {
        return QueueBuilder.durable(INVENTORY_RESERVED_QUEUE).build();
    }

    @Bean
    public Queue inventoryReleasedQueue() {
        return QueueBuilder.durable(INVENTORY_RELEASED_QUEUE).build();
    }

    @Bean
    public Queue prescriptionApprovedQueueCatalog() {
        return QueueBuilder.durable(PRESCRIPTION_APPROVED_QUEUE_CATALOG).build();
    }

    @Bean
    public Queue prescriptionApprovedQueueNotifications() {
        return QueueBuilder.durable(PRESCRIPTION_APPROVED_QUEUE_NOTIFICATIONS).build();
    }

    @Bean
    public Queue prescriptionRejectedQueueCatalog() {
        return QueueBuilder.durable(PRESCRIPTION_REJECTED_QUEUE_CATALOG).build();
    }

    @Bean
    public Queue prescriptionRejectedQueueNotifications() {
        return QueueBuilder.durable(PRESCRIPTION_REJECTED_QUEUE_NOTIFICATIONS).build();
    }

    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(USER_REGISTERED_QUEUE).build();
    }

    @Bean
    public Queue userLoggedInQueueNotifications() {
        return QueueBuilder.durable(USER_LOGGED_IN_QUEUE_NOTIFICATIONS).build();
    }

    @Bean
    public Binding orderPlacedBindingCatalog(Queue orderPlacedQueueCatalog, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderPlacedQueueCatalog).to(orderExchange).with(ORDER_PLACED_ROUTING);
    }

    @Bean
    public Binding orderPlacedBindingNotifications(Queue orderPlacedQueueNotifications, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderPlacedQueueNotifications).to(orderExchange).with(ORDER_PLACED_ROUTING);
    }

    @Bean
    public Binding orderCancelledBindingCatalog(Queue orderCancelledQueueCatalog, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderCancelledQueueCatalog).to(orderExchange).with(ORDER_CANCELLED_ROUTING);
    }

    @Bean
    public Binding orderCancelledBindingNotifications(Queue orderCancelledQueueNotifications, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderCancelledQueueNotifications).to(orderExchange).with(ORDER_CANCELLED_ROUTING);
    }

    @Bean
    public Binding inventoryReservedBinding(Queue inventoryReservedQueue, TopicExchange inventoryExchange) {
        return BindingBuilder.bind(inventoryReservedQueue).to(inventoryExchange).with(INVENTORY_RESERVED_ROUTING);
    }

    @Bean
    public Binding inventoryReleasedBinding(Queue inventoryReleasedQueue, TopicExchange inventoryExchange) {
        return BindingBuilder.bind(inventoryReleasedQueue).to(inventoryExchange).with(INVENTORY_RELEASED_ROUTING);
    }

    @Bean
    public Binding prescriptionApprovedBindingCatalog(Queue prescriptionApprovedQueueCatalog,
            TopicExchange prescriptionExchange) {
        return BindingBuilder.bind(prescriptionApprovedQueueCatalog).to(prescriptionExchange)
                .with(PRESCRIPTION_APPROVED_ROUTING);
    }

    @Bean
    public Binding prescriptionApprovedBindingNotifications(Queue prescriptionApprovedQueueNotifications,
            TopicExchange prescriptionExchange) {
        return BindingBuilder.bind(prescriptionApprovedQueueNotifications).to(prescriptionExchange)
                .with(PRESCRIPTION_APPROVED_ROUTING);
    }

    @Bean
    public Binding prescriptionRejectedBindingCatalog(Queue prescriptionRejectedQueueCatalog,
            TopicExchange prescriptionExchange) {
        return BindingBuilder.bind(prescriptionRejectedQueueCatalog).to(prescriptionExchange)
                .with(PRESCRIPTION_REJECTED_ROUTING);
    }

    @Bean
    public Binding prescriptionRejectedBindingNotifications(Queue prescriptionRejectedQueueNotifications,
            TopicExchange prescriptionExchange) {
        return BindingBuilder.bind(prescriptionRejectedQueueNotifications).to(prescriptionExchange)
                .with(PRESCRIPTION_REJECTED_ROUTING);
    }

    @Bean
    public Binding userRegisteredBinding(Queue userRegisteredQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userRegisteredQueue).to(userExchange).with(USER_REGISTERED_ROUTING);
    }

    @Bean
    public Binding userLoggedInBindingNotifications(Queue userLoggedInQueueNotifications,
            TopicExchange userExchange) {
        return BindingBuilder.bind(userLoggedInQueueNotifications).to(userExchange).with(USER_LOGGED_IN_ROUTING);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
