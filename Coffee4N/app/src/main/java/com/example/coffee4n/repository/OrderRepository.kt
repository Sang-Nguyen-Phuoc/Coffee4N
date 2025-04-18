package com.example.coffee4n.repository

import com.example.coffee4n.model.Order
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val firebaseDatabase: FirebaseDatabase
) {
    suspend fun addOrder(order: Order) {
        try {
            firebaseDatabase.getReference("orders").child(order.id.toString()).setValue(order).await()
        } catch (e: Exception) {
            throw Exception("Không thể thêm đơn hàng: ${e.message}")
        }
    }

    suspend fun getOrdersByUser(userId: Int): List<Order> {
        try {
            val snapshot = firebaseDatabase.getReference("orders").get().await()
            val orders = snapshot.children.mapNotNull { it.getValue(Order::class.java) }
            return orders.filter { it.userId == userId }
        } catch (e: Exception) {
            throw Exception("Không thể lấy đơn hàng: ${e.message}")
        }
    }
}