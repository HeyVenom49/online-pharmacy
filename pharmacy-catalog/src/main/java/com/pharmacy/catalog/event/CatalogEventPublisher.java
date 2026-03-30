package com.pharmacy.catalog.event;

import com.pharmacy.common.config.RabbitMQConfig;
import com.pharmacy.common.events.PrescriptionApprovedEvent;
import com.pharmacy.common.events.PrescriptionRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPrescriptionApproved(PrescriptionApprovedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("PRESCRIPTION_APPROVED");
        event.setTimestamp(LocalDateTime.now());

        log.info("Publishing PrescriptionApprovedEvent: prescriptionId={}, userId={}", 
                event.getPrescriptionId(), event.getUserId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PRESCRIPTION_EXCHANGE,
                RabbitMQConfig.PRESCRIPTION_APPROVED_ROUTING,
                event
        );
    }

    public void publishPrescriptionRejected(PrescriptionRejectedEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("PRESCRIPTION_REJECTED");
        event.setTimestamp(LocalDateTime.now());

        log.info("Publishing PrescriptionRejectedEvent: prescriptionId={}, userId={}", 
                event.getPrescriptionId(), event.getUserId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PRESCRIPTION_EXCHANGE,
                RabbitMQConfig.PRESCRIPTION_REJECTED_ROUTING,
                event
        );
    }
}
