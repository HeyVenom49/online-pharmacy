package com.pharmacy.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineDetailDTO {
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
    private LocalDate expiryDate;
    private Boolean expiringSoon;
    private String dosageForm;
    private String strength;
    private String manufacturer;
    private List<InventoryDTO> inventoryList;
}
