package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orderitem")
data class OrderItem(
    @PrimaryKey val id: Int,
    val orderId: Int,
    val productId: Int,
    val quantity: Int,
    val price: Double
)