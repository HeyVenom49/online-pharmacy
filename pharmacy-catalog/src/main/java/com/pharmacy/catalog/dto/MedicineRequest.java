package com.pharmacy.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private Long categoryId;

    @Positive(message = "Price must be positive")
    private Double price;

    private Double mrp;

    private Boolean requiresPrescription;

    @Positive(message = "Stock must be positive")
    private Integer stock;

    private String expiryDate;

    private String dosageForm;

    private String strength;

    private String manufacturer;
}
