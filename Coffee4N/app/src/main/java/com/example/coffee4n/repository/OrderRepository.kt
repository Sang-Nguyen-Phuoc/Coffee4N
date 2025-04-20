package com.example.coffee4n.repository

import com.example.coffee4n.model.Order
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val firebaseDatabase: FirebaseDatabase
) {
    suspend fun addOrder(order: Order) {
        firebaseDatabase.getReference("orders").child(order.id.toString()).setValue(order).await()
    }

    fun getOrdersByUser(userId: Int): Flow<List<Order>> = callbackFlow {
        val ordersReference = firebaseDatabase.getReference("orders")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = snapshot.children.mapNotNull { it.getValue(Order::class.java) }
                    .filter { it.userId == userId }
                trySend(orders).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ordersReference.addValueEventListener(listener)
        awaitClose { ordersReference.removeEventListener(listener) }
    }

    suspend fun updateOrderStatus(orderId: Int, status: String) {
        firebaseDatabase.getReference("orders").child(orderId.toString())
            .child("status").setValue(status).await()
    }

    suspend fun deleteOrder(orderId: Int) {
        firebaseDatabase.getReference("orders").child(orderId.toString()).removeValue().await()
    }
}