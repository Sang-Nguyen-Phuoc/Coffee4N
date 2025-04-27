package com.example.coffee4n.ui.orders

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
fun OrdersScreen(navController: NavController) {
    val context = LocalContext.current
    val userId = try {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getInt("userId", 0)
    } catch (e: Exception) {
        0
    }

    if (userId == 0) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Please login to view your orders",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B4E31)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate("login") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4E31)),
                    modifier = Modifier
                        .height(48.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Login",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Failed to initialize repositories",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE57373)
                )
            }
            return
        }

        val viewModel: OrdersViewModel = viewModel(
            factory = OrdersViewModelFactory(orderRepository, orderItemRepository, productRepository, userId)
        )
        val state by viewModel.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        var showDatePicker by remember { mutableStateOf(false) }

            Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "YOUR ORDERS",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B4E31)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                try {
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    // Handle navigation error silently
                                }
                            },
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF6B4E31),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFFFAF3E0),
                        titleContentColor = Color(0xFF6B4E31),
                        navigationIconContentColor = Color(0xFF6B4E31)
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .shadow(4.dp)
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color(0xFFF5E8C7)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.searchDate,
                        onValueChange = { },
                        label = { Text("Search by date (dd/MM/yyyy)") },
                        modifier = Modifier
                            .weight(1f),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "Select Date",
                                    tint = Color(0xFF6B4E31)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6B4E31),
                            unfocusedBorderColor = Color(0xFF8D7A55),
                            focusedLabelColor = Color(0xFF6B4E31),
                            unfocusedLabelColor = Color(0xFF8D7A55),
                            cursorColor = Color(0xFF6B4E31),
                            disabledBorderColor = Color(0xFF8D7A55),
                            disabledLabelColor = Color(0xFF8D7A55),
                            disabledTextColor = Color(0xFF6B4E31)
                        ),
                        enabled = false
                    )
                    AnimatedVisibility(
                        visible = state.searchDate.isNotEmpty(),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.updateSearchDate("") },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFE57373).copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Reset Filter",
                                tint = Color(0xFFE57373)
                            )
                        }
                    }
                }

                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState()
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    try {
                                        val selectedDate = datePickerState.selectedDateMillis
                                        if (selectedDate != null) {
                                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                            val formattedDate = sdf.format(Date(selectedDate))
                                            viewModel.updateSearchDate(formattedDate)
                                        }
                                    } catch (e: Exception) {
                                        // Handle date formatting error silently
                                    }
                                    showDatePicker = false
                                }
                            ) {
                                Text("OK", color = Color(0xFF6B4E31))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Cancel", color = Color(0xFF8D7A55))
                            }
                        },
                        colors = DatePickerDefaults.colors(
                            containerColor = Color(0xFFFAF3E0),
                            titleContentColor = Color(0xFF6B4E31),
                            headlineContentColor = Color(0xFF6B4E31),
                            selectedDayContainerColor = Color(0xFF6B4E31),
                            selectedDayContentColor = Color.White
                        )
                    ) {
                        DatePicker(
                            state = datePickerState,
                            modifier = Modifier.padding(16.dp),
                            colors = DatePickerDefaults.colors(
                                selectedDayContainerColor = Color(0xFF6B4E31),
                                selectedDayContentColor = Color.White,
                                todayContentColor = Color(0xFF6B4E31),
                                todayDateBorderColor = Color(0xFF6B4E31)
                            )
                        )
                    }
                }

                AnimatedVisibility(
                    visible = state.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = Color(0xFF6B4E31),
                            strokeWidth = 4.dp
                        )
                    }
                }

                AnimatedVisibility(
                    visible = !state.isLoading && state.error != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            state.error ?: "An error occurred",
                            color = Color(0xFFE57373),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                AnimatedVisibility(
                    visible = !state.isLoading && state.error == null && state.orders.isEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No orders found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8D7A55)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = !state.isLoading && state.error == null && state.orders.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.orders) { order ->
                            OrderCard(
                                order = order,
                                orderItems = state.orderItems[order.order.id] ?: emptyList(),
                                isExpanded = state.expandedOrders.contains(order.order.id),
                                onToggleExpand = { viewModel.toggleOrderExpansion(order.order.id) },
                                onCancel = {
                                    try {
                                        viewModel.cancelOrder(order.order.id)
                                    } catch (e: Exception) {
                                        // Handle cancel order error silently
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        state.successMessage?.let { message ->
            LaunchedEffect(message) {
                try {
                    snackbarHostState.showSnackbar(message)
                    viewModel.clearSuccessMessage()
                } catch (e: Exception) {
                    // Handle snackbar error silently
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: OrderWithDetails,
    orderItems: List<OrderItemWithProduct>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF3E0))
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFAF3E0),
                            Color(0xFFF5E8C7)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Order #${order.order.id}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B4E31)
                    )
                    Text(
                        try {
                            SimpleDateFormat("dd/MM/yyyy").format(order.order.orderDate)
                        } catch (e: Exception) {
                            "Invalid date"
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF8D7A55)
                    )
                }
                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF6B4E31).copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color(0xFF6B4E31),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    try {
                        "Total: $${"%.2f".format(order.order.totalAmount)}"
                    } catch (e: Exception) {
                        "Total: Invalid amount"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B4E31)
                )
                Text(
                    order.order.status,
                    fontSize = 14.sp,
                    color = when (order.order.status) {
                        "PENDING" -> Color(0xFFFFA726)
                        "COMPLETED" -> Color(0xFF4CAF50)
                        "CANCELLED" -> Color(0xFFE57373)
                        else -> Color(0xFFE57373)
                    },
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                try {
                    "Delivery: ${order.order.deliveryMethod}"
                } catch (e: Exception) {
                    "Delivery: Unknown"
                },
                fontSize = 14.sp,
                color = Color(0xFF8D7A55)
            )

            AnimatedVisibility(visible = order.order.status == "PENDING") {
                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .height(48.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE57373),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "Cancel Order",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(
                        color = Color(0xFF8D7A55).copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (orderItems.isEmpty()) {
                        Text(
                            "No items found",
                            fontSize = 14.sp,
                            color = Color(0xFF8D7A55),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        orderItems.forEach { item ->
                            OrderItemRow(item)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItemRow(item: OrderItemWithProduct) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                try {
                    item.productName
                } catch (e: Exception) {
                    "Unknown Product"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B4E31)
            )
            Text(
                try {
                    "Quantity: ${item.orderItem.quantity}"
                } catch (e: Exception) {
                    "Quantity: Unknown"
                },
                fontSize = 12.sp,
                color = Color(0xFF8D7A55)
            )
            Text(
                try {
                    "Price: $${"%.2f".format(item.orderItem.price)}"
                } catch (e: Exception) {
                    "Price: Unknown"
                },
                fontSize = 12.sp,
                color = Color(0xFF8D7A55)
            )
        }
        Text(
            try {
                "$${"%.2f".format(item.orderItem.price * item.orderItem.quantity)}"
            } catch (e: Exception) {
                "$0.00"
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B4E31)
        )
    }
}