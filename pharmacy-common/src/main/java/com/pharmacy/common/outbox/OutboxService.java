package com.pharmacy.common.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public <T> OutboxEvent saveEvent(String aggregateType, String aggregateId, String eventType, T eventPayload) {
        String eventId = UUID.randomUUID().toString();
        String payload;

        try {
            payload = objectMapper.writeValueAsString(eventPayload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event payload for aggregateType={}, aggregateId={}: {}", 
                    aggregateType, aggregateId, e.getMessage());
            throw new RuntimeException("Failed to serialize event payload", e);
        }

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .eventId(eventId)
                .payload(payload)
                .status(OutboxEvent.OutboxStatus.PENDING)
                .build();

        OutboxEvent saved = outboxEventRepository.save(outboxEvent);
        log.debug("Saved outbox event: eventId={}, aggregateType={}, aggregateId={}, eventType={}", 
                eventId, aggregateType, aggregateId, eventType);

        return saved;
    }

    @Transactional
    public void markCompleted(Long eventId) {
        outboxEventRepository.findById(eventId).ifPresent(event -> {
            event.markCompleted();
            outboxEventRepository.save(event);
            log.debug("Marked outbox event as completed: eventId={}", event.getEventId());
        });
    }

    @Transactional
    public void markFailed(Long eventId, String error) {
        outboxEventRepository.findById(eventId).ifPresent(event -> {
            event.markFailed(error);
            outboxEventRepository.save(event);
            log.warn("Marked outbox event as failed: eventId={}, error={}, retryCount={}", 
                    event.getEventId(), error, event.getRetryCount());
        });
    }

    @Transactional
    public void cleanupOldEvents(int daysToKeep) {
        LocalDateTime before = LocalDateTime.now().minusDays(daysToKeep);
        int deleted = outboxEventRepository.deleteOldCompletedEvents(before);
        log.info("Cleaned up {} old completed outbox events older than {} days", deleted, daysToKeep);
    }
}
