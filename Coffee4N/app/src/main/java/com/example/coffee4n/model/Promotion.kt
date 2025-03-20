package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "promotion")
data class Promotion(
    @PrimaryKey val id: Int,
    val code: String,
    val description: String,
    val discountType: String, // Enum: "PERCENTAGE" hoặc "FIXED"
    val discountValue: Double,
    val startDate: Date,
    val endDate: Date,
    val minOrderAmount: Double
)