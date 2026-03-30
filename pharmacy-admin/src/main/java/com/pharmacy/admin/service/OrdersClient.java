package com.pharmacy.admin.service;

import com.pharmacy.common.feign.OrderSummaryDTO;
import com.pharmacy.common.feign.OrdersFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersClient {

    private final OrdersFeignClient ordersFeignClient;

    public List<OrderSummaryDTO> getAllOrders() {
        try {
            List<OrderSummaryDTO> orders = ordersFeignClient.getAllOrders();
            return orders != null ? orders : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching orders: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public long countTotalOrders() {
        return getAllOrders().size();
    }

    public long countPendingOrders() {
        List<OrderSummaryDTO> orders = getAllOrders();
        return orders.stream()
                .filter(order -> {
                    String status = order.getStatus();
                    return status != null && (status.equals("DRAFT_CART") || 
                            status.equals("CHECKOUT_STARTED") || 
                            status.equals("PAYMENT_PENDING") ||
                            status.equals("PRESCRIPTION_PENDING"));
                })
                .count();
    }

    public long countCompletedOrders() {
        List<OrderSummaryDTO> orders = getAllOrders();
        return orders.stream()
                .filter(order -> {
                    String status = order.getStatus();
                    return status != null && status.equals("DELIVERED");
                })
                .count();
    }

    public double calculateTotalRevenue() {
        List<OrderSummaryDTO> orders = getAllOrders();
        return orders.stream()
                .filter(order -> {
                    String status = order.getStatus();
                    return status != null && status.equals("PAID");
                })
                .mapToDouble(order -> order.getGrandTotal() != null ? order.getGrandTotal() : 0.0)
                .sum();
    }
}
