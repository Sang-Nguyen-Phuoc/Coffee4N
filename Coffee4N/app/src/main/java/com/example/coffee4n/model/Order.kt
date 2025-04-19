package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "order")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int = 0,
    val orderDate: Date? = null,
    val status: String = "",
    val totalAmount: Double = 0.0,
    val deliveryMethod: String = "",
    val promotionId: Int? = null
)