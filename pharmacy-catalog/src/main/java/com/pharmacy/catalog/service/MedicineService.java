package com.pharmacy.catalog.service;

import com.pharmacy.catalog.dto.*;
import com.pharmacy.catalog.entity.Category;
import com.pharmacy.catalog.entity.Inventory;
import com.pharmacy.catalog.entity.Medicine;
import com.pharmacy.catalog.repository.CategoryRepository;
import com.pharmacy.catalog.repository.InventoryRepository;
import com.pharmacy.catalog.repository.MedicineRepository;
import com.pharmacy.catalog.specification.MedicineSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;

    public Page<MedicineDTO> getAllMedicines(Pageable pageable) {
        return medicineRepository.findAll(pageable)
                .map(this::toDTO);
    }

    public Page<MedicineDTO> searchMedicines(SearchRequest request, Pageable pageable) {
        Specification<Medicine> spec = Specification
                .where(MedicineSpecification.hasName(request.getName()))
                .and(MedicineSpecification.inCategory(request.getCategoryId()))
                .and(MedicineSpecification.priceBetween(request.getMinPrice(), request.getMaxPrice()))
                .and(MedicineSpecification.requiresPrescription(request.getRequiresPrescription()))
                .and(MedicineSpecification.isActive())
                .and(MedicineSpecification.isNotExpired());

        if (Boolean.TRUE.equals(request.getInStock())) {
            spec = spec.and(MedicineSpecification.hasStock());
        }

        return medicineRepository.findAll(spec, pageable)
                .map(this::toDTO);
    }

    public MedicineDetailDTO getMedicineById(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Medicine", id));

        List<Inventory> inventoryList = inventoryRepository.findActiveByMedicineId(id, LocalDate.now());
        List<InventoryDTO> inventoryDTOs = inventoryList.stream()
                .map(this::toInventoryDTO)
                .collect(Collectors.toList());

        return toDetailDTO(medicine, inventoryDTOs);
    }

    public boolean requiresPrescription(Long medicineId) {
        return medicineRepository.findById(medicineId)
                .map(Medicine::getRequiresPrescription)
                .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Medicine", medicineId));
    }

    public boolean isInStock(Long medicineId, int quantity) {
        return medicineRepository.findById(medicineId)
                .map(m -> m.getStock() >= quantity)
                .orElse(false);
    }

    public boolean isExpired(Long medicineId) {
        return medicineRepository.findById(medicineId)
                .map(m -> m.getExpiryDate() != null && m.getExpiryDate().isBefore(LocalDate.now()))
                .orElse(false);
    }

    @Transactional
    public MedicineDTO createMedicine(MedicineRequest request) {
        Medicine medicine = Medicine.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .mrp(request.getMrp())
                .requiresPrescription(Boolean.TRUE.equals(request.getRequiresPrescription()))
                .stock(request.getStock() != null ? request.getStock() : 0)
                .dosageForm(request.getDosageForm())
                .strength(request.getStrength())
                .manufacturer(request.getManufacturer())
                .active(true)
                .build();

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Category", request.getCategoryId()));
            medicine.setCategory(category);
        }

        if (request.getExpiryDate() != null) {
            medicine.setExpiryDate(LocalDate.parse(request.getExpiryDate()));
        }

        Medicine saved = medicineRepository.save(medicine);
        return toDTO(saved);
    }

    @Transactional
    public MedicineDetailDTO createMedicineWithInventory(MedicineWithInventoryRequest request) {
        Medicine medicine = Medicine.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .mrp(request.getMrp())
                .requiresPrescription(Boolean.TRUE.equals(request.getRequiresPrescription()))
                .stock(request.getInitialStock())
                .dosageForm(request.getDosageForm())
                .strength(request.getStrength())
                .manufacturer(request.getManufacturer())
                .expiryDate(request.getExpiryDate())
                .active(true)
                .build();

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Category", request.getCategoryId()));
            medicine.setCategory(category);
        }

        Medicine saved = medicineRepository.save(medicine);

        Inventory inventory = Inventory.builder()
                .medicine(saved)
                .batchNumber(request.getBatchNumber())
                .quantity(request.getInitialStock())
                .manufactureDate(request.getManufactureDate())
                .expiryDate(request.getExpiryDate())
                .build();
        inventoryRepository.save(inventory);

        return getMedicineById(saved.getId());
    }

    @Transactional
    public MedicineDTO updateMedicine(Long id, MedicineRequest request) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Medicine", id));

        medicine.setName(request.getName());
        medicine.setDescription(request.getDescription());
        medicine.setPrice(request.getPrice());
        medicine.setMrp(request.getMrp());
        medicine.setRequiresPrescription(Boolean.TRUE.equals(request.getRequiresPrescription()));
        medicine.setDosageForm(request.getDosageForm());
        medicine.setStrength(request.getStrength());
        medicine.setManufacturer(request.getManufacturer());

        if (request.getExpiryDate() != null) {
            medicine.setExpiryDate(LocalDate.parse(request.getExpiryDate()));
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Category", request.getCategoryId()));
            medicine.setCategory(category);
        }

        Medicine updated = medicineRepository.save(medicine);
        return toDTO(updated);
    }

    @Transactional
    public void deleteMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Medicine", id));
        medicine.setActive(false);
        medicineRepository.save(medicine);
    }

    @Transactional
    public MedicineDTO activateMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Medicine", id));
        medicine.setActive(true);
        Medicine updated = medicineRepository.save(medicine);
        return toDTO(updated);
    }

    @Transactional
    public MedicineDTO deactivateMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Medicine", id));
        medicine.setActive(false);
        Medicine updated = medicineRepository.save(medicine);
        return toDTO(updated);
    }

    @Transactional
    public void updateStock(Long medicineId, int quantity) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new com.pharmacy.common.exception.ResourceNotFoundException("Medicine", medicineId));
        medicine.setStock(quantity);
        medicineRepository.save(medicine);
    }

    private MedicineDTO toDTO(Medicine medicine) {
        return MedicineDTO.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .description(medicine.getDescription())
                .categoryId(medicine.getCategory() != null ? medicine.getCategory().getId() : null)
                .categoryName(medicine.getCategory() != null ? medicine.getCategory().getName() : null)
                .price(medicine.getPrice())
                .mrp(medicine.getMrp())
                .requiresPrescription(medicine.getRequiresPrescription())
                .stock(medicine.getStock())
                .inStock(medicine.isInStock())
                .expiringSoon(medicine.isExpiringSoon())
                .expiryDate(medicine.getExpiryDate())
                .dosageForm(medicine.getDosageForm())
                .strength(medicine.getStrength())
                .manufacturer(medicine.getManufacturer())
                .build();
    }

    private MedicineDetailDTO toDetailDTO(Medicine medicine, List<InventoryDTO> inventoryList) {
        return MedicineDetailDTO.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .description(medicine.getDescription())
                .categoryId(medicine.getCategory() != null ? medicine.getCategory().getId() : null)
                .categoryName(medicine.getCategory() != null ? medicine.getCategory().getName() : null)
                .price(medicine.getPrice())
                .mrp(medicine.getMrp())
                .requiresPrescription(medicine.getRequiresPrescription())
                .stock(medicine.getStock())
                .inStock(medicine.isInStock())
                .expiryDate(medicine.getExpiryDate())
                .expiringSoon(medicine.isExpiringSoon())
                .dosageForm(medicine.getDosageForm())
                .strength(medicine.getStrength())
                .manufacturer(medicine.getManufacturer())
                .inventoryList(inventoryList)
                .build();
    }

    private InventoryDTO toInventoryDTO(Inventory inventory) {
        return InventoryDTO.builder()
                .id(inventory.getId())
                .medicineId(inventory.getMedicine().getId())
                .batchNumber(inventory.getBatchNumber())
                .quantity(inventory.getQuantity())
                .manufactureDate(inventory.getManufactureDate())
                .expiryDate(inventory.getExpiryDate())
                .expired(inventory.isExpired())
                .expiringSoon(inventory.isExpiringSoon())
                .build();
    }
}
