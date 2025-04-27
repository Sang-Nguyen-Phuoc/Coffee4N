package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "order")
data class Order(
    @PrimaryKey val id: Int,
    val userId: Int = 0,
    val orderDate: Date = Date(),
    val totalAmount: Double = 0.0,
    val status: String = "PENDING",
    val deliveryMethod: String = ""
) {
    constructor() : this(0, 0, Date(), 0.0, "PENDING", "")
}
