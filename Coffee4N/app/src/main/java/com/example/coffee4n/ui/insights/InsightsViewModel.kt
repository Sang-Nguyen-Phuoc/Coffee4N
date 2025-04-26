package com.example.coffee4n.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.BuildConfig
import com.example.coffee4n.repository.OrderItemRepository
import com.example.coffee4n.repository.OrderRepository
import com.example.coffee4n.service.GeminiService
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.util.Log

class InsightsViewModel : ViewModel() {
    private val _state = MutableStateFlow(InsightsState())
    val state: StateFlow<InsightsState> = _state.asStateFlow()

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val orderItemRepository = OrderItemRepository(firebaseDatabase)
    private val orderRepository = OrderRepository(firebaseDatabase)

    // Initialize Gemini Service with your API key
    private val geminiService = GeminiService(BuildConfig.GEMINI_API_KEY)

    init {
        loadInsightsData()
    }

    private fun loadInsightsData() {
        // Load most ordered products
        viewModelScope.launch {
            orderItemRepository.getMostOrderedItemsFlow(10).collectLatest { orderItemStats ->
                _state.value = _state.value.copy(orderItemStats = orderItemStats)
                updateAiInsights()
            }
        }

        // Load revenue by day data
        viewModelScope.launch {
            orderRepository.getRevenueByDayFlow().collectLatest { revenueData ->
                _state.value = _state.value.copy(dailyRevenueData = revenueData)
                updateAiInsights()
            }
        }

        // Load peak hours data
        viewModelScope.launch {
            orderRepository.getPeakHoursFlow().collectLatest { peakHoursData ->
                _state.value = _state.value.copy(peakHoursData = peakHoursData)
                updateAiInsights()
            }
        }
    }

    private fun updateAiInsights() {
        val currentState = _state.value

        // Only proceed if we have data in all categories
        if (currentState.orderItemStats.isEmpty() ||
            currentState.dailyRevenueData.isEmpty() ||
            currentState.peakHoursData.isEmpty()) {
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingInsights = true)

            // Calculate total revenue
            val totalRevenue = currentState.dailyRevenueData.sumOf { it.amount }

            // Generate business insights
            val businessInsights = geminiService.generateBusinessInsights(
                totalRevenue = totalRevenue,
                periodName = "Last 7 Days", // This will be dynamic based on selected period
                revenueData = currentState.dailyRevenueData,
                peakHoursData = currentState.peakHoursData,
                topProducts = currentState.orderItemStats
            )

            // Generate product recommendations
            val productRecommendations = geminiService.generateProductRecommendations(
                topProducts = currentState.orderItemStats
            )

            // Generate revenue trend analysis
            val revenueTrend = geminiService.analyzeRevenueTrend(
                revenueData = currentState.dailyRevenueData
            )

            // Update state with AI insights
            _state.value = _state.value.copy(
                businessInsights = businessInsights,
                productRecommendations = productRecommendations,
                revenueTrendAnalysis = revenueTrend,
                isLoadingInsights = false
            )
        }
    }

    fun refreshInsights(periodName: String) {
        _state.value = _state.value.copy(isLoadingInsights = true)

        viewModelScope.launch {
            val currentState = _state.value
            val totalRevenue = currentState.dailyRevenueData.sumOf { it.amount }

            // Re-generate insights with the new period
            val businessInsights = geminiService.generateBusinessInsights(
                totalRevenue = totalRevenue,
                periodName = periodName,
                revenueData = currentState.dailyRevenueData,
                peakHoursData = currentState.peakHoursData,
                topProducts = currentState.orderItemStats
            )

            _state.value = _state.value.copy(
                businessInsights = businessInsights,
                isLoadingInsights = false
            )
        }
    }
}