package com.example.coffee4n.model

data class BookingTable(
    val id: Int = 0,
    val tableId: Int = 0,
    val customerName: String = "",
    val phoneNumber: String = "",
    val numberOfPeople: Int = 0,
    val bookingTime: String = "",
    val notes: String? = null,
    val status: String = "PENDING"  // PENDING, CONFIRMED, CANCELLED, v.v.
)