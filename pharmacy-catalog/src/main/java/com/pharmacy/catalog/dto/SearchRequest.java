package com.pharmacy.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    private String name;
    private Long categoryId;
    private Double minPrice;
    private Double maxPrice;
    private Boolean requiresPrescription;
    private Boolean inStock;
}
