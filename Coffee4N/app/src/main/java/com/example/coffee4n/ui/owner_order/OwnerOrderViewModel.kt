package com.example.coffee4n.ui.owner_orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.model.Order
import com.example.coffee4n.model.Promotion
import com.example.coffee4n.model.User
import com.example.coffee4n.repository.OrderItemRepository
import com.example.coffee4n.repository.OrderRepository
import com.example.coffee4n.repository.ProductRepository
import com.example.coffee4n.repository.PromotionRepository
import com.example.coffee4n.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OwnerOrderViewModel(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val productRepository: ProductRepository,
    private val promotionRepository: PromotionRepository,
    private val userRepository: UserRepository
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
                        val filteredOrders = applyFilters(orders, currentState.filterStatus, currentState.orderSearchQuery)
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

    fun addPromotion(promotion: Promotion) {
        viewModelScope.launch {
            _state.update { it.copy(isAddingPromotion = true) }
            try {
                val existingPromotion = promotionRepository.getPromotionByCode(promotion.code)
                if (existingPromotion != null) {
                    _state.update {
                        it.copy(
                            isAddingPromotion = false,
                            promotionError = "Promotion code already exists"
                        )
                    }
                    return@launch
                }
                val addedPromotion = promotionRepository.addPromotion(promotion)
                _state.update {
                    it.copy(
                        showAddPromotionDialog = false,
                        isAddingPromotion = false,
                        promotionSuccessMessage = "Promotion ${addedPromotion.code} added successfully with ID ${addedPromotion.id}"
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isAddingPromotion = false,
                        promotionError = "Failed to add promotion: ${e.message}"
                    )
                }
            }
        }
    }

    fun showEditPromotionDialog(promotionId: Int) {
        _state.update { it.copy(selectedPromotionId = promotionId) }
    }

    fun hideEditPromotionDialog() {
        _state.update { it.copy(selectedPromotionId = null) }
    }

    fun updatePromotion(promotion: Promotion) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdatingPromotion = true) }
            try {
                // First delete the old promotion
                promotionRepository.deletePromotion(promotion.id)
                // Then add the updated promotion with the same ID
                promotionRepository.addPromotion(promotion)
                _state.update {
                    it.copy(
                        selectedPromotionId = null,
                        isUpdatingPromotion = false,
                        promotionSuccessMessage = "Promotion ${promotion.code} updated successfully"
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isUpdatingPromotion = false,
                        promotionError = "Failed to update promotion: ${e.message}"
                    )
                }
            }
        }
    }

    fun loadMoreOrders() {
        viewModelScope.launch {
            if (_state.value.isLoading || !_state.value.hasMoreOrders) return@launch
            val nextPage = _state.value.currentOrderPage + 1
            _state.update { it.copy(isLoading = true) }
            try {
                orderRepository.getAllOrders(nextPage, _state.value.orderPageSize).collect { newOrders ->
                    _state.update { currentState ->
                        val updatedOrders = currentState.orders + newOrders
                        val filteredOrders = applyFilters(updatedOrders, currentState.filterStatus, currentState.orderSearchQuery)
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

    fun refreshOrders() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, currentOrderPage = 1, orders = emptyList(), filteredOrders = emptyList()) }
            try {
                orderRepository.getAllOrders(1, _state.value.orderPageSize).collect { orders ->
                    _state.update { currentState ->
                        val filteredOrders = applyFilters(orders, currentState.filterStatus, currentState.orderSearchQuery)
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
                        error = "Failed to refresh orders: ${e.message}"
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
                    // Sắp xếp promotions theo startDate giảm dần (xa nhất đến gần nhất)
                    val sortedPromotions = promotions.sortedBy { it.startDate.time }
                    _state.update { currentState ->
                        val filteredPromotions = if (currentState.promotionSearchQuery.isNotBlank()) {
                            sortedPromotions.filter {
                                it.code.lowercase().contains(currentState.promotionSearchQuery.lowercase())
                            }
                        } else {
                            sortedPromotions
                        }
                        currentState.copy(
                            promotions = sortedPromotions,
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
            val filteredOrders = applyFilters(currentState.orders, status, currentState.orderSearchQuery)
            currentState.copy(
                filterStatus = status,
                filteredOrders = filteredOrders
            )
        }
    }

    fun setOrderSearchQuery(query: String) {
        _state.update { currentState ->
            val filteredOrders = applyFilters(currentState.orders, currentState.filterStatus, query)
            currentState.copy(
                orderSearchQuery = query,
                filteredOrders = filteredOrders
            )
        }
    }

    private fun applyFilters(orders: List<Order>, filterStatus: String?, orderSearchQuery: String): List<Order> {
        var filtered = orders
        if (filterStatus != null) {
            filtered = filtered.filter { it.status == filterStatus }
        }
        if (orderSearchQuery.isNotBlank()) {
            filtered = filtered.filter { it.id.toString().contains(orderSearchQuery) }
        }
        return filtered
    }

    fun resetFilter() {
        _state.update {
            it.copy(
                filterStatus = null,
                orderSearchQuery = "",
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
                    customer = null,
                    isLoadingOrderItems = true
                )
            }
            try {
                val order = state.value.orders.find { it.id == orderId }
                val customer = order?.userId?.let { userRepository.getUserById(it) }

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
                            customer = customer,
                            orderItems = orderItemsWithName,
                            isLoadingOrderItems = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoadingOrderItems = false,
                        error = "Failed to load order details: ${e.message}"
                    )
                }
            }
        }
    }

    fun hideOrderDetails() {
        _state.update {
            it.copy(
                selectedOrderId = null,
                customer = null,
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

    fun setPromotionSearchQuery(query: String) {
        _state.update { currentState ->
            val filteredPromotions = if (query.isNotBlank()) {
                currentState.promotions.filter {
                    it.code.lowercase().contains(query.lowercase())
                }
            } else {
                currentState.promotions
            }
            currentState.copy(
                promotionSearchQuery = query,
                filteredPromotions = filteredPromotions
            )
        }
    }
}

class OwnerOrderViewModelFactory(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val productRepository: ProductRepository,
    private val promotionRepository: PromotionRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OwnerOrderViewModel::class.java)) {
            return OwnerOrderViewModel(
                orderRepository,
                orderItemRepository,
                productRepository,
                promotionRepository,
                userRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
