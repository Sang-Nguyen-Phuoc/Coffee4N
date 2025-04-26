package com.example.coffee4n.model

import java.util.Date

data class Booking(
    val id: Int,
    val userId: Int,
    val bookingTimestamp: Date,
    val status: String // Enum: "PENDING", "CONFIRMED", "CANCELLED"
)