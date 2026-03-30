package com.pharmacy.common.events;

import java.time.LocalDateTime;
import java.util.List;

public class InventoryReservedEvent extends BaseEvent {
    private Long orderId;
    private String reservationId;
    private List<ReservationItem> reservations;

    public InventoryReservedEvent() {}

    public InventoryReservedEvent(String eventId, LocalDateTime timestamp, Long orderId, String reservationId, List<ReservationItem> reservations) {
        super(eventId, null, timestamp, null);
        this.orderId = orderId;
        this.reservationId = reservationId;
        this.reservations = reservations;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String eventId;
        private LocalDateTime timestamp;
        private Long orderId;
        private String reservationId;
        private List<ReservationItem> reservations;

        public Builder eventId(String eventId) { this.eventId = eventId; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public Builder orderId(Long orderId) { this.orderId = orderId; return this; }
        public Builder reservationId(String reservationId) { this.reservationId = reservationId; return this; }
        public Builder reservations(List<ReservationItem> reservations) { this.reservations = reservations; return this; }
        public InventoryReservedEvent build() { return new InventoryReservedEvent(eventId, timestamp, orderId, reservationId, reservations); }
    }

    public static class ReservationItem {
        private Long medicineId;
        private Integer quantity;
        private String batchNumber;

        public ReservationItem() {}
        public ReservationItem(Long medicineId, Integer quantity, String batchNumber) {
            this.medicineId = medicineId;
            this.quantity = quantity;
            this.batchNumber = batchNumber;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long medicineId;
            private Integer quantity;
            private String batchNumber;

            public Builder medicineId(Long medicineId) { this.medicineId = medicineId; return this; }
            public Builder quantity(Integer quantity) { this.quantity = quantity; return this; }
            public Builder batchNumber(String batchNumber) { this.batchNumber = batchNumber; return this; }
            public ReservationItem build() { return new ReservationItem(medicineId, quantity, batchNumber); }
        }

        public Long getMedicineId() { return medicineId; }
        public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getBatchNumber() { return batchNumber; }
        public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }
    public List<ReservationItem> getReservations() { return reservations; }
    public void setReservations(List<ReservationItem> reservations) { this.reservations = reservations; }
}
