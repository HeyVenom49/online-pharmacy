package com.pharmacy.common.feign;

public class MedicineInfoDTO {
    private Long id;
    private String name;
    private Double price;
    private Boolean requiresPrescription;
    private Integer stock;
    private Boolean inStock;

    public MedicineInfoDTO() {}

    public MedicineInfoDTO(Long id, String name, Double price, Boolean requiresPrescription, Integer stock, Boolean inStock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.requiresPrescription = requiresPrescription;
        this.stock = stock;
        this.inStock = inStock;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private Double price;
        private Boolean requiresPrescription;
        private Integer stock;
        private Boolean inStock;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder price(Double price) { this.price = price; return this; }
        public Builder requiresPrescription(Boolean requiresPrescription) { this.requiresPrescription = requiresPrescription; return this; }
        public Builder stock(Integer stock) { this.stock = stock; return this; }
        public Builder inStock(Boolean inStock) { this.inStock = inStock; return this; }
        public MedicineInfoDTO build() { return new MedicineInfoDTO(id, name, price, requiresPrescription, stock, inStock); }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Boolean getRequiresPrescription() { return requiresPrescription; }
    public void setRequiresPrescription(Boolean requiresPrescription) { this.requiresPrescription = requiresPrescription; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Boolean getInStock() { return inStock; }
    public void setInStock(Boolean inStock) { this.inStock = inStock; }
}
