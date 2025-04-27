package com.example.coffee4n.model

data class OrderItem(
    val id: Int = 0,
    val orderId: Int = 0,
    val productId: Int = 0,
    val quantity: Int = 0,
    val price: Double = 0.0
)