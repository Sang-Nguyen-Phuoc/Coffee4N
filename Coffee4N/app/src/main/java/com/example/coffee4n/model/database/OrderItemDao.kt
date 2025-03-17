package com.example.coffee4n.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.coffee4n.model.OrderItem

@Dao
interface OrderItemDao {
    @Query("SELECT * FROM orderitem")
    suspend fun getAllOrderItems(): List<OrderItem>

    @Query("SELECT * FROM orderitem WHERE orderId = :orderId")
    suspend fun getOrderItemsByOrder(orderId: Int): List<OrderItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(orderItem: OrderItem)

    @Query("DELETE FROM orderitem WHERE id = :id")
    suspend fun deleteOrderItem(id: Int)
}