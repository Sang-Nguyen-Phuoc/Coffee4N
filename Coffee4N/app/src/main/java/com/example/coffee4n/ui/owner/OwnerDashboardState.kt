package com.example.coffee4n.ui.owner

data class OwnerDashboardState(
    val ordersCount: Int = 0,
    val lowStockItems: Int = 0,
    val employeesPresent: Int = 0,
    val dailyRevenue: Double = 0.0,
    val bookedTables: Int = 0
)