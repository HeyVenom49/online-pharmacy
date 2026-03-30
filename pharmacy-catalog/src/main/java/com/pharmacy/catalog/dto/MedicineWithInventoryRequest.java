package com.pharmacy.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineWithInventoryRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private Long categoryId;

    @Positive(message = "Price must be positive")
    private Double price;

    private Double mrp;

    private Boolean requiresPrescription;

    private String dosageForm;

    private String strength;

    private String manufacturer;

    private String batchNumber;

    @NotNull(message = "Initial stock quantity is required")
    @Positive(message = "Stock must be positive")
    private Integer initialStock;

    private LocalDate manufactureDate;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;
}
