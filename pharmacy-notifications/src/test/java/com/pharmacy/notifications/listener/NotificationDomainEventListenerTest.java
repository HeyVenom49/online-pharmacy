package com.pharmacy.notifications.listener;

import com.pharmacy.common.events.*;
import com.pharmacy.notifications.service.NotificationDispatchCoordinator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationDomainEventListenerTest {

    @Mock
    private NotificationDispatchCoordinator coordinator;

    @InjectMocks
    private NotificationDomainEventListener listener;

    private OrderPlacedEvent orderPlaced;

    @BeforeEach
    void events() {
        orderPlaced = OrderPlacedEvent.builder()
                .orderId(10L)
                .userId(20L)
                .grandTotal(10.0)
                .items(List.of(OrderPlacedEvent.OrderItemEvent.builder()
                        .medicineName("Aspirin")
                        .quantity(2)
                        .build()))
                .build();
    }

    @Test
    void onOrderPlaced() {
        listener.onOrderPlaced(orderPlaced);
        verify(coordinator).dispatchInAppAndEmail(any(), eq(null));
    }

    @Test
    void onOrderPlaced_nullItemsHandled() {
        OrderPlacedEvent e = OrderPlacedEvent.builder()
                .orderId(1L)
                .userId(2L)
                .grandTotal(1.0)
                .items(null)
                .build();
        listener.onOrderPlaced(e);
        ArgumentCaptor<com.pharmacy.common.dto.notification.CreateNotificationDispatchRequest> cap =
                ArgumentCaptor.forClass(com.pharmacy.common.dto.notification.CreateNotificationDispatchRequest.class);
        verify(coordinator).dispatchInAppAndEmail(cap.capture(), eq(null));
        assertTrue(cap.getValue().getMessage().contains("N/A"));
    }

    @Test
    void onOrderCancelled() {
        listener.onOrderCancelled(OrderCancelledEvent.builder()
                .orderId(3L)
                .userId(4L)
                .reason("changed mind")
                .build());
        verify(coordinator).dispatchInAppAndEmail(any(), eq(null));
    }

    @Test
    void onPrescriptionApproved() {
        listener.onPrescriptionApproved(PrescriptionApprovedEvent.builder()
                .prescriptionId(9L)
                .userId(8L)
                .medicineName("Med")
                .build());
        verify(coordinator).dispatchInAppAndEmail(any(), eq(null));
    }

    @Test
    void onPrescriptionRejected() {
        listener.onPrescriptionRejected(PrescriptionRejectedEvent.builder()
                .prescriptionId(11L)
                .userId(12L)
                .reason("invalid")
                .build());
        verify(coordinator).dispatchInAppAndEmail(any(), eq(null));
    }

    @Test
    void onUserLoggedIn() {
        listener.onUserLoggedIn(UserLoggedInEvent.builder()
                .userId(5L)
                .email("login@test.com")
                .firstName("Sam")
                .build());
        ArgumentCaptor<com.pharmacy.common.dto.notification.CreateNotificationDispatchRequest> cap =
                ArgumentCaptor.forClass(com.pharmacy.common.dto.notification.CreateNotificationDispatchRequest.class);
        verify(coordinator).dispatchInAppAndEmail(cap.capture(), eq("login@test.com"));
        assertEquals("USER_LOGGED_IN", cap.getValue().getType());
        assertEquals("Successful sign-in", cap.getValue().getTitle());
    }

    @Test
    void onUserLoggedIn_coordinatorThrows_isLoggedNotPropagated() {
        doThrow(new RuntimeException("down")).when(coordinator).dispatchInAppAndEmail(any(), any());
        assertDoesNotThrow(() -> listener.onUserLoggedIn(UserLoggedInEvent.builder()
                .userId(1L)
                .email("e@test.com")
                .firstName("A")
                .build()));
    }

    @Test
    void onUserRegistered() {
        listener.onUserRegistered(UserRegisteredEvent.builder()
                .userId(99L)
                .email("new@test.com")
                .firstName("Pat")
                .build());
        ArgumentCaptor<com.pharmacy.common.dto.notification.CreateNotificationDispatchRequest> cap =
                ArgumentCaptor.forClass(com.pharmacy.common.dto.notification.CreateNotificationDispatchRequest.class);
        verify(coordinator).dispatchInAppAndEmail(cap.capture(), eq("new@test.com"));
        assertEquals("USER_REGISTERED", cap.getValue().getType());
    }

    @Test
    void onUserRegistered_nullFirstName() {
        listener.onUserRegistered(UserRegisteredEvent.builder()
                .userId(1L)
                .email("e@test.com")
                .firstName(null)
                .build());
        ArgumentCaptor<com.pharmacy.common.dto.notification.CreateNotificationDispatchRequest> cap =
                ArgumentCaptor.forClass(com.pharmacy.common.dto.notification.CreateNotificationDispatchRequest.class);
        verify(coordinator).dispatchInAppAndEmail(cap.capture(), eq("e@test.com"));
        assertTrue(cap.getValue().getMessage().contains("there"));
    }

    @Test
    void onOrderPlaced_coordinatorThrows_isLoggedNotPropagated() {
        doThrow(new RuntimeException("down")).when(coordinator).dispatchInAppAndEmail(any(), eq(null));
        assertDoesNotThrow(() -> listener.onOrderPlaced(orderPlaced));
    }

    @Test
    void onOrderCancelled_coordinatorThrows_isLoggedNotPropagated() {
        doThrow(new RuntimeException("down")).when(coordinator).dispatchInAppAndEmail(any(), eq(null));
        assertDoesNotThrow(() -> listener.onOrderCancelled(OrderCancelledEvent.builder()
                .orderId(1L)
                .userId(2L)
                .reason("r")
                .build()));
    }

    @Test
    void onPrescriptionApproved_coordinatorThrows_isLoggedNotPropagated() {
        doThrow(new RuntimeException("down")).when(coordinator).dispatchInAppAndEmail(any(), eq(null));
        assertDoesNotThrow(() -> listener.onPrescriptionApproved(PrescriptionApprovedEvent.builder()
                .prescriptionId(1L)
                .userId(2L)
                .medicineName("M")
                .build()));
    }

    @Test
    void onPrescriptionRejected_coordinatorThrows_isLoggedNotPropagated() {
        doThrow(new RuntimeException("down")).when(coordinator).dispatchInAppAndEmail(any(), eq(null));
        assertDoesNotThrow(() -> listener.onPrescriptionRejected(PrescriptionRejectedEvent.builder()
                .prescriptionId(1L)
                .userId(2L)
                .reason("bad")
                .build()));
    }

    @Test
    void onUserRegistered_coordinatorThrows_isLoggedNotPropagated() {
        doThrow(new RuntimeException("down")).when(coordinator).dispatchInAppAndEmail(any(), any());
        assertDoesNotThrow(() -> listener.onUserRegistered(UserRegisteredEvent.builder()
                .userId(1L)
                .email("e@test.com")
                .firstName("A")
                .build()));
    }
}
