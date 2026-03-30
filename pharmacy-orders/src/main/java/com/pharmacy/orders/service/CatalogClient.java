package com.pharmacy.orders.service;

import com.pharmacy.common.feign.CatalogFeignClient;
import com.pharmacy.common.feign.MedicineInfoDTO;
import com.pharmacy.common.feign.PrescriptionCheckDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogClient {

    private final CatalogFeignClient catalogFeignClient;

    public MedicineInfoDTO getMedicineInfo(Long medicineId) {
        try {
            return catalogFeignClient.getMedicineInfo(medicineId);
        } catch (Exception e) {
            log.error("Error fetching medicine info for {}: {}", medicineId, e.getMessage());
            return null;
        }
    }

    public boolean requiresPrescription(Long medicineId) {
        MedicineInfoDTO medicine = getMedicineInfo(medicineId);
        return medicine != null && Boolean.TRUE.equals(medicine.getRequiresPrescription());
    }

    public boolean isInStock(Long medicineId, int quantity) {
        MedicineInfoDTO medicine = getMedicineInfo(medicineId);
        return medicine != null && medicine.getStock() != null && medicine.getStock() >= quantity;
    }

    public boolean hasValidPrescription(Long userId, Long medicineId) {
        try {
            PrescriptionCheckDTO response = catalogFeignClient.checkPrescription(userId, medicineId);
            return response != null && Boolean.TRUE.equals(response.getHasValidPrescription());
        } catch (Exception e) {
            log.error("Error checking prescription: {}", e.getMessage());
            return false;
        }
    }
}
