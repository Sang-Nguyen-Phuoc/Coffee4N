package com.example.coffee4n.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.coffee4n.model.Product

@Dao
interface ProductDao {
    @Query("SELECT * FROM product")
    suspend fun getAllProducts(): List<Product>

    @Insert
    suspend fun insertProduct(product: Product)

    @Query("SELECT COUNT(*) FROM product")
    suspend fun getProductCount(): Int
}