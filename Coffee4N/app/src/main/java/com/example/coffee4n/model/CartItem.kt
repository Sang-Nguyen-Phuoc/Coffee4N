package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cartitem")
data class CartItem(
    @PrimaryKey val id: Int = 0,
    val productId: Int = 0,
    val quantity: Int = 0,
    val userId: Int = 0,
    val productName: String,
    val productPrice: Double,
    val productImageUrl: String = ""
) {
    constructor() : this(0, 0, 0, 0, "", 0.0, "")

    fun getTotal(): Double = productPrice * quantity
}