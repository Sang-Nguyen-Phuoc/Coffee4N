package com.example.coffee4n.ui.owner_orders

import com.example.coffee4n.model.Order
import com.example.coffee4n.model.Promotion
import com.example.coffee4n.model.User

data class OwnerOrderState(
    val orders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val filterStatus: String? = null,
    val orderSearchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val updatingOrderId: Int? = null,
    val selectedOrderId: Int? = null,
    val customer: User? = null,
    val orderItems: List<OrderItemWithName> = emptyList(),
    val isLoadingOrderItems: Boolean = false,
    val selectedTab: Int = 0,
    val promotions: List<Promotion> = emptyList(),
    val filteredPromotions: List<Promotion> = emptyList(),
    val promotionSearchQuery: String = "",
    val isLoadingPromotions: Boolean = false,
    val promotionError: String? = null,
    val promotionSuccessMessage: String? = null,
    val deletingPromotionId: Int? = null,
    val showAddPromotionDialog: Boolean = false,
    val isAddingPromotion: Boolean = false, // Thêm trạng thái mới
    val currentOrderPage: Int = 1,
    val orderPageSize: Int = 10,
    val hasMoreOrders: Boolean = true,
    val selectedPromotionId: Int? = null,
    val isUpdatingPromotion: Boolean = false
)

data class OrderItemWithName(
    val orderId: Int = 0,
    val productId: String = "",
    val productName: String? = null,
    val quantity: Int = 0,
    val price: Double = 0.0
)