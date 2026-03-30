package com.pharmacy.admin.service;

import com.pharmacy.admin.dto.DashboardDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final CatalogClient catalogClient;
    private final OrdersClient ordersClient;

    public DashboardDTO getDashboard() {
        log.info("Fetching dashboard metrics...");

        long totalOrders = ordersClient.countTotalOrders();
        long pendingOrders = ordersClient.countPendingOrders();
        long completedOrders = ordersClient.countCompletedOrders();
        double totalRevenue = ordersClient.calculateTotalRevenue();
        
        long pendingPrescriptions = catalogClient.countPendingPrescriptions();
        long lowStockItems = catalogClient.countLowStockItems();
        long expiringBatches = catalogClient.countExpiringBatches();

        DashboardDTO dashboard = DashboardDTO.builder()
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .completedOrders(completedOrders)
                .totalRevenue((long) totalRevenue)
                .pendingPrescriptions(pendingPrescriptions)
                .lowStockItems(lowStockItems)
                .expiringBatches(expiringBatches)
                .build();

        log.info("Dashboard metrics: {} orders, {} pending, {} completed, revenue: {}",
                totalOrders, pendingOrders, completedOrders, totalRevenue);

        return dashboard;
    }
}
