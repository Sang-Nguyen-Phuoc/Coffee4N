package com.example.coffee4n.ui.owner_dashboard

import OwnerRepository
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.repository.EmployeeRepository
import com.example.coffee4n.repository.IngredientRepository
import com.example.coffee4n.repository.OrderRepository
import com.example.coffee4n.repository.TableRepository
import com.example.coffee4n.session.OwnerSession
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OwnerDashboardViewModel : ViewModel() {
    private val _state = MutableStateFlow(OwnerDashboardState())
    val state: StateFlow<OwnerDashboardState> = _state.asStateFlow()

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val employeeRepository = EmployeeRepository(firebaseDatabase)
    private val ingredientRepository = IngredientRepository(firebaseDatabase)
    private val tableRepository = TableRepository(firebaseDatabase)
    private val orderRepository = OrderRepository(firebaseDatabase)
    private val ownerRepository = OwnerRepository()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        // Load ingredient information to check low stock ingredients
        viewModelScope.launch {
            ingredientRepository.getIngredientsFlow().collectLatest { ingredients ->
                val lowStockIngredients = ingredients.count { it.quantity <= it.threshold }
                _state.value = _state.value.copy(lowStockItems = lowStockIngredients)
            }
        }

        viewModelScope.launch {
            employeeRepository.getEmployeesFlow().collectLatest { employees ->
                _state.value = _state.value.copy(employeesPresent = employees.size)
            }
        }

        // Get pending table requests
        viewModelScope.launch {
            tableRepository.getBookingTablesFlow().collectLatest { bookings ->
                val pendingRequests = bookings.count { it.status == "PENDING" }
                val confirmedRequests = bookings.count { it.status == "CONFIRMED" }
                _state.value = _state.value.copy(
                    pendingTableRequests = pendingRequests,
                    confirmedTableRequests = confirmedRequests
                )
            }
        }

        // Get booked tables count and total tables
        viewModelScope.launch {
            tableRepository.getTablesFlow().collectLatest { tables ->
                val bookedTablesCount = tables.count { it.status == "BOOKED" }
                val totalTablesCount = tables.size
                _state.value = _state.value.copy(
                    bookedTables = bookedTablesCount,
                    totalTables = totalTablesCount
                )
            }
        }

        viewModelScope.launch {
            val ownerId = OwnerSession.ownerId
            ownerRepository.getOwner(ownerId).collect { owner ->
                if (owner != null) {
                    _state.value = _state.value.copy(avatarUrl = owner.avatarUrl)
                }
            }
        }

        // Load orders count and recent activities
        viewModelScope.launch {
            orderRepository.getAllOrders(1, 10).collectLatest { orders ->
                _state.value = _state.value.copy(ordersCount = orders.size)
                loadRecentActivities(orders)
            }
        }
    }

    private fun loadRecentActivities(recentOrders: List<com.example.coffee4n.model.Order>) {
        viewModelScope.launch {
            val activities = mutableListOf<ActivityItem>()

            try {
                // Get low stock ingredients first (prioritizing inventory alerts)
                val ingredients = ingredientRepository.getIngredientsFlow().first()
                val lowStockItems = ingredients.filter { it.quantity <= it.threshold }
                    .sortedBy { it.quantity }
                    .take(2) // Show up to 2 low stock items

                lowStockItems.forEach { ingredient ->
                    val status = when {
                        ingredient.quantity == 0 -> "Out of stock"
                        else -> "Low in stock"
                    }
                    activities.add(
                        ActivityItem(
                            title = "$status: ${ingredient.name} (${ingredient.quantity}/${ingredient.threshold} ${ingredient.unit})",
                            time = "Critical",
                            icon = Icons.Default.Inventory,
                            color = if (ingredient.quantity == 0) Color(0xFFE57373) else Color(0xFFE38B73)
                        )
                    )
                }

                // Add recent orders to activities
                recentOrders.sortedByDescending { it.orderDate }
                    .take(4) // Show up to 4 recent orders
                    .forEach { order ->
                        val orderStatusIcon = when (order.status) {
                            "PENDING" -> Icons.Default.AccessTime
                            "PROCESSING" -> Icons.Default.Sync
                            "COMPLETED" -> Icons.Default.CheckCircle
                            "CANCELLED" -> Icons.Default.Cancel
                            else -> Icons.Default.ShoppingCart
                        }

                        val orderStatusColor = when (order.status) {
                            "PENDING" -> Color(0xFF90CAF9) // Light Blue
                            "PROCESSING" -> Color(0xFF64B5F6) // Blue
                            "COMPLETED" -> Color(0xFF81C784) // Green
                            "CANCELLED" -> Color(0xFFE57373) // Red
                            else -> Color(0xFF78909C) // Blue Grey
                        }

                        activities.add(
                            ActivityItem(
                                title = "Order #${order.id} - ${order.deliveryMethod} - $${String.format("%.2f", order.totalAmount)}",
                                time = getTimeAgo(order.orderDate.time),
                                icon = orderStatusIcon,
                                color = orderStatusColor
                            )
                        )
                    }

                // Add recent table bookings to activities
                val bookings = tableRepository.getBookingTablesFlow().first()
                val recentBookings = bookings
                    .sortedByDescending { it.id } // Sort by id since bookingTime is a string
                    .take(3) // Show up to 3 recent table bookings

                recentBookings.forEach { booking ->
                    val bookingStatusIcon = when (booking.status) {
                        "PENDING" -> Icons.Default.HourglassEmpty
                        "CONFIRMED" -> Icons.Default.EventAvailable
                        "CANCELLED" -> Icons.Default.EventBusy
                        else -> Icons.Default.EventSeat
                    }

                    val bookingStatusColor = when (booking.status) {
                        "PENDING" -> Color(0xFFFFA726) // Orange
                        "CONFIRMED" -> Color(0xFF66BB6A) // Green
                        "CANCELLED" -> Color(0xFFEF5350) // Red
                        else -> Color(0xFF78909C) // Blue Grey
                    }

                    activities.add(
                        ActivityItem(
                            title = "Table #${booking.tableId} - ${booking.customerName} (${booking.numberOfPeople} guests)",
                            time = formatBookingTime(booking.bookingTime),
                            icon = bookingStatusIcon,
                            color = bookingStatusColor
                        )
                    )
                }



                // Sort activities with modified priority - now include table bookings
                val sortedActivities = activities.sortedWith(compareBy(
                    // Primary sort: Critical items first
                    { it.time != "Critical" },
                    // Secondary sort: Pending bookings next
                    { activity ->
                        !activity.title.contains("Table") ||
                                !recentBookings.any {
                                    activity.title.contains("Table #${it.tableId}") &&
                                            it.status == "PENDING"
                                }
                    },
                    // Tertiary sort: Default order in the list
                    { activities.indexOf(it) }
                ))

                _state.value = _state.value.copy(
                    activities = sortedActivities.take(8), // Increased to show more activities
                    error = null
                )
                println("Updated activities: ${activities.size}")
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to load recent activities: ${e.message}"
                )
                println("Error loading activities: ${e.message}")
            }
        }
    }

    private fun formatBookingTime(bookingTimeStr: String): String {
        // Assuming bookingTime is stored in a format like "yyyy-MM-dd HH:mm" or another consistent format
        // This function formats it into a more readable form for display
        try {
            // You may need to adjust this parsing format based on how bookingTime is actually stored
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = inputFormat.parse(bookingTimeStr) ?: return bookingTimeStr

            val now = Date()
            val calendar = java.util.Calendar.getInstance()

            // Set calendar to today at 00:00
            calendar.time = now
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val today = calendar.time

            // Set calendar to tomorrow at 00:00
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
            val tomorrow = calendar.time

            // Set calendar to day after tomorrow at 00:00
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
            val dayAfterTomorrow = calendar.time

            return when {
                date < now -> "Past booking"
                date >= now && date < tomorrow -> "Today, ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)}"
                date >= tomorrow && date < dayAfterTomorrow -> "Tomorrow, ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)}"
                else -> SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(date)
            }
        } catch (e: Exception) {
            // If there's any error parsing the date, just return the original string
            return bookingTimeStr
        }
    }

    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000} minutes ago"
            diff < 86_400_000 -> "${diff / 3_600_000} hours ago"
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }
}