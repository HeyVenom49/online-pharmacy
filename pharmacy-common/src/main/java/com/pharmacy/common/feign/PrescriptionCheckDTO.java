package com.pharmacy.common.feign;

public class PrescriptionCheckDTO {
    private Boolean hasValidPrescription;
    private Long prescriptionId;
    private String status;

    public PrescriptionCheckDTO() {}

    public PrescriptionCheckDTO(Boolean hasValidPrescription, Long prescriptionId, String status) {
        this.hasValidPrescription = hasValidPrescription;
        this.prescriptionId = prescriptionId;
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Boolean hasValidPrescription;
        private Long prescriptionId;
        private String status;

        public Builder hasValidPrescription(Boolean hasValidPrescription) {
            this.hasValidPrescription = hasValidPrescription;
            return this;
        }

        public Builder prescriptionId(Long prescriptionId) {
            this.prescriptionId = prescriptionId;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public PrescriptionCheckDTO build() {
            return new PrescriptionCheckDTO(hasValidPrescription, prescriptionId, status);
        }
    }

    public Boolean getHasValidPrescription() { return hasValidPrescription; }
    public void setHasValidPrescription(Boolean hasValidPrescription) { this.hasValidPrescription = hasValidPrescription; }
    public Long getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Long prescriptionId) { this.prescriptionId = prescriptionId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
