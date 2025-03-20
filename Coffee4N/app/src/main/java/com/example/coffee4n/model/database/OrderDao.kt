package com.example.coffee4n.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.coffee4n.model.Order

@Dao
interface OrderDao {
    @Query("SELECT * FROM `order`")
    suspend fun getAllOrders(): List<Order>

    @Insert
    suspend fun insertOrder(order: Order)

    @Query("SELECT * FROM `order` WHERE userId = :userId")
    suspend fun getOrdersByUser(userId: Int): List<Order>
}