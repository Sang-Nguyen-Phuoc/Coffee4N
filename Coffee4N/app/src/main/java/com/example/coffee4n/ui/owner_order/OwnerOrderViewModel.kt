package com.example.coffee4n.ui.owner_orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.model.Promotion
import com.example.coffee4n.repository.OrderItemRepository
import com.example.coffee4n.repository.OrderRepository
import com.example.coffee4n.repository.ProductRepository
import com.example.coffee4n.repository.PromotionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class OwnerOrderViewModel(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val productRepository: ProductRepository,
    private val promotionRepository: PromotionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OwnerOrderState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        loadPromotions()
        loadInitialOrders()
    }

    private fun loadInitialOrders() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, currentOrderPage = 1, orders = emptyList(), filteredOrders = emptyList()) }
            try {
                orderRepository.getAllOrders(1, _state.value.orderPageSize).collect { orders ->
                    _state.update { currentState ->
                        val filteredOrders = if (currentState.filterStatus != null) {
                            orders.filter { it.status == currentState.filterStatus }
                        } else {
                            orders
                        }
                        val totalOrders = orderRepository.getTotalOrderCount()
                        currentState.copy(
                            orders = orders,
                            filteredOrders = filteredOrders,
                            isLoading = false,
                            error = null,
                            hasMoreOrders = orders.size < totalOrders
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load orders: ${e.message}"
                    )
                }
            }
        }
    }

    fun loadMoreOrders() {
        viewModelScope.launch {
            val nextPage = _state.value.currentOrderPage + 1
            _state.update { it.copy(isLoading = true) }
            try {
                orderRepository.getAllOrders(nextPage, _state.value.orderPageSize).collect { newOrders ->
                    _state.update { currentState ->
                        val updatedOrders = currentState.orders + newOrders
                        val filteredOrders = if (currentState.filterStatus != null) {
                            updatedOrders.filter { it.status == currentState.filterStatus }
                        } else {
                            updatedOrders
                        }
                        val totalOrders = orderRepository.getTotalOrderCount()
                        currentState.copy(
                            orders = updatedOrders,
                            filteredOrders = filteredOrders,
                            currentOrderPage = nextPage,
                            isLoading = false,
                            error = null,
                            hasMoreOrders = updatedOrders.size < totalOrders
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load more orders: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadPromotions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingPromotions = true) }
            try {
                promotionRepository.getPromotionsFlow().collect { promotions ->
                    _state.update { currentState ->
                        val filteredPromotions = if (currentState.searchQuery.isNotBlank()) {
                            promotions.filter {
                                it.code.lowercase().contains(currentState.searchQuery.lowercase())
                            }
                        } else {
                            promotions
                        }
                        currentState.copy(
                            promotions = promotions,
                            filteredPromotions = filteredPromotions,
                            isLoadingPromotions = false,
                            promotionError = null
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoadingPromotions = false,
                        promotionError = "Failed to load promotions: ${e.message}"
                    )
                }
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        _state.update { it.copy(selectedTab = tabIndex) }
    }

    fun setFilterStatus(status: String?) {
        _state.update { currentState ->
            val filteredOrders = if (status != null) {
                currentState.orders.filter { it.status == status }
            } else {
                currentState.orders
            }
            currentState.copy(
                filterStatus = status,
                filteredOrders = filteredOrders
            )
        }
    }

    fun resetFilter() {
        _state.update {
            it.copy(
                filterStatus = null,
                filteredOrders = it.orders
            )
        }
    }

    fun markOrderComplete(orderId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(updatingOrderId = orderId) }
            try {
                orderRepository.updateOrderStatus(orderId, "COMPLETE")
                _state.update {
                    it.copy(
                        updatingOrderId = null,
                        successMessage = "Order #$orderId marked as Complete"
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        updatingOrderId = null,
                        error = "Failed to update order: ${e.message}"
                    )
                }
            }
        }
    }

    fun showOrderDetails(orderId: Int) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    selectedOrderId = orderId,
                    orderItems = emptyList(),
                    isLoadingOrderItems = true
                )
            }
            try {
                orderItemRepository.getOrderItemsByOrderId(orderId).collect { items ->
                    val orderItemsWithName = items.map { item ->
                        val product = try {
                            productRepository.getProductFlow(item.productId.toInt()).first()
                        } catch (e: NumberFormatException) {
                            null
                        }
                        OrderItemWithName(
                            orderId = item.orderId,
                            productId = item.productId.toString(),
                            productName = product?.name,
                            quantity = item.quantity,
                            price = item.price
                        )
                    }
                    _state.update {
                        it.copy(
                            orderItems = orderItemsWithName,
                            isLoadingOrderItems = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoadingOrderItems = false,
                        error = "Failed to load order items: ${e.message}"
                    )
                }
            }
        }
    }

    fun hideOrderDetails() {
        _state.update {
            it.copy(
                selectedOrderId = null,
                orderItems = emptyList(),
                isLoadingOrderItems = false
            )
        }
    }

    fun showAddPromotionDialog() {
        _state.update { it.copy(showAddPromotionDialog = true) }
    }

    fun hideAddPromotionDialog() {
        _state.update { it.copy(showAddPromotionDialog = false) }
    }

    fun addPromotion(promotion: Promotion) {
        viewModelScope.launch {
            try {
                val addedPromotion = promotionRepository.addPromotion(promotion)
                _state.update {
                    it.copy(
                        showAddPromotionDialog = false,
                        promotionSuccessMessage = "Promotion ${addedPromotion.code} added successfully with ID ${addedPromotion.id}"
                    )
                }
                loadPromotions() // Reload promotions to update the list
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        promotionError = "Failed to add promotion: ${e.message}"
                    )
                }
            }
        }
    }

    fun deletePromotion(promotionId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(deletingPromotionId = promotionId) }
            try {
                promotionRepository.deletePromotion(promotionId)
                _state.update {
                    it.copy(
                        deletingPromotionId = null,
                        promotionSuccessMessage = "Promotion #$promotionId deleted successfully"
                    )
                }
                loadPromotions() // Reload promotions to update the list
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        deletingPromotionId = null,
                        promotionError = "Failed to delete promotion: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }

    fun clearPromotionSuccessMessage() {
        _state.update { it.copy(promotionSuccessMessage = null) }
    }

    fun setSearchQuery(query: String) {
        _state.update { currentState ->
            val filteredPromotions = if (query.isNotBlank()) {
                currentState.promotions.filter {
                    it.code.lowercase().contains(query.lowercase())
                }
            } else {
                currentState.promotions
            }
            currentState.copy(
                searchQuery = query,
                filteredPromotions = filteredPromotions
            )
        }
    }
}

class OwnerOrderViewModelFactory(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val productRepository: ProductRepository,
    private val promotionRepository: PromotionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OwnerOrderViewModel::class.java)) {
            return OwnerOrderViewModel(
                orderRepository,
                orderItemRepository,
                productRepository,
                promotionRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}