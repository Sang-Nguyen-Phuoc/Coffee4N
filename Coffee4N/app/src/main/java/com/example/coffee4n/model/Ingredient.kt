package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val unit: String,
    val quantity: Int,
    val threshold: Int
) {
    constructor() : this(0, "", "", 0, 0)
}