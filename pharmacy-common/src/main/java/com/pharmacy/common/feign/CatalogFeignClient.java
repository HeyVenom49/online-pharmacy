package com.pharmacy.common.feign;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "pharmacy-catalog", fallbackFactory = CatalogFeignClientFallbackFactory.class)
public interface CatalogFeignClient {

    @CircuitBreaker(name = "catalogService", fallbackMethod = "getMedicineInfoFallback")
    @Retry(name = "catalogService")
    @GetMapping("/internal/medicines/{id}")
    MedicineInfoDTO getMedicineInfo(@PathVariable("id") Long medicineId);

    @CircuitBreaker(name = "catalogService", fallbackMethod = "checkPrescriptionFallback")
    @Retry(name = "catalogService")
    @GetMapping("/internal/prescriptions/check")
    PrescriptionCheckDTO checkPrescription(
            @RequestParam Long userId,
            @RequestParam Long medicineId);

    @CircuitBreaker(name = "catalogService", fallbackMethod = "countPendingPrescriptionsFallback")
    @Retry(name = "catalogService")
    @GetMapping("/internal/prescriptions/count/pending")
    Long countPendingPrescriptions();

    @CircuitBreaker(name = "catalogService", fallbackMethod = "getLowStockItemsFallback")
    @Retry(name = "catalogService")
    @GetMapping("/internal/inventory/low-stock")
    List<InventoryInfoDTO> getLowStockItems(@RequestParam(required = false) Integer threshold);

    @CircuitBreaker(name = "catalogService", fallbackMethod = "getExpiringBatchesFallback")
    @Retry(name = "catalogService")
    @GetMapping("/internal/inventory/expiring")
    List<InventoryInfoDTO> getExpiringBatches();

    @CircuitBreaker(name = "catalogService", fallbackMethod = "getAvailableStockFallback")
    @Retry(name = "catalogService")
    @GetMapping("/internal/inventory/stock/{medicineId}")
    Integer getAvailableStock(@PathVariable("medicineId") Long medicineId);

    default MedicineInfoDTO getMedicineInfoFallback(Long medicineId, Throwable t) {
        return null;
    }

    default PrescriptionCheckDTO checkPrescriptionFallback(Long userId, Long medicineId, Throwable t) {
        return PrescriptionCheckDTO.builder()
                .hasValidPrescription(false)
                .build();
    }

    default Long countPendingPrescriptionsFallback(Throwable t) {
        return 0L;
    }

    default List<InventoryInfoDTO> getLowStockItemsFallback(Integer threshold, Throwable t) {
        return List.of();
    }

    default List<InventoryInfoDTO> getExpiringBatchesFallback(Throwable t) {
        return List.of();
    }

    default Integer getAvailableStockFallback(Long medicineId, Throwable t) {
        return 0;
    }
}
