package com.pharmacy.admin.service;

import com.pharmacy.common.feign.CatalogFeignClient;
import com.pharmacy.common.feign.InventoryInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogClient {

    private final CatalogFeignClient catalogFeignClient;

    public long countPendingPrescriptions() {
        try {
            Long count = catalogFeignClient.countPendingPrescriptions();
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("Error counting pending prescriptions: {}", e.getMessage());
            return 0L;
        }
    }

    public List<Map<String, Object>> getLowStockItems() {
        try {
            List<InventoryInfoDTO> items = catalogFeignClient.getLowStockItems(null);
            if (items == null) {
                return Collections.emptyList();
            }
            return items.stream()
                    .map(this::toMap)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching low stock items: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getExpiringBatches() {
        try {
            List<InventoryInfoDTO> batches = catalogFeignClient.getExpiringBatches();
            if (batches == null) {
                return Collections.emptyList();
            }
            return batches.stream()
                    .map(this::toMap)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching expiring batches: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public long countLowStockItems() {
        return getLowStockItems().size();
    }

    public long countExpiringBatches() {
        return getExpiringBatches().size();
    }

    private Map<String, Object> toMap(InventoryInfoDTO dto) {
        return Map.of(
                "id", dto.getId() != null ? dto.getId() : 0L,
                "medicineId", dto.getMedicineId() != null ? dto.getMedicineId() : 0L,
                "medicineName", dto.getMedicineName() != null ? dto.getMedicineName() : "Unknown",
                "batchNumber", dto.getBatchNumber() != null ? dto.getBatchNumber() : "",
                "quantity", dto.getQuantity() != null ? dto.getQuantity() : 0,
                "expiryDate", dto.getExpiryDate() != null ? dto.getExpiryDate().toString() : "",
                "expired", dto.getExpired() != null ? dto.getExpired() : false,
                "expiringSoon", dto.getExpiringSoon() != null ? dto.getExpiringSoon() : false
        );
    }
}
