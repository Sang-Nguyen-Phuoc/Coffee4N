package com.example.coffee4n.repository

import android.view.Display.Mode
import com.example.coffee4n.model.Order
import com.example.coffee4n.model.OrderItem
import com.example.coffee4n.model.Product
import com.example.coffee4n.session.Models
import com.example.coffee4n.ui.insights.DailyRevenue
import com.example.coffee4n.ui.insights.HourlyData
import com.example.coffee4n.ui.insights.OrderItemStat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.example.coffee4n.session.OwnerSession
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class OrderItemRepository(
    private val firebaseDatabase: FirebaseDatabase
) {
    private val orderItemRef = firebaseDatabase.getReference(OwnerSession.getReferencePath(model = Models.OrderItem))

    suspend fun addOrderItem(orderItem: OrderItem) {
        val snapshot = orderItemRef.get().await()
        val orderItems = snapshot.children.mapNotNull { it.getValue(OrderItem::class.java) }.toMutableList()
        val existingIndex = orderItems.indexOfFirst { it.id == orderItem.id && it.orderId == orderItem.orderId }
        if (existingIndex != -1) {
            orderItems[existingIndex] = orderItem
        } else {
            orderItems.add(orderItem)
        }
        orderItemRef.setValue(orderItems).await()
    }

    fun getOrderItemsByOrderId(orderId: Int): Flow<List<OrderItem>> = callbackFlow {
        val orderItemsReference = orderItemRef
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderItems = snapshot.children
                    .mapNotNull { it.getValue(OrderItem::class.java) }
                    .filter { it.orderId == orderId }
                trySend(orderItems).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        orderItemsReference.addValueEventListener(listener)
        awaitClose { orderItemsReference.removeEventListener(listener) }
    }

    suspend fun deleteOrderItemsByOrderId(orderId: Int) {
        val snapshot = orderItemRef.get().await()
        val orderItems = snapshot.children.mapNotNull { it.getValue(OrderItem::class.java) }
        val filteredItems = orderItems.filter { it.orderId != orderId }
        orderItemRef.setValue(filteredItems).await()
    }

    fun getMostOrderedItemsFlow(limit: Int = 5): Flow<List<OrderItemStat>> = callbackFlow {
        val orderItemsReference = orderItemRef
        val productsReference = firebaseDatabase.getReference(OwnerSession.getReferencePath(model = Models.Product))

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderItems = snapshot.children.mapNotNull { it.getValue(OrderItem::class.java) }
                val productCountMap = mutableMapOf<Int, Int>()
                orderItems.forEach { orderItem ->
                    val currentCount = productCountMap.getOrDefault(orderItem.productId, 0)
                    productCountMap[orderItem.productId] = currentCount + orderItem.quantity
                }

                productsReference.get().addOnSuccessListener { productSnapshot ->
                    val products = productSnapshot.children.mapNotNull {
                        it.getValue(Product::class.java)
                    }
                    val orderItemStats = productCountMap.map { (productId, count) ->
                        val product = products.find { it.id == productId }
                        OrderItemStat(
                            productName = product?.name ?: "Unknown Product #$productId",
                            count = count
                        )
                    }.sortedByDescending { it.count }
                        .take(limit)
                    trySend(orderItemStats).isSuccess
                }.addOnFailureListener { exception ->
                    close(exception)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        orderItemsReference.addValueEventListener(listener)
        awaitClose { orderItemsReference.removeEventListener(listener) }
    }
}