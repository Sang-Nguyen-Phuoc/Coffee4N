package com.example.coffee4n.repository

import com.google.firebase.database.FirebaseDatabase
import com.example.coffee4n.model.OrderItem
import com.example.coffee4n.model.database.OrderItemDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class OrderItemRepository(
    private val orderItemDao: OrderItemDao,
    private val firebaseDatabase: FirebaseDatabase
) {
    suspend fun getAllOrderItemsFromLocal(): List<OrderItem> {
        return orderItemDao.getAllOrderItems()
    }

    suspend fun getOrderItemsByOrder(orderId: Int): List<OrderItem> {
        val localItems = orderItemDao.getOrderItemsByOrder(orderId)
        if (localItems.isNotEmpty()) return localItems
        val snapshot = firebaseDatabase.getReference("orderitems").get().await()
        val orderItems = snapshot.children.mapNotNull { it.getValue(OrderItem::class.java) }
        val orderItemsForOrder = orderItems.filter { it.orderId == orderId }
        orderItemsForOrder.forEach { orderItemDao.insertOrderItem(it) }
        return orderItemsForOrder
    }

    suspend fun addOrderItem(orderItem: OrderItem) {
        orderItemDao.insertOrderItem(orderItem)
        val snapshot = firebaseDatabase.getReference("orderitems").get().await()
        val orderItems = snapshot.children.mapNotNull { it.getValue(OrderItem::class.java) }.toMutableList()
        val existingIndex = orderItems.indexOfFirst { it.id == orderItem.id && it.orderId == orderItem.orderId }
        if (existingIndex != -1) {
            orderItems[existingIndex] = orderItem
        } else {
            orderItems.add(orderItem)
        }
        firebaseDatabase.getReference("orderitems").setValue(orderItems).await()
    }

    suspend fun deleteOrderItem(id: Int, orderId: Int) {
        orderItemDao.deleteOrderItem(id)
        val snapshot = firebaseDatabase.getReference("orderitems").get().await()
        val orderItems = snapshot.children.mapNotNull { it.getValue(OrderItem::class.java) }.toMutableList()
        val updatedOrderItems = orderItems.filter { it.id != id || it.orderId != orderId }
        firebaseDatabase.getReference("orderitems").setValue(updatedOrderItems).await()
    }

    fun getOrderItemsFlow(orderId: Int): Flow<List<OrderItem>> = flow {
        val snapshot = firebaseDatabase.getReference("orderitems").get().await()
        val orderItems = snapshot.children.mapNotNull { it.getValue(OrderItem::class.java) }
        val orderItemsForOrder = orderItems.filter { it.orderId == orderId }
        orderItemsForOrder.forEach { orderItemDao.insertOrderItem(it) }
        emit(orderItemsForOrder)
    }
}