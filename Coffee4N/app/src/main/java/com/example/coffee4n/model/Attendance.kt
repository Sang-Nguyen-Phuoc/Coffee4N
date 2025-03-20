package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey val id: Int,
    val employeeId: Int,
    val date: Date,
    val checkInTime: Date,
    val checkOutTime: Date?
)