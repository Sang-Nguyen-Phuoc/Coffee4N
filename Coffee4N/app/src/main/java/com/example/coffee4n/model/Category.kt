package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category")
data class Category(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String
) {
    constructor() : this(0, "", "")  // Constructor mặc định cho Firebase
}