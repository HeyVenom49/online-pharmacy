package com.pharmacy.common.events;

import java.time.LocalDateTime;
import java.util.List;

public class OrderPlacedEvent extends BaseEvent {
    private Long orderId;
    private Long userId;
    private Double grandTotal;
    private List<OrderItemEvent> items;

    public OrderPlacedEvent() {}

    public OrderPlacedEvent(String eventId, LocalDateTime timestamp, Long orderId, Long userId, Double grandTotal, List<OrderItemEvent> items) {
        super(eventId, null, timestamp, null);
        this.orderId = orderId;
        this.userId = userId;
        this.grandTotal = grandTotal;
        this.items = items;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String eventId;
        private LocalDateTime timestamp;
        private Long orderId;
        private Long userId;
        private Double grandTotal;
        private List<OrderItemEvent> items;

        public Builder eventId(String eventId) { this.eventId = eventId; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public Builder orderId(Long orderId) { this.orderId = orderId; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder grandTotal(Double grandTotal) { this.grandTotal = grandTotal; return this; }
        public Builder items(List<OrderItemEvent> items) { this.items = items; return this; }
        public OrderPlacedEvent build() { return new OrderPlacedEvent(eventId, timestamp, orderId, userId, grandTotal, items); }
    }

    public static class OrderItemEvent {
        private Long medicineId;
        private String medicineName;
        private Integer quantity;
        private Double unitPrice;

        public OrderItemEvent() {}
        public OrderItemEvent(Long medicineId, String medicineName, Integer quantity, Double unitPrice) {
            this.medicineId = medicineId;
            this.medicineName = medicineName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long medicineId;
            private String medicineName;
            private Integer quantity;
            private Double unitPrice;

            public Builder medicineId(Long medicineId) { this.medicineId = medicineId; return this; }
            public Builder medicineName(String medicineName) { this.medicineName = medicineName; return this; }
            public Builder quantity(Integer quantity) { this.quantity = quantity; return this; }
            public Builder unitPrice(Double unitPrice) { this.unitPrice = unitPrice; return this; }
            public OrderItemEvent build() { return new OrderItemEvent(medicineId, medicineName, quantity, unitPrice); }
        }

        public Long getMedicineId() { return medicineId; }
        public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
        public String getMedicineName() { return medicineName; }
        public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Double getGrandTotal() { return grandTotal; }
    public void setGrandTotal(Double grandTotal) { this.grandTotal = grandTotal; }
    public List<OrderItemEvent> getItems() { return items; }
    public void setItems(List<OrderItemEvent> items) { this.items = items; }
}
