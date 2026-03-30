package com.pharmacy.catalog.repository;

import com.pharmacy.catalog.entity.Prescription;
import com.pharmacy.common.enums.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByUserIdOrderByUploadedAtDesc(Long userId);

    List<Prescription> findByStatusOrderByUploadedAtDesc(PrescriptionStatus status);

    List<Prescription> findByUserIdAndStatus(Long userId, PrescriptionStatus status);

    Optional<Prescription> findByUserIdAndMedicineIdAndStatus(Long userId, Long medicineId, PrescriptionStatus status);

    @Query("SELECT p FROM Prescription p WHERE p.userId = :userId AND p.medicine.id = :medicineId AND p.status = 'APPROVED' ORDER BY p.reviewedAt DESC")
    Optional<Prescription> findApprovedForUserAndMedicine(@Param("userId") Long userId, @Param("medicineId") Long medicineId);

    @Modifying
    @Query("UPDATE Prescription p SET p.status = 'EXPIRED', p.reviewedAt = :now WHERE p.status = 'APPROVED' AND p.expiresAt < :now")
    int expireOldPrescriptions(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.status = :status")
    long countByStatus(@Param("status") PrescriptionStatus status);

    @Query("SELECT COUNT(p) FROM Prescription p WHERE p.status = 'PENDING'")
    long countPendingPrescriptions();
}
