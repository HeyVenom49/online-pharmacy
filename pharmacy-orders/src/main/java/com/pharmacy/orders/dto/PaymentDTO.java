package com.pharmacy.orders.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long id;
    private String status;
    private String paymentMethod;
    private String transactionId;
    private Double amount;
    private LocalDateTime paidAt;
}
