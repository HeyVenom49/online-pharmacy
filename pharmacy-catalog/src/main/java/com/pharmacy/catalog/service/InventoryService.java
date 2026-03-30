package com.pharmacy.catalog.service;

import com.pharmacy.catalog.dto.InventoryDTO;
import com.pharmacy.catalog.dto.InventoryRequest;
import com.pharmacy.catalog.entity.Inventory;
import com.pharmacy.catalog.entity.Medicine;
import com.pharmacy.catalog.repository.InventoryRepository;
import com.pharmacy.catalog.repository.MedicineRepository;
import com.pharmacy.common.exception.BadRequestException;
import com.pharmacy.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final MedicineRepository medicineRepository;

    @Value("${prescription.expiry-alert-days:90}")
    private int expiryAlertDays;

    public List<InventoryDTO> getInventoryByMedicine(Long medicineId) {
        List<Inventory> inventoryList = inventoryRepository.findByMedicineId(medicineId);
        return inventoryList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventoryDTO addInventory(InventoryRequest request) {
        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", request.getMedicineId()));

        if (request.getExpiryDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot add inventory with past expiry date");
        }

        Inventory inventory = Inventory.builder()
                .medicine(medicine)
                .batchNumber(request.getBatchNumber())
                .quantity(request.getQuantity())
                .manufactureDate(request.getManufactureDate())
                .expiryDate(request.getExpiryDate())
                .build();

        Inventory saved = inventoryRepository.save(inventory);
        syncMedicineStock(medicine.getId());
        syncMedicineExpiryDate(medicine.getId());

        return toDTO(saved);
    }

    @Transactional
    public InventoryDTO updateInventory(Long id, InventoryRequest request) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", id));

        inventory.setBatchNumber(request.getBatchNumber());
        inventory.setQuantity(request.getQuantity());
        inventory.setManufactureDate(request.getManufactureDate());
        inventory.setExpiryDate(request.getExpiryDate());

        Inventory saved = inventoryRepository.save(inventory);
        syncMedicineStock(inventory.getMedicine().getId());
        syncMedicineExpiryDate(inventory.getMedicine().getId());

        return toDTO(saved);
    }

    @Transactional
    public void deleteInventory(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", id));

        Long medicineId = inventory.getMedicine().getId();
        inventoryRepository.delete(inventory);
        syncMedicineStock(medicineId);
        syncMedicineExpiryDate(medicineId);
    }

    @Transactional
    public boolean deductStock(Long medicineId, int quantity) {
        List<Inventory> activeBatches = inventoryRepository.findActiveByMedicineIdWithLock(
                medicineId, LocalDate.now());

        if (activeBatches.isEmpty()) {
            log.warn("No active batches found for medicineId={}", medicineId);
            return false;
        }

        int totalAvailable = activeBatches.stream().mapToInt(Inventory::getQuantity).sum();
        if (totalAvailable < quantity) {
            log.warn("Insufficient stock for medicineId={}. Required: {}, Available: {}", 
                    medicineId, quantity, totalAvailable);
            return false;
        }

        int remaining = quantity;
        for (Inventory batch : activeBatches) {
            if (remaining <= 0) break;

            int available = batch.getQuantity();
            int toDeduct = Math.min(available, remaining);

            batch.setQuantity(available - toDeduct);
            inventoryRepository.save(batch);
            log.debug("Deducted {} from batchId={}, new quantity={}", toDeduct, batch.getId(), batch.getQuantity());
            remaining -= toDeduct;
        }

        syncMedicineStock(medicineId);
        syncMedicineExpiryDate(medicineId);

        return remaining == 0;
    }

    @Transactional
    public boolean reserveStock(Long medicineId, int quantity, String reservationId) {
        List<Inventory> activeBatches = inventoryRepository.findActiveByMedicineIdWithLock(
                medicineId, LocalDate.now());

        if (activeBatches.isEmpty()) {
            log.warn("No active batches found for reservation. medicineId={}, reservationId={}", medicineId, reservationId);
            return false;
        }

        int totalAvailable = activeBatches.stream().mapToInt(Inventory::getQuantity).sum();
        if (totalAvailable < quantity) {
            log.warn("Insufficient stock for reservation. medicineId={}, required={}, available={}, reservationId={}", 
                    medicineId, quantity, totalAvailable, reservationId);
            return false;
        }

        int remaining = quantity;
        for (Inventory batch : activeBatches) {
            if (remaining <= 0) break;

            int available = batch.getQuantity();
            int toReserve = Math.min(available, remaining);

            batch.setQuantity(available - toReserve);
            inventoryRepository.save(batch);
            log.debug("Reserved {} from batchId={} for reservationId={}", toReserve, batch.getId(), reservationId);
            remaining -= toReserve;
        }

        syncMedicineStock(medicineId);
        return remaining == 0;
    }

    @Transactional
    public void addStock(Long medicineId, int quantity) {
        List<Inventory> activeBatches = inventoryRepository.findActiveByMedicineIdWithLock(
                medicineId, LocalDate.now());

        if (!activeBatches.isEmpty()) {
            Inventory oldestBatch = activeBatches.get(0);
            oldestBatch.setQuantity(oldestBatch.getQuantity() + quantity);
            inventoryRepository.save(oldestBatch);
            log.debug("Released {} to batchId={}, new quantity={}", quantity, oldestBatch.getId(), oldestBatch.getQuantity());
        } else {
            log.warn("No active batches found when releasing stock. medicineId={}, quantity={}", medicineId, quantity);
        }

        syncMedicineStock(medicineId);
    }

    @Transactional
    public void releaseStock(Long medicineId, int quantity) {
        addStock(medicineId, quantity);
        log.info("Released {} units for medicineId={}", quantity, medicineId);
    }

    public List<InventoryDTO> getLowStockMedicines(int threshold) {
        List<Medicine> medicines = medicineRepository.findAll();
        List<InventoryDTO> lowStockItems = new ArrayList<>();

        for (Medicine medicine : medicines) {
            int available = inventoryRepository.getTotalAvailableStock(medicine.getId(), LocalDate.now());
            if (available <= threshold) {
                InventoryDTO dto = InventoryDTO.builder()
                        .medicineId(medicine.getId())
                        .medicineName(medicine.getName())
                        .quantity(available)
                        .build();
                lowStockItems.add(dto);
            }
        }

        return lowStockItems;
    }

    public List<InventoryDTO> getExpiringBatches() {
        LocalDate alertDate = LocalDate.now().plusDays(expiryAlertDays);
        List<Inventory> expiring = inventoryRepository.findExpiringBefore(alertDate);

        return expiring.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Integer getAvailableStock(Long medicineId) {
        return inventoryRepository.getTotalAvailableStock(medicineId, LocalDate.now());
    }

    @Transactional
    public void syncMedicineStock(Long medicineId) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", medicineId));

        Integer totalStock = inventoryRepository.getTotalAvailableStock(medicineId, LocalDate.now());
        medicine.setStock(totalStock != null ? totalStock : 0);
        medicineRepository.save(medicine);
    }

    @Transactional
    public void syncMedicineExpiryDate(Long medicineId) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", medicineId));

        List<Inventory> activeBatches = inventoryRepository.findActiveByMedicineId(
                medicineId, LocalDate.now());

        if (!activeBatches.isEmpty()) {
            LocalDate nearestExpiry = activeBatches.stream()
                    .map(Inventory::getExpiryDate)
                    .min(LocalDate::compareTo)
                    .orElse(null);
            medicine.setExpiryDate(nearestExpiry);
            medicineRepository.save(medicine);
        }
    }

    private InventoryDTO toDTO(Inventory inventory) {
        return InventoryDTO.builder()
                .id(inventory.getId())
                .medicineId(inventory.getMedicine().getId())
                .medicineName(inventory.getMedicine().getName())
                .batchNumber(inventory.getBatchNumber())
                .quantity(inventory.getQuantity())
                .manufactureDate(inventory.getManufactureDate())
                .expiryDate(inventory.getExpiryDate())
                .expired(inventory.isExpired())
                .expiringSoon(inventory.isExpiringSoon())
                .build();
    }
}
