package com.pharmacy.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionCheckDTO {
    private Boolean hasValidPrescription;
    private Long prescriptionId;
    private String status;
}
