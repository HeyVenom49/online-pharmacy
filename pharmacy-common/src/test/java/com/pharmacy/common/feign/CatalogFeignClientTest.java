package com.pharmacy.common.feign;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatalogFeignClientTest {

    @Test
    void testMedicineInfoDTOBuilder() {
        MedicineInfoDTO dto = MedicineInfoDTO.builder()
                .id(1L)
                .name("Test Medicine")
                .price(99.99)
                .requiresPrescription(true)
                .stock(100)
                .inStock(true)
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Test Medicine", dto.getName());
        assertEquals(99.99, dto.getPrice());
        assertTrue(dto.getRequiresPrescription());
        assertEquals(100, dto.getStock());
        assertTrue(dto.getInStock());
    }

    @Test
    void testPrescriptionCheckDTOBuilder() {
        PrescriptionCheckDTO dto = PrescriptionCheckDTO.builder()
                .hasValidPrescription(true)
                .prescriptionId(123L)
                .status("APPROVED")
                .build();

        assertTrue(dto.getHasValidPrescription());
        assertEquals(123L, dto.getPrescriptionId());
        assertEquals("APPROVED", dto.getStatus());
    }

    @Test
    void testInventoryInfoDTOBuilder() {
        InventoryInfoDTO dto = InventoryInfoDTO.builder()
                .id(1L)
                .medicineId(10L)
                .medicineName("Aspirin")
                .batchNumber("BATCH001")
                .quantity(50)
                .expired(false)
                .expiringSoon(true)
                .build();

        assertEquals(1L, dto.getId());
        assertEquals(10L, dto.getMedicineId());
        assertEquals("Aspirin", dto.getMedicineName());
        assertEquals("BATCH001", dto.getBatchNumber());
        assertEquals(50, dto.getQuantity());
        assertFalse(dto.getExpired());
        assertTrue(dto.getExpiringSoon());
    }

    @Test
    void testOrderSummaryDTOBuilder() {
        OrderSummaryDTO dto = OrderSummaryDTO.builder()
                .id(1L)
                .userId(100L)
                .status("PAID")
                .totalAmount(150.00)
                .deliveryFee(50.0)
                .discount(10.0)
                .grandTotal(190.0)
                .build();

        assertEquals(1L, dto.getId());
        assertEquals(100L, dto.getUserId());
        assertEquals("PAID", dto.getStatus());
        assertEquals(150.00, dto.getTotalAmount());
        assertEquals(50.0, dto.getDeliveryFee());
        assertEquals(10.0, dto.getDiscount());
        assertEquals(190.0, dto.getGrandTotal());
    }

    @Test
    void testMedicineInfoDTONullability() {
        MedicineInfoDTO dto = new MedicineInfoDTO();
        
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getPrice());
        assertNull(dto.getRequiresPrescription());
        assertNull(dto.getStock());
        assertNull(dto.getInStock());
    }

    @Test
    void testPrescriptionCheckDTONullPrescription() {
        PrescriptionCheckDTO dto = PrescriptionCheckDTO.builder()
                .hasValidPrescription(false)
                .build();

        assertFalse(dto.getHasValidPrescription());
        assertNull(dto.getPrescriptionId());
        assertNull(dto.getStatus());
    }
}
