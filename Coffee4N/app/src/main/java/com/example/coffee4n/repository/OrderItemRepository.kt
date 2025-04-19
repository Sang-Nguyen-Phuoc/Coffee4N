package com.example.coffee4n.repository

import com.example.coffee4n.model.Order
import com.example.coffee4n.model.OrderItem
import com.example.coffee4n.model.Product
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class OrderItemRepository(
    private val firebaseDatabase: FirebaseDatabase
) {
    suspend fun addOrderItem(orderItem: OrderItem) {
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

    fun getMostOrderedItemsFlow(limit: Int = 5): Flow<List<OrderItemStat>> = callbackFlow {
        val orderItemsReference = firebaseDatabase.getReference("orderitems")
        val productsReference = firebaseDatabase.getReference("products")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Lấy tất cả order items
                val orderItems = snapshot.children.mapNotNull { it.getValue(OrderItem::class.java) }

                // Đếm số lượng mỗi sản phẩm được đặt hàng
                val productCountMap = mutableMapOf<Int, Int>()
                orderItems.forEach { orderItem ->
                    val currentCount = productCountMap.getOrDefault(orderItem.productId, 0)
                    productCountMap[orderItem.productId] = currentCount + orderItem.quantity
                }

                // Lấy danh sách sản phẩm từ Firebase để lấy tên sản phẩm
                productsReference.get().addOnSuccessListener { productSnapshot ->
                    val products = productSnapshot.children.mapNotNull {
                        it.getValue(Product::class.java)
                    }

                    // Tạo danh sách OrderItemStat
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