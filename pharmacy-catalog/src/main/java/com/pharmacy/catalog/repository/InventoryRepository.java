package com.pharmacy.catalog.repository;

import com.pharmacy.catalog.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByMedicineId(Long medicineId);

    @Query("SELECT i FROM Inventory i WHERE i.medicine.id = :medicineId AND i.expiryDate > :date ORDER BY i.expiryDate ASC")
    List<Inventory> findActiveByMedicineId(@Param("medicineId") Long medicineId, @Param("date") LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.medicine.id = :medicineId AND i.expiryDate > :date AND i.quantity > 0 ORDER BY i.expiryDate ASC")
    List<Inventory> findActiveByMedicineIdWithLock(@Param("medicineId") Long medicineId, @Param("date") LocalDate date);

    @Query("SELECT i FROM Inventory i WHERE i.expiryDate <= :date AND i.quantity > 0")
    List<Inventory> findExpiringBefore(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i WHERE i.medicine.id = :medicineId AND i.expiryDate > :date")
    Integer getTotalAvailableStock(@Param("medicineId") Long medicineId, @Param("date") LocalDate date);

    @Modifying
    @Query("UPDATE Inventory i SET i.quantity = i.quantity - :quantity WHERE i.id = :id AND i.quantity >= :quantity")
    int decrementStock(@Param("id") Long id, @Param("quantity") int quantity);

    Optional<Inventory> findFirstByMedicineIdAndExpiryDateAfterOrderByExpiryDateAsc(Long medicineId, LocalDate date);
}
