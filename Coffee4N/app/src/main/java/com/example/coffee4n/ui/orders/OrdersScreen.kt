package com.example.coffee4n.ui.orders

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.coffee4n.repository.OrderItemRepository
import com.example.coffee4n.repository.OrderRepository
import com.example.coffee4n.repository.ProductRepository
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(navController: NavController, parentNavController: NavController) {
    val context = LocalContext.current
    val userId = try {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getInt("userId", 0)
    } catch (e: Exception) {
        0
    }

    if (userId == 0) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAF3E0)),
            contentAlignment = Alignment.Center
        ) {
            LoginRequiredContent(parentNavController)
        }
    } else {
        val orderRepository = try {
            OrderRepository(FirebaseDatabase.getInstance())
        } catch (e: Exception) {
            null
        }
        val orderItemRepository = try {
            OrderItemRepository(FirebaseDatabase.getInstance())
        } catch (e: Exception) {
            null
        }
        val productRepository = try {
            ProductRepository(FirebaseDatabase.getInstance())
        } catch (e: Exception) {
            null
        }

        if (orderRepository == null || orderItemRepository == null || productRepository == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFAF3E0)),
                contentAlignment = Alignment.Center
            ) {
                ErrorContent("Failed to initialize repositories")
            }
            return
        }

        val viewModel: OrdersViewModel = viewModel(
            factory = OrdersViewModelFactory(orderRepository, orderItemRepository, productRepository, userId)
        )
        val state by viewModel.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        var showDatePicker by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAF3E0))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Enhanced Top Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            "My Orders",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        if (state.orders.isNotEmpty()) {
                            Surface(
                                color = Color(239, 83, 80).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${state.orders.size} orders",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = Color(239, 83, 80),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Enhanced Search Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    EnhancedDatePicker(
                        selectedDate = state.searchDate,
                        onDateSelected = { viewModel.updateSearchDate(it) },
                        onClear = { viewModel.updateSearchDate("") }
                    )
                }

                // Main Content
                when {
                    state.isLoading -> {
                        LoadingState()
                    }
                    state.error != null -> {
                        ErrorState(state.error ?: "An error occurred")
                    }
                    state.orders.isEmpty() -> {
                        EmptyOrdersState()
                    }
                    else -> {
                        OrdersList(
                            orders = state.orders,
                            orderItems = state.orderItems,
                            expandedOrders = state.expandedOrders,
                            onToggleExpand = { viewModel.toggleOrderExpansion(it) },
                            onCancel = { viewModel.cancelOrder(it) }
                        )
                    }
                }
            }

            // Snackbar
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )

            // Date Picker Dialog
            if (showDatePicker) {
                DatePickerModal(
                    onDateSelected = { viewModel.updateSearchDate(it) },
                    onDismiss = { showDatePicker = false }
                )
            }

            // Success message
            LaunchedEffect(state.successMessage) {
                state.successMessage?.let { message ->
                    snackbarHostState.showSnackbar(message)
                    viewModel.clearSuccessMessage()
                }
            }
        }
    }
}

@Composable
private fun LoginRequiredContent(navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            Icons.Default.Receipt,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(bottom = 16.dp),
            tint = Color(239, 83, 80)
        )

        Text(
            text = "Sign in to view your orders",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Track your orders and reorder your favorites",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate("login") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(28.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(239, 83, 80)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Login,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(239, 83, 80)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = Color.Red,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedDatePicker(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onClear: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate,
        onValueChange = { },
        label = { Text("Filter by date") },
        placeholder = { Text("dd/MM/yyyy") },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        trailingIcon = {
            Row {
                if (selectedDate.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear date",
                            tint = Color.Gray
                        )
                    }
                }
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "Select date",
                        tint = Color(239, 83, 80)
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(239, 83, 80),
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = Color(239, 83, 80),
            unfocusedLabelColor = Color.Gray
        ),
        shape = RoundedCornerShape(12.dp)
    )

    if (showDatePicker) {
        DatePickerModal(
            onDateSelected = {
                onDateSelected(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val formattedDate = sdf.format(Date(millis))
                        onDateSelected(formattedDate)
                    }
                }
            ) {
                Text("OK", color = Color(239, 83, 80))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = Color(239, 83, 80),
                todayDateBorderColor = Color(239, 83, 80)
            )
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(239, 83, 80),
            strokeWidth = 4.dp,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun ErrorState(error: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(239, 83, 80)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = Color.Red,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyOrdersState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Receipt,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp),
                tint = Color.Gray.copy(alpha = 0.5f)
            )
            Text(
                text = "No orders found",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Your order history will appear here",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun OrdersList(
    orders: List<OrderWithDetails>,
    orderItems: Map<Int, List<OrderItemWithProduct>>,
    expandedOrders: Set<Int>,
    onToggleExpand: (Int) -> Unit,
    onCancel: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(orders) { order ->
            EnhancedOrderCard(
                order = order,
                orderItems = orderItems[order.order.id] ?: emptyList(),
                isExpanded = expandedOrders.contains(order.order.id),
                onToggleExpand = { onToggleExpand(order.order.id) },
                onCancel = { onCancel(order.order.id) }
            )
        }
    }
}

@Composable
fun EnhancedOrderCard(
    order: OrderWithDetails,
    orderItems: List<OrderItemWithProduct>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCancel: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                onClick = {
                    isPressed = true
                    onToggleExpand()
                    isPressed = false
                }
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Order #${order.order.id}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge(order.order.status)
                    }
                    Text(
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            .format(order.order.orderDate),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color(239, 83, 80)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Total Amount",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        "$${String.format("%.2f", order.order.totalAmount)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(239, 83, 80)
                    )
                }

                Surface(
                    color = Color(239, 83, 80).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        order.order.deliveryMethod,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color(239, 83, 80),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = Color.LightGray
                    )

                    orderItems.forEach { item ->
                        EnhancedOrderItemRow(item)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (order.order.status == "PENDING") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onCancel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Cancel,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Cancel Order",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val statusColor = when (status) {
        "PENDING" -> Color(0xFFFFA726)
        "COMPLETED" -> Color(0xFF4CAF50)
        "CANCELLED" -> Color(0xFFE57373)
        else -> Color.Gray
    }

    Surface(
        color = statusColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = statusColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EnhancedOrderItemRow(item: OrderItemWithProduct) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.productName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    "$${"%.2f".format(item.orderItem.price)}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    " × ${item.orderItem.quantity}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Text(
            "$${"%.2f".format(item.orderItem.price * item.orderItem.quantity)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(239, 83, 80)
        )
    }
}