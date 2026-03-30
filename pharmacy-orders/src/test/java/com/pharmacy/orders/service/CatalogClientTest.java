package com.pharmacy.orders.service;

import com.pharmacy.common.feign.MedicineInfoDTO;
import com.pharmacy.orders.repository.CartRepository;
import com.pharmacy.orders.repository.CartItemRepository;
import com.pharmacy.orders.repository.OrderRepository;
import com.pharmacy.orders.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogClientTest {

    @Mock
    private com.pharmacy.common.feign.CatalogFeignClient catalogFeignClient;

    @InjectMocks
    private CatalogClient catalogClient;

    @Test
    void getMedicineInfo_shouldReturnMedicineInfo() {
        MedicineInfoDTO expected = MedicineInfoDTO.builder()
                .id(1L)
                .name("Paracetamol")
                .price(50.0)
                .requiresPrescription(false)
                .stock(100)
                .inStock(true)
                .build();

        when(catalogFeignClient.getMedicineInfo(1L)).thenReturn(expected);

        MedicineInfoDTO result = catalogClient.getMedicineInfo(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Paracetamol", result.getName());
        assertEquals(50.0, result.getPrice());
        verify(catalogFeignClient).getMedicineInfo(1L);
    }

    @Test
    void getMedicineInfo_shouldReturnNullOnException() {
        when(catalogFeignClient.getMedicineInfo(1L)).thenThrow(new RuntimeException("Service unavailable"));

        MedicineInfoDTO result = catalogClient.getMedicineInfo(1L);

        assertNull(result);
    }

    @Test
    void requiresPrescription_shouldReturnTrueWhenMedicineRequiresRx() {
        MedicineInfoDTO medicine = MedicineInfoDTO.builder()
                .id(1L)
                .requiresPrescription(true)
                .build();

        when(catalogFeignClient.getMedicineInfo(1L)).thenReturn(medicine);

        boolean result = catalogClient.requiresPrescription(1L);

        assertTrue(result);
    }

    @Test
    void requiresPrescription_shouldReturnFalseWhenMedicineDoesNotRequireRx() {
        MedicineInfoDTO medicine = MedicineInfoDTO.builder()
                .id(1L)
                .requiresPrescription(false)
                .build();

        when(catalogFeignClient.getMedicineInfo(1L)).thenReturn(medicine);

        boolean result = catalogClient.requiresPrescription(1L);

        assertFalse(result);
    }

    @Test
    void requiresPrescription_shouldReturnFalseOnNullMedicine() {
        when(catalogFeignClient.getMedicineInfo(1L)).thenReturn(null);

        boolean result = catalogClient.requiresPrescription(1L);

        assertFalse(result);
    }

    @Test
    void isInStock_shouldReturnTrueWhenStockSufficient() {
        MedicineInfoDTO medicine = MedicineInfoDTO.builder()
                .id(1L)
                .stock(100)
                .inStock(true)
                .build();

        when(catalogFeignClient.getMedicineInfo(1L)).thenReturn(medicine);

        boolean result = catalogClient.isInStock(1L, 50);

        assertTrue(result);
    }

    @Test
    void isInStock_shouldReturnFalseWhenStockInsufficient() {
        MedicineInfoDTO medicine = MedicineInfoDTO.builder()
                .id(1L)
                .stock(10)
                .inStock(true)
                .build();

        when(catalogFeignClient.getMedicineInfo(1L)).thenReturn(medicine);

        boolean result = catalogClient.isInStock(1L, 50);

        assertFalse(result);
    }

    @Test
    void isInStock_shouldReturnFalseWhenOutOfStock() {
        MedicineInfoDTO medicine = MedicineInfoDTO.builder()
                .id(1L)
                .stock(0)
                .inStock(false)
                .build();

        when(catalogFeignClient.getMedicineInfo(1L)).thenReturn(medicine);

        boolean result = catalogClient.isInStock(1L, 1);

        assertFalse(result);
    }

    @Test
    void hasValidPrescription_shouldReturnTrueWhenValid() {
        com.pharmacy.common.feign.PrescriptionCheckDTO checkDto = 
                com.pharmacy.common.feign.PrescriptionCheckDTO.builder()
                        .hasValidPrescription(true)
                        .prescriptionId(123L)
                        .status("APPROVED")
                        .build();

        when(catalogFeignClient.checkPrescription(1L, 2L)).thenReturn(checkDto);

        boolean result = catalogClient.hasValidPrescription(1L, 2L);

        assertTrue(result);
    }

    @Test
    void hasValidPrescription_shouldReturnFalseWhenInvalid() {
        com.pharmacy.common.feign.PrescriptionCheckDTO checkDto = 
                com.pharmacy.common.feign.PrescriptionCheckDTO.builder()
                        .hasValidPrescription(false)
                        .build();

        when(catalogFeignClient.checkPrescription(1L, 2L)).thenReturn(checkDto);

        boolean result = catalogClient.hasValidPrescription(1L, 2L);

        assertFalse(result);
    }

    @Test
    void hasValidPrescription_shouldReturnFalseOnException() {
        when(catalogFeignClient.checkPrescription(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Service unavailable"));

        boolean result = catalogClient.hasValidPrescription(1L, 2L);

        assertFalse(result);
    }
}
