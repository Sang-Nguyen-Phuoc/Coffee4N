package com.example.coffee4n.ui.owner_dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.repository.OrderItemRepository
import com.example.coffee4n.repository.ProductRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OwnerDashboardViewModel : ViewModel() {
    private val _state = MutableStateFlow(OwnerDashboardState())
    val state: StateFlow<OwnerDashboardState> = _state.asStateFlow()

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val orderItemRepository = OrderItemRepository(
        firebaseDatabase = firebaseDatabase
    )
    private val productRepository = ProductRepository(firebaseDatabase)

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        // Tải dữ liệu các mục được đặt hàng nhiều nhất
        viewModelScope.launch {
            orderItemRepository.getMostOrderedItemsFlow(5).collectLatest { orderItemStats ->
                _state.value = _state.value.copy(orderItemStats = orderItemStats)
            }
        }

        // Tải thông tin sản phẩm để kiểm tra hàng tồn kho thấp
        viewModelScope.launch {
            productRepository.getProductsFlow().collectLatest { products ->
                val lowStockCount = products.count { it.stockQuantity < 10 }
                _state.value = _state.value.copy(lowStockItems = lowStockCount)
            }
        }

        // Giá trị mặc định cho các phần còn lại
        _state.value = _state.value.copy(
            ordersCount = 3,
            employeesPresent = 2,
            dailyRevenue = 150.75,
            bookedTables = 1
        )
    }
}