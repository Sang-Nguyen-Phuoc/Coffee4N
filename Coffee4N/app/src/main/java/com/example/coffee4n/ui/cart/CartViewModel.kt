package com.example.coffee4n.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(private val repository: CartRepository) : ViewModel() {
    private val _state = MutableStateFlow(CartState(isLoading = true))
    val state: StateFlow<CartState> = _state.asStateFlow()

    init {
        loadCartItems()
    }

    fun loadCartItems() {
        if (_state.value.cartItems.isNotEmpty()) return // Tránh tải lại nếu đã có dữ liệu
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                repository.getAllProductsFlow().collect { products ->
                    val cartItems = products.map { CartItem(it, 1) }
                    updateTotals(cartItems)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Không thể tải giỏ hàng: ${e.message}"
                )
            }
        }
    }

    fun updateQuantity(productId: Int, newQuantity: Int) {
        val item = _state.value.cartItems.find { it.product.id == productId }
        if (item != null) {
            if (newQuantity < 0) return // Không cho phép số lượng âm
            if (newQuantity > item.product.stockQuantity) {
                _state.value = _state.value.copy(
                    error = "Số lượng vượt quá tồn kho cho ${item.product.name}"
                )
                return
            }
            val updatedItems = _state.value.cartItems.map {
                if (it.product.id == productId) it.copy(quantity = newQuantity) else it
            }
            updateTotals(updatedItems)
            _state.value = _state.value.copy(successMessage = "Update ${item.product.name} quantity")
        }
    }

    fun removeItem(productId: Int) {
        val itemDeleted = _state.value.cartItems.find {it.product.id == productId}
        if (itemDeleted != null) {
            val updatedItems = _state.value.cartItems.filter { it.product.id != productId }
            updateTotals(updatedItems)
            _state.value = _state.value.copy(successMessage = "${itemDeleted.product.name} deleted!")
            return
        }
    }

    private fun updateTotals(cartItems: List<CartItem>) {
        val itemTotal = cartItems.sumOf { it.product.price * it.quantity }
        val tax = itemTotal * 0.02 // Thuế 2%
        val total = itemTotal + tax
        val outOfStock = cartItems.filter { it.quantity > it.product.stockQuantity }
            .map { it.product.id }
        _state.value = _state.value.copy(
            cartItems = cartItems,
            itemTotal = itemTotal,
            tax = tax,
            total = total,
            isLoading = false,
            outOfStockItems = outOfStock
        )
    }
}

class CartViewModelFactory(private val repository: CartRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            return CartViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}