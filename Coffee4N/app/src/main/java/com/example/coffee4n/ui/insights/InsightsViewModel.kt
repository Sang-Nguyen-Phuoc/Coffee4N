package com.example.coffee4n.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.repository.OrderItemRepository
import com.example.coffee4n.repository.OrderRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InsightsViewModel : ViewModel() {
    private val _state = MutableStateFlow(InsightsState())
    val state: StateFlow<InsightsState> = _state.asStateFlow()

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val orderItemRepository = OrderItemRepository(firebaseDatabase)
    private val orderRepository = OrderRepository(firebaseDatabase)

    init {
        loadInsightsData()
    }

    private fun loadInsightsData() {
        // Load most ordered products
        viewModelScope.launch {
            orderItemRepository.getMostOrderedItemsFlow(10).collectLatest { orderItemStats ->
                _state.value = _state.value.copy(orderItemStats = orderItemStats)
            }
        }

        // Load revenue by day data
        viewModelScope.launch {
            orderRepository.getRevenueByDayFlow().collectLatest { revenueData ->
                _state.value = _state.value.copy(dailyRevenueData = revenueData)
            }
        }

        // Load peak hours data
        viewModelScope.launch {
            orderRepository.getPeakHoursFlow().collectLatest { peakHoursData ->
                _state.value = _state.value.copy(peakHoursData = peakHoursData)
            }
        }
    }
}