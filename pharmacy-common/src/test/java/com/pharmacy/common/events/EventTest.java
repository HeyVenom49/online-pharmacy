package com.pharmacy.common.events;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    void testOrderPlacedEventCreation() {
        OrderPlacedEvent.OrderItemEvent item = OrderPlacedEvent.OrderItemEvent.builder()
                .medicineId(1L)
                .medicineName("Aspirin")
                .quantity(2)
                .unitPrice(50.0)
                .build();

        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .orderId(100L)
                .userId(1L)
                .grandTotal(150.0)
                .items(List.of(item))
                .build();

        event.setEventId("event-123");
        event.setEventType("ORDER_PLACED");
        event.setTimestamp(LocalDateTime.now());
        event.setCorrelationId("corr-456");

        assertEquals("event-123", event.getEventId());
        assertEquals("ORDER_PLACED", event.getEventType());
        assertNotNull(event.getTimestamp());
        assertEquals("corr-456", event.getCorrelationId());
        assertEquals(100L, event.getOrderId());
        assertEquals(1L, event.getUserId());
        assertEquals(150.0, event.getGrandTotal());
        assertEquals(1, event.getItems().size());
        assertEquals("Aspirin", event.getItems().get(0).getMedicineName());
    }

    @Test
    void testOrderCancelledEventCreation() {
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderId(100L)
                .userId(1L)
                .reason("Customer requested cancellation")
                .build();

        event.setEventId("cancel-123");
        event.setEventType("ORDER_CANCELLED");
        event.setTimestamp(LocalDateTime.now());

        assertEquals("cancel-123", event.getEventId());
        assertEquals("ORDER_CANCELLED", event.getEventType());
        assertEquals(100L, event.getOrderId());
        assertEquals(1L, event.getUserId());
        assertEquals("Customer requested cancellation", event.getReason());
    }

    @Test
    void testInventoryReservedEventCreation() {
        InventoryReservedEvent.ReservationItem reservation = 
                InventoryReservedEvent.ReservationItem.builder()
                        .medicineId(1L)
                        .quantity(10)
                        .batchNumber("BATCH001")
                        .build();

        InventoryReservedEvent event = InventoryReservedEvent.builder()
                .orderId(100L)
                .reservationId("res-123")
                .reservations(List.of(reservation))
                .build();

        event.setEventId("reserve-123");
        event.setEventType("INVENTORY_RESERVED");
        event.setTimestamp(LocalDateTime.now());

        assertEquals("reserve-123", event.getEventId());
        assertEquals("INVENTORY_RESERVED", event.getEventType());
        assertEquals(100L, event.getOrderId());
        assertEquals("res-123", event.getReservationId());
        assertEquals(1, event.getReservations().size());
        assertEquals("BATCH001", event.getReservations().get(0).getBatchNumber());
    }

    @Test
    void testInventoryReleasedEventCreation() {
        InventoryReleasedEvent.ReleaseItem release = 
                InventoryReleasedEvent.ReleaseItem.builder()
                        .medicineId(1L)
                        .quantity(5)
                        .batchNumber("BATCH002")
                        .build();

        InventoryReleasedEvent event = InventoryReleasedEvent.builder()
                .orderId(100L)
                .reservationId("res-123")
                .releases(List.of(release))
                .build();

        assertEquals(100L, event.getOrderId());
        assertEquals("res-123", event.getReservationId());
        assertEquals(1, event.getReleases().size());
        assertEquals(5, event.getReleases().get(0).getQuantity());
    }

    @Test
    void testPrescriptionApprovedEventCreation() {
        PrescriptionApprovedEvent event = PrescriptionApprovedEvent.builder()
                .prescriptionId(123L)
                .userId(1L)
                .medicineId(10L)
                .medicineName("Amoxicillin")
                .build();

        event.setEventId("rx-approve-123");
        event.setEventType("PRESCRIPTION_APPROVED");
        event.setTimestamp(LocalDateTime.now());

        assertEquals("rx-approve-123", event.getEventId());
        assertEquals("PRESCRIPTION_APPROVED", event.getEventType());
        assertEquals(123L, event.getPrescriptionId());
        assertEquals(1L, event.getUserId());
        assertEquals(10L, event.getMedicineId());
        assertEquals("Amoxicillin", event.getMedicineName());
    }

    @Test
    void testPrescriptionRejectedEventCreation() {
        PrescriptionRejectedEvent event = PrescriptionRejectedEvent.builder()
                .prescriptionId(123L)
                .userId(1L)
                .medicineId(10L)
                .reason("Illegible prescription image")
                .build();

        event.setEventId("rx-reject-123");
        event.setEventType("PRESCRIPTION_REJECTED");
        event.setTimestamp(LocalDateTime.now());

        assertEquals("rx-reject-123", event.getEventId());
        assertEquals("PRESCRIPTION_REJECTED", event.getEventType());
        assertEquals(123L, event.getPrescriptionId());
        assertEquals("Illegible prescription image", event.getReason());
    }

    @Test
    void testUserRegisteredEventCreation() {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(1L)
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        event.setEventId("user-reg-123");
        event.setEventType("USER_REGISTERED");
        event.setTimestamp(LocalDateTime.now());

        assertEquals("user-reg-123", event.getEventId());
        assertEquals("USER_REGISTERED", event.getEventType());
        assertEquals(1L, event.getUserId());
        assertEquals("john@example.com", event.getEmail());
        assertEquals("John", event.getFirstName());
        assertEquals("Doe", event.getLastName());
    }

    @Test
    void testBaseEventFields() {
        BaseEvent event = new BaseEvent() {};
        event.setEventId("base-123");
        event.setEventType("TEST_EVENT");
        event.setTimestamp(LocalDateTime.now());
        event.setCorrelationId("corr-789");

        assertEquals("base-123", event.getEventId());
        assertEquals("TEST_EVENT", event.getEventType());
        assertNotNull(event.getTimestamp());
        assertEquals("corr-789", event.getCorrelationId());
    }
}
