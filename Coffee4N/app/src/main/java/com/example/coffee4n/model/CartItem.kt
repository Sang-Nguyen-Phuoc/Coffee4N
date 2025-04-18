package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "cartitem")
data class CartItem(
    @PrimaryKey val id: Int = 0,
    val productId: Int = 0,
    val quantity: Int = 0,
    val userId: Int = 0,
    val note: String? = null // Ghi chú của người dùng
) {
    constructor() : this(0, 0, 0, 0, null)

    // This function can be used when sending data to Firebase
    fun toSimpleCartItem(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "productId" to productId,
            "quantity" to quantity,
            "userId" to userId,
            "note" to (note ?: "")
        )
    }
}