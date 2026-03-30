package com.pharmacy.common.feign;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CatalogFeignClientFallbackFactory implements FallbackFactory<CatalogFeignClient> {
    
    private static final Logger log = LoggerFactory.getLogger(CatalogFeignClientFallbackFactory.class);

    @Override
    public CatalogFeignClient create(Throwable cause) {
        log.error("CatalogFeignClient fallback triggered: {}", cause.getMessage(), cause);
        
        return new CatalogFeignClient() {
            @Override
            public MedicineInfoDTO getMedicineInfo(Long medicineId) {
                log.warn("Fallback: getMedicineInfo for {}", medicineId);
                return null;
            }

            @Override
            public PrescriptionCheckDTO checkPrescription(Long userId, Long medicineId) {
                log.warn("Fallback: checkPrescription for user {} and medicine {}", userId, medicineId);
                return PrescriptionCheckDTO.builder()
                        .hasValidPrescription(false)
                        .build();
            }

            @Override
            public Long countPendingPrescriptions() {
                log.warn("Fallback: countPendingPrescriptions");
                return 0L;
            }

            @Override
            public List<InventoryInfoDTO> getLowStockItems(Integer threshold) {
                log.warn("Fallback: getLowStockItems");
                return List.of();
            }

            @Override
            public List<InventoryInfoDTO> getExpiringBatches() {
                log.warn("Fallback: getExpiringBatches");
                return List.of();
            }

            @Override
            public Integer getAvailableStock(Long medicineId) {
                log.warn("Fallback: getAvailableStock for {}", medicineId);
                return 0;
            }
        };
    }
}
