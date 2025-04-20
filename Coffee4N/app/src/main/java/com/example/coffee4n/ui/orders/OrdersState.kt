package com.example.coffee4n.ui.orders

import com.example.coffee4n.model.Order
import com.example.coffee4n.model.OrderItem

data class OrdersState(
    val orders: List<OrderWithDetails> = emptyList(),
    val orderItems: Map<Int, List<OrderItemWithProduct>> = emptyMap(),
    val expandedOrders: Set<Int> = emptySet(),
    val searchDate: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

data class OrderWithDetails(
    val order: Order
)

data class OrderItemWithProduct(
    val orderItem: OrderItem,
    val productName: String
)