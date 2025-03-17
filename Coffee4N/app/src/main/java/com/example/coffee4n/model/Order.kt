package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "order")
data class Order(
    @PrimaryKey val id: Int,
    val userId: Int,
    val orderDate: Date,
    val status: String,
    val totalAmount: Double,
    val deliveryMethod: String,
    val tableId: Int?,
    val promotionId: Int?
)