package com.example.coffee4n.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.coffee4n.model.CartItem

@Dao
interface CartItemDao {
    @Query("SELECT * FROM cartitem")
    suspend fun getAllCartItems(): List<CartItem>

    @Query("SELECT * FROM cartitem WHERE userId = :userId")
    suspend fun getCartItemsByUser(userId: Int): List<CartItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem)

    @Query("DELETE FROM cartitem WHERE id = :id")
    suspend fun deleteCartItem(id: Int)

    @Query("DELETE FROM cartitem WHERE userId = :userId")
    suspend fun deleteCartItemsByUser(userId: Int)
}