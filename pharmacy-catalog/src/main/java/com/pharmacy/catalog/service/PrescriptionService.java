package com.pharmacy.catalog.service;

import com.pharmacy.catalog.dto.PrescriptionDTO;
import com.pharmacy.catalog.entity.Medicine;
import com.pharmacy.catalog.entity.Prescription;
import com.pharmacy.catalog.event.CatalogEventPublisher;
import com.pharmacy.catalog.repository.MedicineRepository;
import com.pharmacy.catalog.repository.PrescriptionRepository;
import com.pharmacy.common.enums.PrescriptionStatus;
import com.pharmacy.common.events.PrescriptionApprovedEvent;
import com.pharmacy.common.events.PrescriptionRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicineRepository medicineRepository;
    private final FileStorageService fileStorageService;
    private final CatalogEventPublisher catalogEventPublisher;

    private static final int PRESCRIPTION_VALIDITY_MONTHS = 6;

    public List<PrescriptionDTO> getUserPrescriptions(Long userId) {
        return prescriptionRepository.findByUserIdOrderByUploadedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PrescriptionDTO getPrescriptionById(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Prescription", id));
        return toDTO(prescription);
    }

    public Optional<Prescription> findApprovedPrescription(Long userId, Long medicineId) {
        return prescriptionRepository.findApprovedForUserAndMedicine(userId, medicineId);
    }

    public boolean hasValidPrescription(Long userId, Long medicineId) {
        Optional<Prescription> prescription = prescriptionRepository.findApprovedForUserAndMedicine(userId, medicineId);
        return prescription.isPresent() && prescription.get().isValid();
    }

    @Transactional
    public PrescriptionDTO uploadPrescription(Long userId, Long medicineId, MultipartFile file) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Medicine", medicineId));

        if (!medicine.getRequiresPrescription()) {
            throw new com.pharmacy.common.exception.BadRequestException("This medicine does not require a prescription");
        }

        String fileName = fileStorageService.storeFile(file);

        Prescription prescription = Prescription.builder()
                .userId(userId)
                .medicine(medicine)
                .filePath(fileName)
                .fileName(file.getOriginalFilename())
                .status(PrescriptionStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMonths(PRESCRIPTION_VALIDITY_MONTHS))
                .build();

        Prescription saved = prescriptionRepository.save(prescription);
        return toDTO(saved);
    }

    @Transactional
    public PrescriptionDTO approvePrescription(Long prescriptionId, Long adminId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Prescription", prescriptionId));

        prescription.setStatus(PrescriptionStatus.APPROVED);
        prescription.setReviewedBy(adminId);
        prescription.setReviewedAt(LocalDateTime.now());

        Prescription updated = prescriptionRepository.save(prescription);

        publishPrescriptionApproved(updated);

        return toDTO(updated);
    }

    @Transactional
    public PrescriptionDTO rejectPrescription(Long prescriptionId, Long adminId, String reason) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Prescription", prescriptionId));

        prescription.setStatus(PrescriptionStatus.REJECTED);
        prescription.setReviewedBy(adminId);
        prescription.setReviewedAt(LocalDateTime.now());
        prescription.setRejectionReason(reason);

        Prescription updated = prescriptionRepository.save(prescription);

        publishPrescriptionRejected(updated, reason);

        return toDTO(updated);
    }

    private void publishPrescriptionApproved(Prescription prescription) {
        try {
            PrescriptionApprovedEvent event = PrescriptionApprovedEvent.builder()
                    .prescriptionId(prescription.getId())
                    .userId(prescription.getUserId())
                    .medicineId(prescription.getMedicine().getId())
                    .medicineName(prescription.getMedicine().getName())
                    .build();

            catalogEventPublisher.publishPrescriptionApproved(event);
        } catch (Exception e) {
            log.error("Failed to publish PrescriptionApprovedEvent: {}", e.getMessage());
        }
    }

    private void publishPrescriptionRejected(Prescription prescription, String reason) {
        try {
            PrescriptionRejectedEvent event = PrescriptionRejectedEvent.builder()
                    .prescriptionId(prescription.getId())
                    .userId(prescription.getUserId())
                    .medicineId(prescription.getMedicine().getId())
                    .reason(reason)
                    .build();

            catalogEventPublisher.publishPrescriptionRejected(event);
        } catch (Exception e) {
            log.error("Failed to publish PrescriptionRejectedEvent: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getPendingPrescriptions() {
        return prescriptionRepository.findByStatusOrderByUploadedAtDesc(PrescriptionStatus.PENDING)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public long countPendingPrescriptions() {
        return prescriptionRepository.countPendingPrescriptions();
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void expireOldPrescriptions() {
        int expired = prescriptionRepository.expireOldPrescriptions(LocalDateTime.now());
        if (expired > 0) {
            log.info("Expired {} old prescriptions", expired);
        }
    }

    private PrescriptionDTO toDTO(Prescription prescription) {
        return PrescriptionDTO.builder()
                .id(prescription.getId())
                .userId(prescription.getUserId())
                .medicineId(prescription.getMedicine().getId())
                .medicineName(prescription.getMedicine().getName())
                .filePath("/api/catalog/prescriptions/" + prescription.getId() + "/download")
                .fileName(prescription.getFileName())
                .status(prescription.getStatus().name())
                .rejectionReason(prescription.getRejectionReason())
                .reviewedBy(prescription.getReviewedBy())
                .uploadedAt(prescription.getUploadedAt())
                .reviewedAt(prescription.getReviewedAt())
                .expiresAt(prescription.getExpiresAt())
                .build();
    }
}
