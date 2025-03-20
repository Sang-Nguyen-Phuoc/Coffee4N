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
        return orderItemDao.getOrderItemsByOrder(orderId)
    }

    private suspend fun syncOrderItemsFromRemote(orderId: Int) {
        val snapshot = firebaseDatabase.getReference("orderitems").child(orderId.toString()).get().await()
        val orderItems = snapshot.children.mapNotNull { it.getValue(OrderItem::class.java) }
        orderItems.forEach { orderItemDao.insertOrderItem(it) }
    }

    suspend fun addOrderItem(orderItem: OrderItem) {
        orderItemDao.insertOrderItem(orderItem)
        firebaseDatabase.getReference("orderitems").child(orderItem.orderId.toString()).child(orderItem.id.toString()).setValue(orderItem).await()
    }

    suspend fun deleteOrderItem(id: Int, orderId: Int) {
        orderItemDao.deleteOrderItem(id)
        firebaseDatabase.getReference("orderitems").child(orderId.toString()).child(id.toString()).removeValue().await()
    }

    fun getOrderItemsFlow(orderId: Int): Flow<List<OrderItem>> = flow {
        emit(orderItemDao.getOrderItemsByOrder(orderId))
        syncOrderItemsFromRemote(orderId)
        emit(orderItemDao.getOrderItemsByOrder(orderId))
    }
}