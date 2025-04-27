package com.example.coffee4n.model

data class Product(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,

    @get:JvmName("getBestSeller")
    @set:JvmName("setBestSeller")
    var isBestSeller: Boolean = false,

    val categoryId: Int,
    val imageUrl: String="",
    val stockQuantity: Int = 0,
    val costPrice: Double = 0.0
) {
    constructor() : this(0, "", "", 0.0, false, 0, "", 0, 0.0)
}