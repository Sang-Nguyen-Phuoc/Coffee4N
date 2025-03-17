package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey val id: Int,
    val username: String,
    val email: String,
    val phone: String,
    val name: String,
    val address: String
)