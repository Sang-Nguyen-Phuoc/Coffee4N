package com.example.coffee4n.model

import java.util.Date

data class Order(
    val id: Int,
    val userId: Int = 0,
    val orderDate: Date = Date(),
    val totalAmount: Double = 0.0,
    val status: String = "PENDING",
    val deliveryMethod: String = ""
) {
    // Thêm constructor không tham số cho Firebase
    constructor() : this(0, 0, Date(), 0.0, "PENDING", "")
}
