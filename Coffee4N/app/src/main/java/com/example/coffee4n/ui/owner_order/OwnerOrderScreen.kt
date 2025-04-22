package com.example.coffee4n.ui.owner_orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.coffee4n.model.Order
import com.example.coffee4n.model.Promotion
import com.example.coffee4n.repository.OrderItemRepository
import com.example.coffee4n.repository.OrderRepository
import com.example.coffee4n.repository.ProductRepository
import com.example.coffee4n.repository.PromotionRepository
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerOrderScreen(navController: NavController) {
    val firebaseDatabase = FirebaseDatabase.getInstance()
    val orderRepository = OrderRepository(firebaseDatabase)
    val orderItemRepository = OrderItemRepository(firebaseDatabase)
    val productRepository = ProductRepository(firebaseDatabase)
    val promotionRepository = PromotionRepository(firebaseDatabase)
    val viewModel: OwnerOrderViewModel = viewModel(
        factory = OwnerOrderViewModelFactory(
            orderRepository,
            orderItemRepository,
            productRepository,
            promotionRepository
        )
    )
    val state by viewModel.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Show Snackbar for success messages
    state.successMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
        }
    }
    state.promotionSuccessMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearPromotionSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Orders - Promotions Management",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF313131)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFF9F2ED)
                    )
                )
                TabRow(
                    selectedTabIndex = state.selectedTab,
                    containerColor = Color(0xFFF9F2ED),
                    contentColor = Color(0xFFC67C4E)
                ) {
                    Tab(
                        selected = state.selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        text = {
                            Text(
                                "Orders",
                                color = if (state.selectedTab == 0) Color(0xFFC67C4E) else Color(0xFF8D7A55)
                            )
                        }
                    )
                    Tab(
                        selected = state.selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        text = {
                            Text(
                                "Promotions",
                                color = if (state.selectedTab == 1) Color(0xFFC67C4E) else Color(0xFF8D7A55)
                            )
                        }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF9F2ED)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxSize()
        ) {
            when (state.selectedTab) {
                0 -> OrderContent(viewModel, state)
                1 -> PromotionContent(viewModel, state)
            }
        }

        // Order detail dialog
        state.selectedOrderId?.let { orderId ->
            val order = state.filteredOrders.find { it.id == orderId }
            order?.let {
                OrderDetailDialog(
                    order = it,
                    orderItems = state.orderItems,
                    isLoadingItems = state.isLoadingOrderItems,
                    onDismiss = { viewModel.hideOrderDetails() }
                )
            }
        }

        // Add promotion dialog
        if (state.showAddPromotionDialog) {
            AddPromotionDialog(
                onDismiss = { viewModel.hideAddPromotionDialog() },
                onAddPromotion = { promotion ->
                    viewModel.addPromotion(promotion)
                }
            )
        }
    }
}


@Composable
fun OrderContent(viewModel: OwnerOrderViewModel, state: OwnerOrderState) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Status filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.filterStatus == null,
                    onClick = { viewModel.setFilterStatus(null) },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFC67C4E),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFFFFFFF),
                        labelColor = Color(0xFF313131)
                    ),
                    border = BorderStroke(1.dp, if (state.filterStatus == null) Color(0xFFC67C4E) else Color(0xFFB0BEC5))
                )
                FilterChip(
                    selected = state.filterStatus == "PENDING",
                    onClick = { viewModel.setFilterStatus("PENDING") },
                    label = { Text("Pending") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFC67C4E),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFFFFFFF),
                        labelColor = Color(0xFF313131)
                    ),
                    border = BorderStroke(1.dp, if (state.filterStatus == "PENDING") Color(0xFFC67C4E) else Color(0xFFB0BEC5))
                )
                FilterChip(
                    selected = state.filterStatus == "COMPLETE",
                    onClick = { viewModel.setFilterStatus("COMPLETE") },
                    label = { Text("Complete") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFC67C4E),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFFFFFFF),
                        labelColor = Color(0xFF313131)
                    ),
                    border = BorderStroke(1.dp, if (state.filterStatus == "COMPLETE") Color(0xFFC67C4E) else Color(0xFFB0BEC5))
                )
            }
            OutlinedButton(
                onClick = { viewModel.resetFilter() },
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFC67C4E)
                ),
                border = BorderStroke(1.dp, Color(0xFFC67C4E))
            ) {
                Text("Reset", fontSize = 14.sp)
            }
        }

        // Loading state for initial load
        AnimatedVisibility(
            visible = state.isLoading && state.currentOrderPage == 1,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .shadow(6.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFFFFFF),
                                        Color(0xFFF9F2ED)
                                    )
                                )
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFC67C4E),
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Loading orders...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8D7A55)
                        )
                    }
                }
            }
        }

        // Error state
        AnimatedVisibility(
            visible = !state.isLoading && state.error != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .shadow(6.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFFFFFF),
                                        Color(0xFFF9F2ED)
                                    )
                                )
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.error ?: "An error occurred",
                            color = Color(0xFFE57373),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Order list with pagination
        AnimatedVisibility(
            visible = !state.isLoading && state.error == null,
            enter = slideInVertically(initialOffsetY = { 200 }) + fadeIn(),
            exit = fadeOut()
        ) {
            if (state.filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFFFFFFF),
                                            Color(0xFFF9F2ED)
                                        )
                                    )
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No orders found",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF8D7A55)
                            )
                        }
                    }
                }
            } else {
                val listState = rememberLazyListState()
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredOrders) { order ->
                        OrderCard(
                            order = order,
                            isUpdating = state.updatingOrderId == order.id,
                            onMarkComplete = { viewModel.markOrderComplete(order.id) },
                            onClick = { viewModel.showOrderDetails(order.id) }
                        )
                    }

                    // Enhanced loading indicator for pagination
                    if (state.hasMoreOrders && state.isLoading) {
                        item {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp, horizontal = 16.dp)
                                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color(0xFFFFFFFF),
                                                        Color(0xFFF9F2ED)
                                                    )
                                                )
                                            ),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color(0xFFC67C4E),
                                            strokeWidth = 4.dp,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Loading more orders...",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF8D7A55)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Detect when the user scrolls to the end of the list
                LaunchedEffect(listState) {
                    snapshotFlow { listState.layoutInfo.visibleItemsInfo }
                        .collect { visibleItems ->
                            val lastVisibleItem = visibleItems.lastOrNull()
                            if (lastVisibleItem != null &&
                                lastVisibleItem.index >= state.filteredOrders.size - 1 &&
                                state.hasMoreOrders &&
                                !state.isLoading
                            ) {
                                viewModel.loadMoreOrders()
                            }
                        }
                }
            }
        }
    }
}


@Composable
fun PromotionContent(viewModel: OwnerOrderViewModel, state: OwnerOrderState) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar and add promotion button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Search by Code") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFFC67C4E),
                    unfocusedIndicatorColor = Color(0xFFB0BEC5),
                    focusedLabelColor = Color(0xFFC67C4E),
                    cursorColor = Color(0xFFC67C4E)
                )
            )
            Button(
                onClick = { viewModel.showAddPromotionDialog() },
                modifier = Modifier
                    .size(50.dp)
                    .shadow(4.dp, CircleShape),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Loading state
        AnimatedVisibility(
            visible = state.isLoadingPromotions,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = Color(0xFFC67C4E),
                    strokeWidth = 4.dp
                )
            }
        }

        // Error state
        AnimatedVisibility(
            visible = !state.isLoadingPromotions && state.promotionError != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = state.promotionError ?: "An error occurred",
                    color = Color(0xFFE57373),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Promotion list
        AnimatedVisibility(
            visible = !state.isLoadingPromotions && state.promotionError == null,
            enter = slideInVertically(initialOffsetY = { 200 }) + fadeIn(),
            exit = fadeOut()
        ) {
            if (state.filteredPromotions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No promotions found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8D7A55)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredPromotions) { promotion ->
                        PromotionCard(
                            promotion = promotion,
                            onDelete = { viewModel.deletePromotion(promotion.id) },
                            isDeleting = state.deletingPromotionId == promotion.id
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    isUpdating: Boolean,
    onMarkComplete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF9F2ED)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Order #${order.id}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF313131)
                )
                Text(
                    text = order.status,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (order.status == "PENDING") Color(0xFFE57373) else Color(0xFF4CAF50)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Date: ${
                    order.orderDate?.let {
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
                    } ?: "Unknown"
                }",
                fontSize = 14.sp,
                color = Color(0xFF8D7A55)
            )
            Text(
                text = "Total: $${"%.2f".format(order.totalAmount)}",
                fontSize = 14.sp,
                color = Color(0xFF8D7A55)
            )
            Text(
                text = "Delivery: ${order.deliveryMethod}",
                fontSize = 14.sp,
                color = Color(0xFF8D7A55)
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (order.status == "PENDING") {
                Button(
                    onClick = onMarkComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC67C4E),
                        disabledContainerColor = Color(0xFFB0BEC5)
                    ),
                    enabled = !isUpdating
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Mark as Complete",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderDetailDialog(
    order: Order,
    orderItems: List<Any>,
    isLoadingItems: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = true,
            enter = scaleIn() + fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F2ED))
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Order Details #${order.id}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF313131)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Date: ${
                            order.orderDate?.let {
                                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
                            } ?: "Unknown"
                        }",
                        fontSize = 14.sp,
                        color = Color(0xFF8D7A55)
                    )
                    Text(
                        text = "Total: $${"%.2f".format(order.totalAmount)}",
                        fontSize = 14.sp,
                        color = Color(0xFF8D7A55)
                    )
                    Text(
                        text = "Delivery: ${order.deliveryMethod}",
                        fontSize = 14.sp,
                        color = Color(0xFF8D7A55)
                    )
                    Text(
                        text = "Status: ${order.status}",
                        fontSize = 14.sp,
                        color = if (order.status == "PENDING") Color(0xFFE57373) else Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Items",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF313131)
                    )
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFFE0E0E0)
                    )
                    AnimatedVisibility(
                        visible = isLoadingItems,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFC67C4E),
                                strokeWidth = 4.dp
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = !isLoadingItems,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (orderItems.isEmpty()) {
                            Text(
                                text = "No items found",
                                fontSize = 14.sp,
                                color = Color(0xFF8D7A55)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(orderItems) { item ->
                                    OrderItemRow(item as OrderItemWithName)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFC67C4E)
                        )
                    ) {
                        Text(
                            text = "Close",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItemRow(item: OrderItemWithName) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = item.productName ?: "Unknown Product",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF313131)
            )
            Text(
                text = "Quantity: ${item.quantity}",
                fontSize = 14.sp,
                color = Color(0xFF8D7A55)
            )
        }
        Text(
            text = "$${"%.2f".format(item.price * item.quantity)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF313131)
        )
    }
}

@Composable
fun PromotionCard(
    promotion: Promotion,
    onDelete: () -> Unit,
    isDeleting: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF9F2ED)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Code: ${promotion.code}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF313131)
                )
                Text(
                    text = if (promotion.isValid()) "Active" else "Expired",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (promotion.isValid()) Color(0xFF4CAF50) else Color(0xFFE57373)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = promotion.description,
                fontSize = 14.sp,
                color = Color(0xFF8D7A55)
            )
            Text(
                text = "Discount: ${
                    if (promotion.discountType == "PERCENTAGE") "${promotion.discountValue}%"
                    else "$${promotion.discountValue}"
                }",
                fontSize = 14.sp,
                color = Color(0xFF8D7A55)
            )
            Text(
                text = "Start: ${
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(promotion.startDate)
                }",
                fontSize = 14.sp,
                color = Color(0xFF8D7A55)
            )
            Text(
                text = "End: ${
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(promotion.endDate)
                }",
                fontSize = 14.sp,
                color = Color(0xFF8D7A55)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE57373),
                    disabledContainerColor = Color(0xFFB0BEC5)
                ),
                enabled = !isDeleting
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Delete",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPromotionDialog(
    onDismiss: () -> Unit,
    onAddPromotion: (Promotion) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var discountType by remember { mutableStateOf("PERCENTAGE") }
    var discountValue by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = true,
            enter = scaleIn() + fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F2ED))
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Add Promotion",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF313131)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Code") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color(0xFFC67C4E),
                            unfocusedIndicatorColor = Color(0xFFB0BEC5),
                            focusedLabelColor = Color(0xFFC67C4E),
                            cursorColor = Color(0xFFC67C4E)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color(0xFFC67C4E),
                            unfocusedIndicatorColor = Color(0xFFB0BEC5),
                            focusedLabelColor = Color(0xFFC67C4E),
                            cursorColor = Color(0xFFC67C4E)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = discountType,
                            onValueChange = {},
                            label = { Text("Discount Type") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color(0xFFC67C4E),
                                unfocusedIndicatorColor = Color(0xFFB0BEC5),
                                focusedLabelColor = Color(0xFFC67C4E),
                                cursorColor = Color(0xFFC67C4E)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("PERCENTAGE") },
                                onClick = {
                                    discountType = "PERCENTAGE"
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("FIXED") },
                                onClick = {
                                    discountType = "FIXED"
                                    expanded = false
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = discountValue,
                        onValueChange = { discountValue = it },
                        label = { Text("Discount Value") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color(0xFFC67C4E),
                            unfocusedIndicatorColor = Color(0xFFB0BEC5),
                            focusedLabelColor = Color(0xFFC67C4E),
                            cursorColor = Color(0xFFC67C4E)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date (yyyy-MM-dd)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color(0xFFC67C4E),
                            unfocusedIndicatorColor = Color(0xFFB0BEC5),
                            focusedLabelColor = Color(0xFFC67C4E),
                            cursorColor = Color(0xFFC67C4E)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date (yyyy-MM-dd)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color(0xFFC67C4E),
                            unfocusedIndicatorColor = Color(0xFFB0BEC5),
                            focusedLabelColor = Color(0xFFC67C4E),
                            cursorColor = Color(0xFFC67C4E)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = Color(0xFFE57373),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB0BEC5)
                            )
                        ) {
                            Text(
                                text = "Cancel",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                try {
                                    val parsedDiscountValue = discountValue.toDoubleOrNull()
                                        ?: throw IllegalArgumentException("Invalid discount value")
                                    val parsedStartDate = try {
                                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(startDate)
                                            ?: throw IllegalArgumentException("Invalid start date")
                                    } catch (e: Exception) {
                                        throw IllegalArgumentException("Invalid start date format")
                                    }
                                    val parsedEndDate = try {
                                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(endDate)
                                            ?: throw IllegalArgumentException("Invalid end date")
                                    } catch (e: Exception) {
                                        throw IllegalArgumentException("Invalid end date format")
                                    }
                                    if (code.isBlank() || description.isBlank()) {
                                        throw IllegalArgumentException("Code and description cannot be empty")
                                    }
                                    if (parsedStartDate.after(parsedEndDate)) {
                                        throw IllegalArgumentException("Start date must be before end date")
                                    }
                                    val promotion = Promotion(
                                        id = 0, // Temporary ID, will be auto-generated in repository
                                        code = code,
                                        description = description,
                                        discountType = discountType,
                                        discountValue = parsedDiscountValue,
                                        startDate = parsedStartDate,
                                        endDate = parsedEndDate,
                                        isActive = true
                                    )
                                    onAddPromotion(promotion)
                                    // Clear inputs after successful submission
                                    code = ""
                                    description = ""
                                    discountType = "PERCENTAGE"
                                    discountValue = ""
                                    startDate = ""
                                    endDate = ""
                                    errorMessage = null
                                } catch (e: Exception) {
                                    errorMessage = e.message
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC67C4E)
                            )
                        ) {
                            Text(
                                text = "Add",
                                color = Color.White,
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
