package com.pharmacy.catalog.controller;

import com.pharmacy.catalog.dto.InventoryDTO;
import com.pharmacy.catalog.dto.InventoryRequest;
import com.pharmacy.catalog.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory batch management APIs")
public class InventoryController {

    private final InventoryService inventoryService;

    @Value("${inventory.low-stock-threshold:10}")
    private int defaultLowStockThreshold;

    @GetMapping("/medicine/{medicineId}")
    @Operation(summary = "Get inventory by medicine", description = "Returns all inventory batches for a medicine")
    public ResponseEntity<List<InventoryDTO>> getInventoryByMedicine(@PathVariable Long medicineId) {
        return ResponseEntity.ok(inventoryService.getInventoryByMedicine(medicineId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add inventory batch", description = "Adds a new inventory batch for a medicine")
    public ResponseEntity<InventoryDTO> addInventory(@Valid @RequestBody InventoryRequest request) {
        InventoryDTO inventory = inventoryService.addInventory(request);
        return ResponseEntity.ok(inventory);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update inventory batch", description = "Updates an existing inventory batch")
    public ResponseEntity<InventoryDTO> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody InventoryRequest request) {
        InventoryDTO inventory = inventoryService.updateInventory(id, request);
        return ResponseEntity.ok(inventory);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete inventory batch", description = "Deletes an inventory batch")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get low stock medicines", description = "Returns medicines with stock below threshold")
    public ResponseEntity<List<InventoryDTO>> getLowStockMedicines(
            @RequestParam(required = false) Integer threshold) {
        int effectiveThreshold = threshold != null ? threshold : defaultLowStockThreshold;
        return ResponseEntity.ok(inventoryService.getLowStockMedicines(effectiveThreshold));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get expiring batches", description = "Returns inventory batches expiring within alert period")
    public ResponseEntity<List<InventoryDTO>> getExpiringBatches() {
        return ResponseEntity.ok(inventoryService.getExpiringBatches());
    }

    @GetMapping("/stock/{medicineId}")
    @Operation(summary = "Get available stock", description = "Returns available stock quantity for a medicine")
    public ResponseEntity<Integer> getAvailableStock(@PathVariable Long medicineId) {
        return ResponseEntity.ok(inventoryService.getAvailableStock(medicineId));
    }
}
