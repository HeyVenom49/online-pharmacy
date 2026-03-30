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
public class CartItemDTO {
    private Long id;
    private Long medicineId;
    private String medicineName;
    private Integer quantity;
    private Double unitPrice;
    private Double subtotal;
    private Long prescriptionId;
    private LocalDateTime addedAt;
}
