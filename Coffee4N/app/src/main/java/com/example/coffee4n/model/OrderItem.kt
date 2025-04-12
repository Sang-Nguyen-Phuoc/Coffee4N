package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orderitem")
data class OrderItem(
    @PrimaryKey val id: Int = 0,
    val orderId: Int = 0,
    val productId: Int = 0,
    val quantity: Int = 0,
    val price: Double = 0.0
)