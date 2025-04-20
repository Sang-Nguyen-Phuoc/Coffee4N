package com.example.coffee4n.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.model.Order
import com.example.coffee4n.model.OrderItem
import com.example.coffee4n.repository.OrderItemRepository
import com.example.coffee4n.repository.OrderRepository
import com.example.coffee4n.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class OrdersViewModel(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val productRepository: ProductRepository,
    private val userId: Int
) : ViewModel() {

    private val _state = MutableStateFlow(OrdersState(isLoading = true))
    val state: StateFlow<OrdersState> = _state.asStateFlow()

    init {
        loadOrders()
    }

    private fun loadOrders() {
        viewModelScope.launch {
            try {
                val ordersFlow = orderRepository.getOrdersByUser(userId)
                val orders = ordersFlow.firstOrNull() ?: emptyList()
                val orderWithDetails = orders.map { OrderWithDetails(it) }
                _state.value = _state.value.copy(orders = orderWithDetails, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Không thể tải đơn hàng: ${e.message}"
                )
            }
        }
    }

    fun updateSearchDate(date: String) {
        try {
            _state.value = _state.value.copy(searchDate = date)
            filterOrdersByDate(date)
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = "Lỗi khi cập nhật ngày: ${e.message}"
            )
        }
    }

    private fun filterOrdersByDate(date: String) {
        viewModelScope.launch {
            try {
                val ordersFlow = orderRepository.getOrdersByUser(userId)
                val orders = ordersFlow.firstOrNull() ?: emptyList()
                val filteredOrders = if (date.isBlank()) {
                    orders
                } else {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    try {
                        val parsedDate = sdf.parse(date)
                        orders.filter { order ->
                            parsedDate?.let {
                                sdf.format(order.orderDate) == date
                            } ?: false
                        }
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
                _state.value = _state.value.copy(
                    orders = filteredOrders.map { OrderWithDetails(it) },
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Lỗi khi lọc đơn hàng: ${e.message}"
                )
            }
        }
    }

    fun toggleOrderExpansion(orderId: Int) {
        try {
            val currentExpanded = _state.value.expandedOrders
            val newExpandedOrders = if (currentExpanded.contains(orderId)) {
                currentExpanded - orderId
            } else {
                currentExpanded + orderId
            }
            _state.value = _state.value.copy(expandedOrders = newExpandedOrders)

            if (newExpandedOrders.contains(orderId) && !_state.value.orderItems.containsKey(orderId)) {
                loadOrderItems(orderId)
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = "Lỗi khi mở rộng đơn hàng: ${e.message}"
            )
        }
    }

    private fun loadOrderItems(orderId: Int) {
        viewModelScope.launch {
            try {
                orderItemRepository.getOrderItemsByOrderId(orderId).collect { orderItems ->
                    val orderItemsWithProduct = orderItems.mapNotNull { orderItem ->
                        try {
                            val product = productRepository.getProductFlow(orderItem.productId).firstOrNull()
                            product?.let {
                                OrderItemWithProduct(
                                    orderItem = orderItem,
                                    productName = it.name
                                )
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _state.value = _state.value.copy(
                        orderItems = _state.value.orderItems + (orderId to orderItemsWithProduct)
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Lỗi khi tải chi tiết đơn hàng: ${e.message}"
                )
            }
        }
    }

    fun cancelOrder(orderId: Int) {
        viewModelScope.launch {
            try {
                val order = _state.value.orders.find { it.order.id == orderId }?.order
                if (order?.status == "PENDING") {
                    orderRepository.deleteOrder(orderId)
                    orderItemRepository.deleteOrderItemsByOrderId(orderId)
                    val updatedOrders = _state.value.orders.filter { it.order.id != orderId }
                    _state.value = _state.value.copy(
                        orders = updatedOrders,
                        orderItems = _state.value.orderItems - orderId,
                        successMessage = "Order #$orderId has been cancelled and removed"
                    )
                } else {
                    _state.value = _state.value.copy(
                        error = "Cannot cancel order #$orderId: Only PENDING orders can be cancelled"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to cancel order: ${e.message}"
                )
            }
        }
    }

    fun clearSuccessMessage() {
        try {
            _state.value = _state.value.copy(successMessage = null)
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = "Lỗi khi xóa thông báo: ${e.message}"
            )
        }
    }
}

class OrdersViewModelFactory(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val productRepository: ProductRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrdersViewModel::class.java)) {
            return OrdersViewModel(
                orderRepository,
                orderItemRepository,
                productRepository,
                userId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}