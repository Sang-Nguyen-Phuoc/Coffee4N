package com.example.coffee4n.ui.owner_dashboard
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class OwnerDashboardState(
    val ordersCount: Int = 0,
    val lowStockItems: Int = 0,
    val employeesPresent: Int = 0,
    val pendingTableRequests: Int = 0,
    val confirmedTableRequests: Int = 0,
    val bookedTables: Int = 0,
    val totalTables: Int = 0,
    val activities: List<ActivityItem> = emptyList(),
    val avatarUrl: String = "",
    val error: String? = null
)

data class ActivityItem(
    val title: String,
    val time: String,
    val icon: ImageVector,
    val color: Color
)