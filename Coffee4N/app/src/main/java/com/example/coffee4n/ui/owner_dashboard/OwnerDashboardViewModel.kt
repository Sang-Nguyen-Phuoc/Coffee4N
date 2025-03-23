package com.example.coffee4n.ui.owner_dashboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OwnerDashboardViewModel : ViewModel() {
    private val _state = MutableStateFlow(OwnerDashboardState())
    val state: StateFlow<OwnerDashboardState> = _state.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        _state.value = OwnerDashboardState(
            ordersCount = 3,           // Mock: 3 active orders
            lowStockItems = 2,         // Mock: 2 items low on stock
            employeesPresent = 2,      // Mock: 2 employees present
            dailyRevenue = 150.75,     // Mock: $150.75 daily revenue
            bookedTables = 1           // Mock: 1 table booked
        )
    }
}