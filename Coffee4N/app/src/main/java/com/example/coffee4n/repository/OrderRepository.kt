package com.example.coffee4n.repository

import com.example.coffee4n.model.Order
import com.example.coffee4n.ui.insights.*
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

    fun getRevenueByDayFlow(): Flow<List<DailyRevenue>> = callbackFlow {
        val ordersReference = firebaseDatabase.getReference("orders")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = mutableListOf<Order>()

                // Get all orders
                snapshot.children.forEach { orderSnapshot ->
                    val order = orderSnapshot.getValue(Order::class.java)
                    if (order != null) {
                        orders.add(order)
                    }
                }

                // Group orders by day of week and sum totalAmounts
                val calendar = Calendar.getInstance()
                val dayFormat = SimpleDateFormat("EEE", Locale.US) // Short day name (Mon, Tue, etc.)
                val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

                // Initialize revenue map with all days of the week
                val revenueByDay = mutableMapOf<String, Double>()
                dayNames.forEach { day -> revenueByDay[day] = 0.0 }

                // Calculate revenue for each day
                orders.forEach { order ->
                    val orderDate = order.orderDate?.time ?: return@forEach
                    calendar.timeInMillis = orderDate
                    val dayOfWeek = dayFormat.format(calendar.time)

                    val currentAmount = revenueByDay[dayOfWeek] ?: 0.0
                    revenueByDay[dayOfWeek] = currentAmount + order.totalAmount
                }

                // Convert to DailyRevenue objects and sort by day of week
                val dailyRevenue = dayNames.map { day ->
                    DailyRevenue(day, revenueByDay[day] ?: 0.0)
                }

                trySend(dailyRevenue).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ordersReference.addValueEventListener(listener)
        awaitClose { ordersReference.removeEventListener(listener) }
    }

    fun getPeakHoursFlow(): Flow<List<HourlyData>> = callbackFlow {
        val ordersReference = firebaseDatabase.getReference("orders")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = mutableListOf<Order>()

                // Get all orders
                snapshot.children.forEach { orderSnapshot ->
                    val order = orderSnapshot.getValue(Order::class.java)
                    if (order != null) {
                        orders.add(order)
                    }
                }

                // Define hour ranges
                val hourRanges = listOf(
                    "7-9 AM", "9-11 AM", "11-1 PM", "1-3 PM",
                    "3-5 PM", "5-7 PM", "7-9 PM", "9-11 PM"
                )

                // Initialize counts map
                val orderCountsByHour = mutableMapOf<String, Int>()
                hourRanges.forEach { range -> orderCountsByHour[range] = 0 }

                // Count orders by hour range
                val calendar = Calendar.getInstance()
                orders.forEach { order ->
                    val orderDate = order.orderDate?.time ?: return@forEach
                    calendar.timeInMillis = orderDate
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)

                    val hourRange = when {
                        hour in 7..8 -> "7-9 AM"
                        hour in 9..10 -> "9-11 AM"
                        hour in 11..12 -> "11-1 PM"
                        hour in 13..14 -> "1-3 PM"
                        hour in 15..16 -> "3-5 PM"
                        hour in 17..18 -> "5-7 PM"
                        hour in 19..20 -> "7-9 PM"
                        hour in 21..22 -> "9-11 PM"
                        else -> null
                    }

                    if (hourRange != null) {
                        val currentCount = orderCountsByHour[hourRange] ?: 0
                        orderCountsByHour[hourRange] = currentCount + 1
                    }
                }

                // Convert to HourlyData objects
                val hourlyData = hourRanges.map { range ->
                    HourlyData(range, orderCountsByHour[range] ?: 0)
                }

                trySend(hourlyData).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ordersReference.addValueEventListener(listener)
        awaitClose { ordersReference.removeEventListener(listener) }
    }
}