package com.pharmacy.orders.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private Long userId;
    private String status;
    private Double totalAmount;
    private Double deliveryFee;
    private Double discount;
    private Double grandTotal;
    private String addressSnapshot;
    private String addressPincode;
    private String deliverySlot;
    private String notes;
    private List<OrderItemDTO> items;
    private PaymentDTO payment;
    private LocalDateTime orderedAt;
    private LocalDateTime updatedAt;
}
