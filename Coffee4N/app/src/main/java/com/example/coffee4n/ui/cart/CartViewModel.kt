package com.example.coffee4n.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.model.CartItem
import com.example.coffee4n.model.Product
import com.example.coffee4n.repository.CartItemRepository
import com.example.coffee4n.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CartViewModel(
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
    private val userId: Int
) : ViewModel() {
    private val _state = MutableStateFlow(CartState(isLoading = true))
    val state: StateFlow<CartState> = _state.asStateFlow()

    init {
        loadCartItems()
    }

    fun loadCartItems() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                cartItemRepository.getCartItemsFlow(userId).collect { cartItems ->
                    println("DEBUG: Received cartItems for userId $userId: $cartItems")
                    updateUIFromCartItems(cartItems)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Không thể tải giỏ hàng: ${e.message}"
                )
            }
        }
    }

    fun updateQuantity(cartItem: CartItemWithProduct, newQuantity: Int) {
        viewModelScope.launch {
            if (newQuantity < 1) {
                removeItem(cartItem)
                return@launch
            }
            if (newQuantity > cartItem.product.stockQuantity) {
                _state.value = _state.value.copy(
                    error = "Số lượng vượt quá tồn kho cho ${cartItem.product.name}"
                )
                return@launch
            }
            try {
                val updatedCartItem = cartItem.cartItem.copy(quantity = newQuantity)
                cartItemRepository.addCartItem(updatedCartItem)
                _state.value = _state.value.copy(
                    successMessage = "Đã cập nhật số lượng ${cartItem.product.name}"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Không thể cập nhật số lượng: ${e.message}"
                )
            }
        }
    }

    fun removeItem(cartItem: CartItemWithProduct) {
        viewModelScope.launch {
            try {
                cartItemRepository.deleteCartItem(cartItem.cartItem.id, cartItem.cartItem.userId)
                _state.value = _state.value.copy(
                    successMessage = "Đã xóa ${cartItem.product.name}!"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Không thể xóa sản phẩm: ${e.message}"
                )
            }
        }
    }

    fun updateCartItemNote(cartItem: CartItemWithProduct, newNote: String?) {
        viewModelScope.launch {
            try {
                cartItemRepository.updateCartItemNote(
                    cartItem.cartItem.id,
                    cartItem.cartItem.userId,
                    newNote
                )
                _state.value = _state.value.copy(
                    successMessage = "Đã cập nhật ghi chú cho ${cartItem.product.name}"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Không thể cập nhật ghi chú: ${e.message}"
                )
            }
        }
    }

    private fun updateUIFromCartItems(cartItems: List<CartItem>) {
        viewModelScope.launch {
            if (cartItems.isEmpty()) {
                updateTotals(emptyList())
            } else {
                val productFlows = cartItems.map { cartItem ->
                    productRepository.getProductFlow(cartItem.productId).map { product ->
                        product?.let { CartItemWithProduct(cartItem, it) }
                    }
                }
                combine(productFlows) { items ->
                    items.filterNotNull()
                }.distinctUntilChanged().collect { cartItemsWithProduct ->
                    updateTotals(cartItemsWithProduct)
                }
            }
        }
    }

    private fun updateTotals(cartItems: List<CartItemWithProduct>) {
        val itemTotal = cartItems.sumOf { it.product.price * it.cartItem.quantity }
        val tax = itemTotal * 0.02 // Thuế 2%
        val total = itemTotal + tax
        val outOfStock = cartItems.filter { it.cartItem.quantity > it.product.stockQuantity }
            .map { it.product.id }
        println("DEBUG: Updating totals with ${cartItems.size} items: $cartItems")
        _state.value = _state.value.copy(
            cartItems = cartItems,
            itemTotal = itemTotal,
            tax = tax,
            total = total,
            isLoading = false,
            outOfStockItems = outOfStock,
            error = null,
            successMessage = null
        )
    }

    fun clearCart() {
        viewModelScope.launch {
            cartItemRepository.deleteAllCartItems(userId)
            _state.update { it.copy(cartItems = emptyList(), itemTotal = 0.0, tax = 0.0, total = 0.0) }
        }
    }
}

class CartViewModelFactory(
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            return CartViewModel(cartItemRepository, productRepository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}