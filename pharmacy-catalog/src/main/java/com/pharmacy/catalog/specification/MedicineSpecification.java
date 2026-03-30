package com.pharmacy.catalog.specification;

import com.pharmacy.catalog.entity.Medicine;
import org.springframework.data.jpa.domain.Specification;

public class MedicineSpecification {

    public static Specification<Medicine> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Medicine> inCategory(Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("category").get("id"), categoryId);
        };
    }

    public static Specification<Medicine> priceBetween(Double minPrice, Double maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null && maxPrice == null) {
                return criteriaBuilder.conjunction();
            }
            if (minPrice != null && maxPrice != null) {
                return criteriaBuilder.between(root.get("price"), minPrice, maxPrice);
            }
            if (minPrice != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    public static Specification<Medicine> requiresPrescription(Boolean requiresPrescription) {
        return (root, query, criteriaBuilder) -> {
            if (requiresPrescription == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("requiresPrescription"), requiresPrescription);
        };
    }

    public static Specification<Medicine> isActive() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("active"), true);
    }

    public static Specification<Medicine> hasStock() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("stock"), 0);
    }

    public static Specification<Medicine> isNotExpired() {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("expiryDate")),
                    criteriaBuilder.greaterThan(root.get("expiryDate"), java.time.LocalDate.now())
            );
        };
    }
}
