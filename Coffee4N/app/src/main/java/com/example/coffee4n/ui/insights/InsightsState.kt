package com.example.coffee4n.ui.insights

data class InsightsState(
    val orderItemStats: List<OrderItemStat> = emptyList(),
    val dailyRevenueData: List<DailyRevenue> = emptyList(),
    val peakHoursData: List<HourlyData> = emptyList(),
)

data class OrderItemStat(
    val productName: String,
    val count: Int
)

data class DailyRevenue(
    val dayName: String,
    val amount: Double
)

data class HourlyData(
    val hourRange: String,
    val orderCount: Int
)