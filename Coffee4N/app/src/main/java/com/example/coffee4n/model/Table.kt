package com.example.coffee4n.model

data class Table(
    val id: Int = 0,
    val tableNumber: String = "",
    val capacity: Int = 0,
    val status: String = "AVAILABLE", // Default to "AVAILABLE"
    val imageUrl: String = ""
)