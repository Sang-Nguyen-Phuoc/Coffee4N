package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_info")
data class Table(
    @PrimaryKey val id: Int,
    val tableNumber: String,
    val capacity: Int,
    val status: String // Enum: "AVAILABLE", "OCCUPIED"
)