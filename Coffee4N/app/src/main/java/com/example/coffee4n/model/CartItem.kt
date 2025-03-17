package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cartitem")
data class CartItem(
    @PrimaryKey val id: Int,
    val userId: Int,
    val productId: Int,
    val quantity: Int
)