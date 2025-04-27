package com.example.coffee4n.model

data class Category(
    val id: Int,
    val name: String,
    val description: String
) {
    constructor() : this(0, "", "")  // Constructor mặc định cho Firebase
}