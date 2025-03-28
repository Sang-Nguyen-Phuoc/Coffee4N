package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_info")
data class Table(
    @PrimaryKey val id: Int = 0,
    val tableNumber: String = "",
    val capacity: Int = 0,
    val status: String = "AVAILABLE" // Default to "AVAILABLE"
)