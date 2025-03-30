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

    @Query("DELETE FROM product")
    suspend fun deleteAllProducts()

    @Query("DELETE FROM product WHERE id = :productId")
    suspend fun deleteProductById(productId: Int)

    @Query("UPDATE product SET name = :name, description = :description, price = :price, category = :category WHERE id = :productId")
    suspend fun updateProductById(productId: Int, name: String, description: String, price: Double, category: String)
}