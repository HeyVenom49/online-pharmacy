package com.pharmacy.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineDTO {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Double price;
    private Double mrp;
    private Boolean requiresPrescription;
    private Integer stock;
    private Boolean inStock;
    private Boolean expiringSoon;
    private LocalDate expiryDate;
    private String dosageForm;
    private String strength;
    private String manufacturer;
}
