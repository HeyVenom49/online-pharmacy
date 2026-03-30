package com.pharmacy.orders.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.common.config.RabbitMQConfig;
import com.pharmacy.common.events.*;
import com.pharmacy.common.outbox.OutboxEvent;
import com.pharmacy.common.outbox.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRIES = 5;
    private static final int BATCH_SIZE = 100;

    @Scheduled(fixedDelayString = "${outbox.poll-interval:1000}")
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEvents(MAX_RETRIES);
        
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Processing {} pending outbox events", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                processEvent(event);
            } catch (Exception e) {
                log.error("Failed to process outbox event: eventId={}, error={}", 
                        event.getEventId(), e.getMessage(), e);
                event.markFailed(e.getMessage());
                outboxEventRepository.save(event);
            }
        }
    }

    private void processEvent(OutboxEvent event) {
        event.markProcessing();
        outboxEventRepository.save(event);

        String exchange = getExchange(event.getEventType());
        String routingKey = getRoutingKey(event.getEventType());

        if (exchange == null || routingKey == null) {
            log.warn("Unknown event type: {}. Marking as completed.", event.getEventType());
            event.markCompleted();
            outboxEventRepository.save(event);
            return;
        }

        rabbitTemplate.convertAndSend(exchange, routingKey, event.getPayload());
        log.info("Published outbox event: eventId={}, eventType={}, aggregateId={}", 
                event.getEventId(), event.getEventType(), event.getAggregateId());

        event.markCompleted();
        outboxEventRepository.save(event);
    }

    private String getExchange(String eventType) {
        return switch (eventType) {
            case "ORDER_PLACED", "ORDER_CANCELLED" -> RabbitMQConfig.ORDER_EXCHANGE;
            case "INVENTORY_RESERVED", "INVENTORY_RELEASED" -> RabbitMQConfig.INVENTORY_EXCHANGE;
            case "PRESCRIPTION_APPROVED", "PRESCRIPTION_REJECTED" -> RabbitMQConfig.PRESCRIPTION_EXCHANGE;
            case "USER_REGISTERED" -> RabbitMQConfig.USER_EXCHANGE;
            default -> null;
        };
    }

    private String getRoutingKey(String eventType) {
        return switch (eventType) {
            case "ORDER_PLACED" -> RabbitMQConfig.ORDER_PLACED_ROUTING;
            case "ORDER_CANCELLED" -> RabbitMQConfig.ORDER_CANCELLED_ROUTING;
            case "INVENTORY_RESERVED" -> RabbitMQConfig.INVENTORY_RESERVED_ROUTING;
            case "INVENTORY_RELEASED" -> RabbitMQConfig.INVENTORY_RELEASED_ROUTING;
            case "PRESCRIPTION_APPROVED" -> RabbitMQConfig.PRESCRIPTION_APPROVED_ROUTING;
            case "PRESCRIPTION_REJECTED" -> RabbitMQConfig.PRESCRIPTION_REJECTED_ROUTING;
            case "USER_REGISTERED" -> RabbitMQConfig.USER_REGISTERED_ROUTING;
            default -> null;
        };
    }

    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void resetStaleEvents() {
        var fiveMinutesAgo = java.time.LocalDateTime.now().minusMinutes(5);
        int reset = outboxEventRepository.resetStaleProcessingEvents(fiveMinutesAgo);
        if (reset > 0) {
            log.warn("Reset {} stale PROCESSING events", reset);
        }
    }
}
