package com.pharmacy.admin.service;

import com.pharmacy.admin.dto.DashboardDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private CatalogClient catalogClient;

    @Mock
    private OrdersClient ordersClient;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getDashboard_aggregates() {
        when(ordersClient.countTotalOrders()).thenReturn(10L);
        when(ordersClient.countPendingOrders()).thenReturn(2L);
        when(ordersClient.countCompletedOrders()).thenReturn(7L);
        when(ordersClient.calculateTotalRevenue()).thenReturn(99.5);
        when(catalogClient.countPendingPrescriptions()).thenReturn(3L);
        when(catalogClient.countLowStockItems()).thenReturn(4L);
        when(catalogClient.countExpiringBatches()).thenReturn(1L);

        DashboardDTO d = dashboardService.getDashboard();

        assertNotNull(d);
        assertEquals(10L, d.getTotalOrders());
        assertEquals(2L, d.getPendingOrders());
        assertEquals(7L, d.getCompletedOrders());
        assertEquals(99L, d.getTotalRevenue());
        assertEquals(3L, d.getPendingPrescriptions());
        assertEquals(4L, d.getLowStockItems());
        assertEquals(1L, d.getExpiringBatches());
    }
}
