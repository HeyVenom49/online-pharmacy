package com.pharmacy.admin.service;

import com.pharmacy.common.feign.OrderSummaryDTO;
import com.pharmacy.common.feign.OrdersFeignClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdersClientTest {

    @Mock
    private OrdersFeignClient ordersFeignClient;

    @InjectMocks
    private OrdersClient ordersClient;

    @Test
    void getAllOrders_returnsEmptyOnNull() {
        when(ordersFeignClient.getAllOrders()).thenReturn(null);
        assertTrue(ordersClient.getAllOrders().isEmpty());
    }

    @Test
    void getAllOrders_returnsEmptyOnException() {
        when(ordersFeignClient.getAllOrders()).thenThrow(new RuntimeException("down"));
        assertTrue(ordersClient.getAllOrders().isEmpty());
    }

    @Test
    void countsAndRevenue() {
        List<OrderSummaryDTO> orders = List.of(
                order(1L, "DELIVERED", 10.0),
                order(2L, "PAID", 20.0),
                order(3L, "DRAFT_CART", 0.0),
                order(4L, "CHECKOUT_STARTED", 0.0),
                order(5L, "PAYMENT_PENDING", 0.0),
                order(6L, "PRESCRIPTION_PENDING", 0.0)
        );
        when(ordersFeignClient.getAllOrders()).thenReturn(orders);

        assertEquals(6, ordersClient.countTotalOrders());
        assertEquals(4, ordersClient.countPendingOrders());
        assertEquals(1, ordersClient.countCompletedOrders());
        assertEquals(20.0, ordersClient.calculateTotalRevenue(), 0.001);
    }

    private static OrderSummaryDTO order(Long id, String status, double grandTotal) {
        return OrderSummaryDTO.builder()
                .id(id)
                .userId(1L)
                .status(status)
                .grandTotal(grandTotal)
                .build();
    }
}
