package com.example.coffee4n.ui.owner_dashboard

data class OwnerDashboardState(
    val ordersCount: Int = 0,
    val lowStockItems: Int = 0,
    val employeesPresent: Int = 0,
    val dailyRevenue: Double = 0.0,
    val bookedTables: Int = 0,
    val orderItemStats: List<OrderItemStat> = emptyList() // Thay cartItemStats bằng orderItemStats
)

data class OrderItemStat(
    val productName: String,
    val count: Int
)