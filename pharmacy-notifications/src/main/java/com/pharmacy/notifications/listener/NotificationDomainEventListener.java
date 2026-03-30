package com.pharmacy.notifications.listener;

import com.pharmacy.common.config.RabbitMQConfig;
import com.pharmacy.common.dto.notification.CreateNotificationDispatchRequest;
import com.pharmacy.common.events.*;
import com.pharmacy.notifications.service.NotificationDispatchCoordinator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDomainEventListener {

    private final NotificationDispatchCoordinator coordinator;

    @RabbitListener(queues = RabbitMQConfig.ORDER_PLACED_QUEUE_NOTIFICATIONS)
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("OrderPlacedEvent: orderId={}, userId={}", event.getOrderId(), event.getUserId());
        try {
            String items = event.getItems() != null
                    ? event.getItems().stream()
                            .map(i -> i.getMedicineName() + " x" + i.getQuantity())
                            .collect(Collectors.joining(", "))
                    : "N/A";
            String total = event.getGrandTotal() != null
                    ? "$" + event.getGrandTotal().toString()
                    : "N/A";
            String title = "Order Confirmed";
            String message = "Your order #" + event.getOrderId() + " has been confirmed. Total: " + total
                    + ". Items: " + items;
            CreateNotificationDispatchRequest req = CreateNotificationDispatchRequest.builder()
                    .userId(event.getUserId())
                    .type("ORDER_PLACED")
                    .title(title)
                    .message(message)
                    .referenceId(event.getOrderId().toString())
                    .build();
            coordinator.dispatchInAppAndEmail(req, null);
        } catch (Exception e) {
            log.error("Error handling OrderPlacedEvent: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCELLED_QUEUE_NOTIFICATIONS)
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("OrderCancelledEvent: orderId={}, userId={}", event.getOrderId(), event.getUserId());
        try {
            String title = "Order Cancelled";
            String message = "Your order #" + event.getOrderId() + " has been cancelled. Reason: " + event.getReason();
            CreateNotificationDispatchRequest req = CreateNotificationDispatchRequest.builder()
                    .userId(event.getUserId())
                    .type("ORDER_CANCELLED")
                    .title(title)
                    .message(message)
                    .referenceId(event.getOrderId().toString())
                    .build();
            coordinator.dispatchInAppAndEmail(req, null);
        } catch (Exception e) {
            log.error("Error handling OrderCancelledEvent: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.PRESCRIPTION_APPROVED_QUEUE_NOTIFICATIONS)
    public void onPrescriptionApproved(PrescriptionApprovedEvent event) {
        log.info("PrescriptionApprovedEvent: prescriptionId={}, userId={}", event.getPrescriptionId(), event.getUserId());
        try {
            String title = "Prescription Approved";
            String message = "Your prescription for \"" + event.getMedicineName() + "\" has been approved!";
            CreateNotificationDispatchRequest req = CreateNotificationDispatchRequest.builder()
                    .userId(event.getUserId())
                    .type("PRESCRIPTION_APPROVED")
                    .title(title)
                    .message(message)
                    .referenceId(event.getPrescriptionId().toString())
                    .build();
            coordinator.dispatchInAppAndEmail(req, null);
        } catch (Exception e) {
            log.error("Error handling PrescriptionApprovedEvent: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.PRESCRIPTION_REJECTED_QUEUE_NOTIFICATIONS)
    public void onPrescriptionRejected(PrescriptionRejectedEvent event) {
        log.info("PrescriptionRejectedEvent: prescriptionId={}, userId={}", event.getPrescriptionId(), event.getUserId());
        try {
            String title = "Prescription Rejected";
            String message = "Your prescription #" + event.getPrescriptionId() + " has been rejected. Reason: "
                    + event.getReason();
            CreateNotificationDispatchRequest req = CreateNotificationDispatchRequest.builder()
                    .userId(event.getUserId())
                    .type("PRESCRIPTION_REJECTED")
                    .title(title)
                    .message(message)
                    .referenceId(event.getPrescriptionId().toString())
                    .build();
            coordinator.dispatchInAppAndEmail(req, null);
        } catch (Exception e) {
            log.error("Error handling PrescriptionRejectedEvent: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.USER_LOGGED_IN_QUEUE_NOTIFICATIONS)
    public void onUserLoggedIn(UserLoggedInEvent event) {
        log.info("UserLoggedInEvent: userId={}, email={}", event.getUserId(), event.getEmail());
        try {
            String name = event.getFirstName() != null ? event.getFirstName() : "there";
            String title = "Successful sign-in";
            String message = "Hi " + name + ", you just signed in to Online Pharmacy. "
                    + "If this wasn't you, please change your password immediately.";
            CreateNotificationDispatchRequest req = CreateNotificationDispatchRequest.builder()
                    .userId(event.getUserId())
                    .type("USER_LOGGED_IN")
                    .title(title)
                    .message(message)
                    .referenceId(event.getUserId().toString())
                    .build();
            coordinator.dispatchInAppAndEmail(req, event.getEmail());
        } catch (Exception e) {
            log.error("Error handling UserLoggedInEvent: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.USER_REGISTERED_QUEUE)
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("UserRegisteredEvent: userId={}, email={}", event.getUserId(), event.getEmail());
        try {
            String name = event.getFirstName() != null ? event.getFirstName() : "there";
            String title = "Welcome!";
            String message = "Welcome to Online Pharmacy, " + name + "! Your account has been created.";
            CreateNotificationDispatchRequest req = CreateNotificationDispatchRequest.builder()
                    .userId(event.getUserId())
                    .type("USER_REGISTERED")
                    .title(title)
                    .message(message)
                    .referenceId(event.getUserId().toString())
                    .build();
            coordinator.dispatchInAppAndEmail(req, event.getEmail());
        } catch (Exception e) {
            log.error("Error handling UserRegisteredEvent: {}", e.getMessage(), e);
        }
    }
}
