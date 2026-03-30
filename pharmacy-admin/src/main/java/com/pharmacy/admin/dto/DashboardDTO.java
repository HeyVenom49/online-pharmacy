package com.pharmacy.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private long totalOrders;
    private long pendingOrders;
    private long completedOrders;
    private long totalRevenue;
    private long pendingPrescriptions;
    private long lowStockItems;
    private long expiringBatches;
}
