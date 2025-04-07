package com.example.coffee4n.repository

import com.google.firebase.database.FirebaseDatabase
import com.example.coffee4n.model.Order
import com.example.coffee4n.model.database.OrderDao
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val orderDao: OrderDao,
    private val firebaseDatabase: FirebaseDatabase
) {
    suspend fun addOrder(order: Order) {
        orderDao.insertOrder(order)
        val snapshot = firebaseDatabase.getReference("orders").get().await()
        val orders = snapshot.children.mapNotNull { it.getValue(Order::class.java) }.toMutableList()
        val existingIndex = orders.indexOfFirst { it.id == order.id }
        if (existingIndex != -1) {
            orders[existingIndex] = order
        } else {
            orders.add(order)
        }
        firebaseDatabase.getReference("orders").setValue(orders).await()
    }

    suspend fun getOrdersByUser(userId: Int): List<Order> {
        val localOrders = orderDao.getOrdersByUser(userId)
        if (localOrders.isNotEmpty()) return localOrders
        val snapshot = firebaseDatabase.getReference("orders").get().await()
        val orders = snapshot.children.mapNotNull { it.getValue(Order::class.java) }
        val userOrders = orders.filter { it.userId == userId }
        userOrders.forEach { orderDao.insertOrder(it) }
        return userOrders
    }
}