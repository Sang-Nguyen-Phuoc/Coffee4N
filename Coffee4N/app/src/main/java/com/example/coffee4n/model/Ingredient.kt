package com.example.coffee4n.model

data class Ingredient(
    val id: Int = 0,
    val name: String,
    val unit: String,
    val quantity: Int,
    val threshold: Int
) {
    constructor() : this(0, "", "", 0, 0)
}