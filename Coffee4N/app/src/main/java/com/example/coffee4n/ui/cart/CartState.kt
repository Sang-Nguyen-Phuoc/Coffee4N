package com.example.coffee4n.ui.cart

import com.example.coffee4n.model.CartItem
import com.example.coffee4n.model.Product

data class CartState(
    val cartItems: List<CartItemWithProduct> = emptyList(),
    val itemTotal: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val outOfStockItems: List<Int> = emptyList()
)

data class CartItemWithProduct(
    val cartItem: CartItem,
    val product: Product
)