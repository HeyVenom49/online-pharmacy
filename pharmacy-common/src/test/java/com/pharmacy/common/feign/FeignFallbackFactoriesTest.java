package com.pharmacy.common.feign;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FeignFallbackFactoriesTest {

    @Test
    void catalogFallbackFactory_returnsSafeDefaults() {
        CatalogFeignClientFallbackFactory factory = new CatalogFeignClientFallbackFactory();
        CatalogFeignClient client = factory.create(new RuntimeException("down"));

        assertNull(client.getMedicineInfo(1L));
        assertFalse(client.checkPrescription(1L, 2L).getHasValidPrescription());
        assertEquals(0L, client.countPendingPrescriptions());
        assertTrue(client.getLowStockItems(5).isEmpty());
        assertTrue(client.getExpiringBatches().isEmpty());
        assertEquals(0, client.getAvailableStock(9L));
    }

    @Test
    void ordersFallbackFactory_returnsEmptyList() {
        OrdersFeignClientFallbackFactory factory = new OrdersFeignClientFallbackFactory();
        OrdersFeignClient client = factory.create(new RuntimeException("down"));

        List<OrderSummaryDTO> all = client.getAllOrders();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }
}
