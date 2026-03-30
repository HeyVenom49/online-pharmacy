package com.pharmacy.catalog.repository;

import com.pharmacy.catalog.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long>, JpaSpecificationExecutor<Medicine> {

    List<Medicine> findByActiveTrue();

    List<Medicine> findByCategoryIdAndActiveTrue(Long categoryId);

    @Query("SELECT m FROM Medicine m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%')) AND m.active = true")
    List<Medicine> searchByName(@Param("name") String name);

    @Query("SELECT m FROM Medicine m WHERE m.requiresPrescription = true AND m.active = true")
    List<Medicine> findAllRequiringPrescription();

    @Query("SELECT m FROM Medicine m WHERE m.stock <= :threshold AND m.active = true")
    List<Medicine> findLowStock(@Param("threshold") int threshold);

    boolean existsByName(String name);
}
