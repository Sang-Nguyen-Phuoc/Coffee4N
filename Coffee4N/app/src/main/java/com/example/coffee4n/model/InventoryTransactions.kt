package com.example.coffee4n.model

data class InventoryTransaction(
    val id: Int = 0,
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