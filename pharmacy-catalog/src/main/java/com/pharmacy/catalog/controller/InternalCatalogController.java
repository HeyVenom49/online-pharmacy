package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.InventoryDTO;
import com.pharmacy.catalog.dto.MedicineDetailDTO;
import com.pharmacy.catalog.service.InventoryService;
import com.pharmacy.catalog.service.MedicineService;
import com.pharmacy.catalog.service.PrescriptionService;
import com.pharmacy.common.feign.InventoryInfoDTO;
import com.pharmacy.common.feign.MedicineInfoDTO;
import com.pharmacy.common.feign.PrescriptionCheckDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalCatalogController {

    private final MedicineService medicineService;
    private final InventoryService inventoryService;
    private final PrescriptionService prescriptionService;

    @Value("${inventory.low-stock-threshold:10}")
    private int defaultLowStockThreshold;

    @GetMapping("/medicines/{id}")
    public ResponseEntity<MedicineInfoDTO> getMedicineInfo(@PathVariable Long id) {
        MedicineDetailDTO medicine = medicineService.getMedicineById(id);
        
        MedicineInfoDTO dto = MedicineInfoDTO.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .price(medicine.getPrice())
                .requiresPrescription(medicine.getRequiresPrescription())
                .stock(medicine.getStock())
                .inStock(medicine.getInStock())
                .build();
        
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/prescriptions/check")
    public ResponseEntity<PrescriptionCheckDTO> checkPrescription(
            @RequestParam Long userId,
            @RequestParam Long medicineId) {
        boolean hasValid = prescriptionService.hasValidPrescription(userId, medicineId);
        var prescription = prescriptionService.findApprovedPrescription(userId, medicineId);
        
        PrescriptionCheckDTO response = PrescriptionCheckDTO.builder()
                .hasValidPrescription(hasValid)
                .prescriptionId(prescription.map(p -> p.getId()).orElse(null))
                .status(prescription.map(p -> p.getStatus().name()).orElse(null))
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/prescriptions/count/pending")
    public ResponseEntity<Long> countPendingPrescriptions() {
        return ResponseEntity.ok(prescriptionService.countPendingPrescriptions());
    }

    @GetMapping("/inventory/low-stock")
    public ResponseEntity<List<InventoryInfoDTO>> getLowStockItems(
            @RequestParam(required = false) Integer threshold) {
        int effectiveThreshold = threshold != null ? threshold : defaultLowStockThreshold;
        List<InventoryDTO> inventory = inventoryService.getLowStockMedicines(effectiveThreshold);
        
        List<InventoryInfoDTO> result = inventory.stream()
                .map(this::toInventoryInfo)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/inventory/expiring")
    public ResponseEntity<List<InventoryInfoDTO>> getExpiringBatches() {
        List<InventoryDTO> inventory = inventoryService.getExpiringBatches();
        
        List<InventoryInfoDTO> result = inventory.stream()
                .map(this::toInventoryInfo)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/inventory/stock/{medicineId}")
    public ResponseEntity<Integer> getAvailableStock(@PathVariable Long medicineId) {
        return ResponseEntity.ok(inventoryService.getAvailableStock(medicineId));
    }

    private InventoryInfoDTO toInventoryInfo(InventoryDTO dto) {
        String medicineName = null;
        try {
            MedicineDetailDTO medicine = medicineService.getMedicineById(dto.getMedicineId());
            if (medicine != null) {
                medicineName = medicine.getName();
            }
        } catch (Exception e) {
            medicineName = "Unknown";
        }
        
        return InventoryInfoDTO.builder()
                .id(dto.getId())
                .medicineId(dto.getMedicineId())
                .medicineName(medicineName)
                .batchNumber(dto.getBatchNumber())
                .quantity(dto.getQuantity())
                .expiryDate(dto.getExpiryDate())
                .expired(dto.getExpired())
                .expiringSoon(dto.getExpiringSoon())
                .build();
    }
}
