package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "booking")
data class Booking(
    @PrimaryKey val id: Int,
    val userId: Int,
    val bookingTimestamp: Date,
    val status: String // Enum: "PENDING", "CONFIRMED", "CANCELLED"
)