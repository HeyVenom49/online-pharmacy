package com.pharmacy.common.feign;

import java.time.LocalDateTime;

public class OrderSummaryDTO {
    private Long id;
    private Long userId;
    private String status;
    private Double totalAmount;
    private Double deliveryFee;
    private Double discount;
    private Double grandTotal;
    private LocalDateTime orderedAt;
    private LocalDateTime updatedAt;

    public OrderSummaryDTO() {}

    public OrderSummaryDTO(Long id, Long userId, String status, Double totalAmount, Double deliveryFee,
                          Double discount, Double grandTotal, LocalDateTime orderedAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.deliveryFee = deliveryFee;
        this.discount = discount;
        this.grandTotal = grandTotal;
        this.orderedAt = orderedAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long userId;
        private String status;
        private Double totalAmount;
        private Double deliveryFee;
        private Double discount;
        private Double grandTotal;
        private LocalDateTime orderedAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder totalAmount(Double totalAmount) { this.totalAmount = totalAmount; return this; }
        public Builder deliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; return this; }
        public Builder discount(Double discount) { this.discount = discount; return this; }
        public Builder grandTotal(Double grandTotal) { this.grandTotal = grandTotal; return this; }
        public Builder orderedAt(LocalDateTime orderedAt) { this.orderedAt = orderedAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public OrderSummaryDTO build() { return new OrderSummaryDTO(id, userId, status, totalAmount, deliveryFee, discount, grandTotal, orderedAt, updatedAt); }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public Double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(Double deliveryFee) { this.deliveryFee = deliveryFee; }
    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }
    public Double getGrandTotal() { return grandTotal; }
    public void setGrandTotal(Double grandTotal) { this.grandTotal = grandTotal; }
    public LocalDateTime getOrderedAt() { return orderedAt; }
    public void setOrderedAt(LocalDateTime orderedAt) { this.orderedAt = orderedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
