package com.pharmacy.orders.event;

import com.pharmacy.common.config.RabbitMQConfig;
import com.pharmacy.common.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderPlaced(OrderPlacedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("ORDER_PLACED");
        event.setTimestamp(LocalDateTime.now());
        
        log.info("Scheduling OrderPlacedEvent publish: orderId={}, userId={}", event.getOrderId(), event.getUserId());
        publishAfterCommit(() -> {
            log.info("Publishing OrderPlacedEvent: orderId={}, userId={}", event.getOrderId(), event.getUserId());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_PLACED_ROUTING,
                    event
            );
        });
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("ORDER_CANCELLED");
        event.setTimestamp(LocalDateTime.now());
        
        log.info("Scheduling OrderCancelledEvent publish: orderId={}, userId={}", event.getOrderId(), event.getUserId());
        publishAfterCommit(() -> {
            log.info("Publishing OrderCancelledEvent: orderId={}, userId={}", event.getOrderId(), event.getUserId());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_CANCELLED_ROUTING,
                    event
            );
        });
    }

    public void publishInventoryReserved(InventoryReservedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("INVENTORY_RESERVED");
        event.setTimestamp(LocalDateTime.now());
        
        log.info("Scheduling InventoryReservedEvent publish: orderId={}, items={}", 
                event.getOrderId(), 
                event.getReservations() != null ? event.getReservations().size() : 0);
        publishAfterCommit(() -> {
            log.info("Publishing InventoryReservedEvent: orderId={}, items={}", 
                    event.getOrderId(), 
                    event.getReservations() != null ? event.getReservations().size() : 0);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INVENTORY_EXCHANGE,
                    RabbitMQConfig.INVENTORY_RESERVED_ROUTING,
                    event
            );
        });
    }

    public void publishInventoryReleased(InventoryReleasedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("INVENTORY_RELEASED");
        event.setTimestamp(LocalDateTime.now());
        
        log.info("Scheduling InventoryReleasedEvent publish: orderId={}, items={}", 
                event.getOrderId(), 
                event.getReleases() != null ? event.getReleases().size() : 0);
        publishAfterCommit(() -> {
            log.info("Publishing InventoryReleasedEvent: orderId={}, items={}", 
                    event.getOrderId(), 
                    event.getReleases() != null ? event.getReleases().size() : 0);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.INVENTORY_EXCHANGE,
                    RabbitMQConfig.INVENTORY_RELEASED_ROUTING,
                    event
            );
        });
    }

    private void publishAfterCommit(Runnable publishAction) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        publishAction.run();
                    } catch (Exception e) {
                        log.error("Failed to publish event after commit: {}", e.getMessage(), e);
                    }
                }
            });
        } else {
            try {
                publishAction.run();
            } catch (Exception e) {
                log.error("Failed to publish event: {}", e.getMessage(), e);
            }
        }
    }
}
