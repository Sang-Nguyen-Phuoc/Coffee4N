package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "employee")
data class Employee(
    @PrimaryKey val id: Int,
    val name: String,
    val position: String,
    val salary: Double,
    val hireDate: Date,
    val phone: String,
    val email: String
)