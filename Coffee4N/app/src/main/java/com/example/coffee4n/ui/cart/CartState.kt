package com.example.coffee4n.ui.cart

import com.example.coffee4n.model.CartItem

data class CartState(
    val cartItems: List<CartItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)