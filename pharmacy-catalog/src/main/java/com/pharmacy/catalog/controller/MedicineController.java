package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.MedicineDTO;
import com.pharmacy.catalog.dto.MedicineDetailDTO;
import com.pharmacy.catalog.dto.MedicineRequest;
import com.pharmacy.catalog.dto.MedicineWithInventoryRequest;
import com.pharmacy.catalog.dto.SearchRequest;
import com.pharmacy.catalog.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalog/medicines")
@RequiredArgsConstructor
@Tag(name = "Medicines", description = "Medicine catalog APIs")
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    @Operation(summary = "Get all medicines", description = "Returns paginated list of active medicines")
    public ResponseEntity<Page<MedicineDTO>> getAllMedicines(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(medicineService.getAllMedicines(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search medicines", description = "Search medicines with filters")
    public ResponseEntity<Page<MedicineDTO>> searchMedicines(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean requiresPrescription,
            @RequestParam(required = false) Boolean inStock,
            @PageableDefault(size = 20) Pageable pageable) {
        
        SearchRequest request = SearchRequest.builder()
                .name(name)
                .categoryId(categoryId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .requiresPrescription(requiresPrescription)
                .inStock(inStock)
                .build();
        
        return ResponseEntity.ok(medicineService.searchMedicines(request, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get medicine details", description = "Returns full details of a medicine")
    public ResponseEntity<MedicineDetailDTO> getMedicineById(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.getMedicineById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create medicine", description = "Creates a new medicine (Admin only)")
    public ResponseEntity<MedicineDTO> createMedicine(@Valid @RequestBody MedicineRequest request) {
        MedicineDTO medicine = medicineService.createMedicine(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(medicine);
    }

    @PostMapping("/with-inventory")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create medicine with inventory", description = "Creates a new medicine with initial inventory batch (Admin only)")
    public ResponseEntity<MedicineDetailDTO> createMedicineWithInventory(
            @Valid @RequestBody MedicineWithInventoryRequest request) {
        MedicineDetailDTO medicine = medicineService.createMedicineWithInventory(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(medicine);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update medicine", description = "Updates an existing medicine (Admin only)")
    public ResponseEntity<MedicineDTO> updateMedicine(
            @PathVariable Long id,
            @Valid @RequestBody MedicineRequest request) {
        MedicineDTO medicine = medicineService.updateMedicine(id, request);
        return ResponseEntity.ok(medicine);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete medicine", description = "Soft-deletes a medicine by setting active=false (Admin only)")
    public ResponseEntity<Void> deleteMedicine(@PathVariable Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate medicine", description = "Reactivates a soft-deleted medicine (Admin only)")
    public ResponseEntity<MedicineDTO> activateMedicine(@PathVariable Long id) {
        MedicineDTO medicine = medicineService.activateMedicine(id);
        return ResponseEntity.ok(medicine);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate medicine", description = "Deactivates a medicine (Admin only)")
    public ResponseEntity<MedicineDTO> deactivateMedicine(@PathVariable Long id) {
        MedicineDTO medicine = medicineService.deactivateMedicine(id);
        return ResponseEntity.ok(medicine);
    }
}
