package com.pharmacy.admin.service;

import com.pharmacy.common.feign.CatalogFeignClient;
import com.pharmacy.common.feign.InventoryInfoDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogClientTest {

    @Mock
    private CatalogFeignClient catalogFeignClient;

    @InjectMocks
    private CatalogClient catalogClient;

    @Test
    void countPendingPrescriptions_successAndErrors() {
        when(catalogFeignClient.countPendingPrescriptions()).thenReturn(5L);
        assertEquals(5L, catalogClient.countPendingPrescriptions());

        when(catalogFeignClient.countPendingPrescriptions()).thenThrow(new RuntimeException("x"));
        assertEquals(0L, catalogClient.countPendingPrescriptions());
    }

    @Test
    void getLowStockItems_mapsRows() {
        InventoryInfoDTO dto = InventoryInfoDTO.builder()
                .id(1L)
                .medicineId(2L)
                .medicineName("M")
                .batchNumber("B")
                .quantity(3)
                .expiryDate(LocalDate.now())
                .expired(false)
                .expiringSoon(true)
                .build();
        when(catalogFeignClient.getLowStockItems(null)).thenReturn(List.of(dto));

        List<Map<String, Object>> rows = catalogClient.getLowStockItems();
        assertEquals(1, rows.size());
        assertEquals("M", rows.get(0).get("medicineName"));
        assertEquals(1L, catalogClient.countLowStockItems());
    }

    @Test
    void getLowStockItems_nullAndException() {
        when(catalogFeignClient.getLowStockItems(null)).thenReturn(null);
        assertTrue(catalogClient.getLowStockItems().isEmpty());

        when(catalogFeignClient.getLowStockItems(null)).thenThrow(new RuntimeException("e"));
        assertTrue(catalogClient.getLowStockItems().isEmpty());
    }

    @Test
    void getExpiringBatches_mapsAndHandlesNull() {
        when(catalogFeignClient.getExpiringBatches()).thenReturn(null);
        assertTrue(catalogClient.getExpiringBatches().isEmpty());
        assertEquals(0L, catalogClient.countExpiringBatches());
    }
}
