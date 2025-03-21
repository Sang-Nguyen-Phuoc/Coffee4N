package com.example.coffee4n.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.model.CartItem
import com.example.coffee4n.repository.CartItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(
    private val cartItemRepository: CartItemRepository
) : ViewModel() {
    // StateFlow để quản lý trạng thái giỏ hàng
    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state.asStateFlow()

    // Tải danh sách giỏ hàng theo userId
    fun loadCartItems(userId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                cartItemRepository.getCartItemsFlow(userId).collect { items ->
                    _state.value = _state.value.copy(cartItems = items, isLoading = false)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    // Tăng số lượng của một item
    fun increaseQuantity(cartItem: CartItem) {
        viewModelScope.launch {
            val updatedItem = cartItem.copy(slave = cartItem.quantity + 1)
            cartItemRepository.addCartItem(updatedItem)
        }
    }

    // Giảm số lượng của một item
    fun decreaseQuantity(cartItem: CartItem) {
        viewModelScope.launch {
            if (cartItem.quantity > 1) {
                val updatedItem = cartItem.copy(quantity = cartItem.quantity - 1)
                cartItemRepository.addCartItem(updatedItem)
            } else {
                cartItemRepository.deleteCartItem(cartItem.id, cartItem.userId)
            }
        }
    }

    // Xóa một item khỏi giỏ hàng
    fun removeItem(cartItem: CartItem) {
        viewModelScope.launch {
            cartItemRepository.deleteCartItem(cartItem.id, cartItem.userId)
        }
    }

    // Thanh toán (xóa toàn bộ giỏ hàng)
    fun checkout(userId: Int) {
        viewModelScope.launch {
            cartItemRepository.clearCart(userId)
        }
    }
}