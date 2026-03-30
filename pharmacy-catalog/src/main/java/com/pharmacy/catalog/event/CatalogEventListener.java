package com.pharmacy.catalog.event;

import com.pharmacy.catalog.service.InventoryService;
import com.pharmacy.common.config.RabbitMQConfig;
import com.pharmacy.common.events.*;
import com.pharmacy.common.outbox.ProcessedEvent;
import com.pharmacy.common.outbox.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CatalogEventListener {

    private static final String CONSUMER_NAME = "catalog-service";

    private final InventoryService inventoryService;
    private final ProcessedEventRepository processedEventRepository;

    @RabbitListener(queues = RabbitMQConfig.ORDER_PLACED_QUEUE_CATALOG)
    @Transactional
    public void handleOrderPlaced(OrderPlacedEvent event) {
        if (isAlreadyProcessed(event.getEventId())) {
            log.info("OrderPlacedEvent already processed: eventId={}", event.getEventId());
            return;
        }

        log.info("Received OrderPlacedEvent: orderId={}, userId={}", event.getOrderId(), event.getUserId());
        try {
            for (OrderPlacedEvent.OrderItemEvent item : event.getItems()) {
                log.debug("Processing order item: medicineId={}, quantity={}", 
                        item.getMedicineId(), item.getQuantity());
                boolean success = inventoryService.deductStock(item.getMedicineId(), item.getQuantity());
                if (success) {
                    log.info("Successfully deducted stock for orderId={}, medicineId={}, quantity={}", 
                            event.getOrderId(), item.getMedicineId(), item.getQuantity());
                } else {
                    log.error("Failed to deduct stock for orderId={}, medicineId={}, quantity={}", 
                            event.getOrderId(), item.getMedicineId(), item.getQuantity());
                }
            }
            markAsProcessed(event.getEventId());
        } catch (Exception e) {
            log.error("Error processing OrderPlacedEvent: {}", e.getMessage(), e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCELLED_QUEUE_CATALOG)
    @Transactional
    public void handleOrderCancelled(OrderCancelledEvent event) {
        if (isAlreadyProcessed(event.getEventId())) {
            log.info("OrderCancelledEvent already processed: eventId={}", event.getEventId());
            return;
        }

        log.info("Received OrderCancelledEvent: orderId={}, userId={}", event.getOrderId(), event.getUserId());
        try {
            log.debug("Order cancellation reason: {}", event.getReason());
            if (event.getItems() != null) {
                for (OrderCancelledEvent.CancelledItem item : event.getItems()) {
                    inventoryService.releaseStock(item.getMedicineId(), item.getQuantity());
                    log.info("Released stock for cancelled orderId={}, medicineId={}, quantity={}", 
                            event.getOrderId(), item.getMedicineId(), item.getQuantity());
                }
            }
            markAsProcessed(event.getEventId());
        } catch (Exception e) {
            log.error("Error processing OrderCancelledEvent: {}", e.getMessage(), e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.INVENTORY_RESERVED_QUEUE)
    @Transactional
    public void handleInventoryReserved(InventoryReservedEvent event) {
        if (isAlreadyProcessed(event.getEventId())) {
            log.info("InventoryReservedEvent already processed: eventId={}", event.getEventId());
            return;
        }

        log.info("Received InventoryReservedEvent: orderId={}, reservationId={}", 
                event.getOrderId(), event.getReservationId());
        try {
            if (event.getReservations() != null) {
                for (InventoryReservedEvent.ReservationItem item : event.getReservations()) {
                    boolean success = inventoryService.reserveStock(
                            item.getMedicineId(), item.getQuantity(), event.getReservationId());
                    if (success) {
                        log.info("Successfully reserved stock: medicineId={}, quantity={}, reservationId={}", 
                                item.getMedicineId(), item.getQuantity(), event.getReservationId());
                    } else {
                        log.error("Failed to reserve stock: medicineId={}, quantity={}, reservationId={}", 
                                item.getMedicineId(), item.getQuantity(), event.getReservationId());
                    }
                }
            }
            markAsProcessed(event.getEventId());
        } catch (Exception e) {
            log.error("Error processing InventoryReservedEvent: {}", e.getMessage(), e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.INVENTORY_RELEASED_QUEUE)
    @Transactional
    public void handleInventoryReleased(InventoryReleasedEvent event) {
        if (isAlreadyProcessed(event.getEventId())) {
            log.info("InventoryReleasedEvent already processed: eventId={}", event.getEventId());
            return;
        }

        log.info("Received InventoryReleasedEvent: orderId={}, reservationId={}", 
                event.getOrderId(), event.getReservationId());
        try {
            if (event.getReleases() != null) {
                for (InventoryReleasedEvent.ReleaseItem item : event.getReleases()) {
                    inventoryService.releaseStock(item.getMedicineId(), item.getQuantity());
                    log.info("Released reserved stock: medicineId={}, quantity={}, reservationId={}", 
                            item.getMedicineId(), item.getQuantity(), event.getReservationId());
                }
            }
            markAsProcessed(event.getEventId());
        } catch (Exception e) {
            log.error("Error processing InventoryReleasedEvent: {}", e.getMessage(), e);
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.PRESCRIPTION_APPROVED_QUEUE_CATALOG)
    @Transactional
    public void handlePrescriptionApproved(PrescriptionApprovedEvent event) {
        if (isAlreadyProcessed(event.getEventId())) {
            log.info("PrescriptionApprovedEvent already processed: eventId={}", event.getEventId());
            return;
        }

        log.info("Received PrescriptionApprovedEvent: prescriptionId={}, userId={}, medicineName={}", 
                event.getPrescriptionId(), event.getUserId(), event.getMedicineName());
        try {
            log.debug("Prescription approved - user can now purchase medicine");
            markAsProcessed(event.getEventId());
        } catch (Exception e) {
            log.error("Error processing PrescriptionApprovedEvent: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.PRESCRIPTION_REJECTED_QUEUE_CATALOG)
    @Transactional
    public void handlePrescriptionRejected(PrescriptionRejectedEvent event) {
        if (isAlreadyProcessed(event.getEventId())) {
            log.info("PrescriptionRejectedEvent already processed: eventId={}", event.getEventId());
            return;
        }

        log.info("Received PrescriptionRejectedEvent: prescriptionId={}, userId={}, reason={}", 
                event.getPrescriptionId(), event.getUserId(), event.getReason());
        try {
            log.debug("Prescription rejected - notifying user");
            markAsProcessed(event.getEventId());
        } catch (Exception e) {
            log.error("Error processing PrescriptionRejectedEvent: {}", e.getMessage(), e);
        }
    }

    private boolean isAlreadyProcessed(String eventId) {
        return processedEventRepository.existsByConsumerAndEventId(CONSUMER_NAME, eventId);
    }

    private void markAsProcessed(String eventId) {
        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .consumer(CONSUMER_NAME)
                .eventId(eventId)
                .build();
        processedEventRepository.save(processedEvent);
        log.debug("Marked event as processed: eventId={}, consumer={}", eventId, CONSUMER_NAME);
    }
}
