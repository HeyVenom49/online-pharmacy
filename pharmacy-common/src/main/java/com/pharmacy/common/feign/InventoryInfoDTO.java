package com.pharmacy.common.feign;

import java.time.LocalDate;

public class InventoryInfoDTO {
    private Long id;
    private Long medicineId;
    private String medicineName;
    private String batchNumber;
    private Integer quantity;
    private LocalDate expiryDate;
    private Boolean expired;
    private Boolean expiringSoon;

    public InventoryInfoDTO() {}

    public InventoryInfoDTO(Long id, Long medicineId, String medicineName, String batchNumber,
                           Integer quantity, LocalDate expiryDate, Boolean expired, Boolean expiringSoon) {
        this.id = id;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.expired = expired;
        this.expiringSoon = expiringSoon;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long medicineId;
        private String medicineName;
        private String batchNumber;
        private Integer quantity;
        private LocalDate expiryDate;
        private Boolean expired;
        private Boolean expiringSoon;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder medicineId(Long medicineId) { this.medicineId = medicineId; return this; }
        public Builder medicineName(String medicineName) { this.medicineName = medicineName; return this; }
        public Builder batchNumber(String batchNumber) { this.batchNumber = batchNumber; return this; }
        public Builder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public Builder expiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; return this; }
        public Builder expired(Boolean expired) { this.expired = expired; return this; }
        public Builder expiringSoon(Boolean expiringSoon) { this.expiringSoon = expiringSoon; return this; }
        public InventoryInfoDTO build() { return new InventoryInfoDTO(id, medicineId, medicineName, batchNumber, quantity, expiryDate, expired, expiringSoon); }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public Boolean getExpired() { return expired; }
    public void setExpired(Boolean expired) { this.expired = expired; }
    public Boolean getExpiringSoon() { return expiringSoon; }
    public void setExpiringSoon(Boolean expiringSoon) { this.expiringSoon = expiringSoon; }
}
