package com.example.coffee4n.model

import java.util.Date

data class Employee(
    val id: Int = 0,
    val name: String = "",
    val position: String = "",
    val salary: Double = 0.0,
    val hireDate: Date = Date(),
    val phone: String = "",
    val email: String = ""
)