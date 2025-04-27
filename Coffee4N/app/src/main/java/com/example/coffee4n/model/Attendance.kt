package com.example.coffee4n.model

import java.util.Date

data class Attendance(
    val id: Int,
    val employeeId: Int,
    val date: Date,
    val checkInTime: Date,
    val checkOutTime: Date?
)