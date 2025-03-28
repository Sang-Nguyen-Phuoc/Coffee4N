package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.coffee4n.utils.Converters
import java.util.Date

@Entity
@TypeConverters(Converters::class)
data class Employee(
    @PrimaryKey
    val id: Int = 0,
    val name: String = "",
    val position: String = "",
    val salary: Double = 0.0,
    val hireDate: Date = Date(),
    val phone: String = "",
    val email: String = ""
)