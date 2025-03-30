package com.example.coffee4n.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
    val isBestSeller: Boolean = false
)


//package com.example.coffee4n.model


//
//import androidx.room.Entity
//import androidx.room.PrimaryKey
//
//@Entity(tableName = "product")
//data class Product(
//    @PrimaryKey val id: Int,
//    val name: String,
//    val description: String,
//    val price: Double,
//    val categoryId: Int,
//    val imageUrl: String,
//    val stockQuantity: Int,
//    val costPrice: Double
//)