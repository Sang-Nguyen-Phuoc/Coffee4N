package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_transactions")
data class InventoryTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemId: Int,
    val timestamp: Long, // epoch millis
    val quantity: Int,
    val unit: String,
    val type: TransactionType, // Enum: IMPORT / EXPORT
    val unitPrice: Double
) {
    constructor(): this(0, 0, 0, 0, "", TransactionType.IMPORT,0.0)
}

enum class TransactionType {
    IMPORT,
    EXPORT
}