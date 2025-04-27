package com.example.coffee4n.ui.checkout

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.coffee4n.model.Promotion
import com.example.coffee4n.navigation.Destinations
import com.example.coffee4n.repository.*
import com.example.coffee4n.ui.cart.CartItemWithProduct
import com.example.coffee4n.ui.cart.CartViewModel
import com.example.coffee4n.ui.cart.CartViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(navController: NavController) {
    val context = LocalContext.current
    val userId = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        .getInt("userId", 0)

    // Setup repositories and view models
    val firebaseDatabase = FirebaseDatabase.getInstance()
    val cartItemRepository = CartItemRepository(firebaseDatabase)
    val productRepository = ProductRepository(firebaseDatabase)
    val promotionRepository = PromotionRepository(firebaseDatabase)
    val orderRepository = OrderRepository(firebaseDatabase)
    val orderItemRepository = OrderItemRepository(firebaseDatabase)
    val userRepository = UserRepository(
        firebaseAuth = FirebaseAuth.getInstance(),
        firebaseDatabase = firebaseDatabase
    )

    val cartViewModel: CartViewModel = viewModel(
        factory = CartViewModelFactory(cartItemRepository, productRepository, userId)
    )
    val cartState by cartViewModel.state.collectAsState()

    val checkoutViewModel: CheckoutViewModel = viewModel(
        factory = CheckoutViewModelFactory(
            cartViewModel = cartViewModel,
            promotionRepository = promotionRepository,
            orderRepository = orderRepository,
            orderItemRepository = orderItemRepository,
            userRepository = userRepository,
            userId = userId
        )
    )
    val checkoutState by checkoutViewModel.state.collectAsState()

    var selectedDeliveryMethod by remember { mutableStateOf("PICKUP") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle error messages
    LaunchedEffect(checkoutState.errorMessage) {
        if (checkoutState.errorMessage != null) {
            errorMessage = checkoutState.errorMessage
            showErrorDialog = true
            checkoutViewModel.clearErrorMessage()
        }
    }

    // Handle success messages
    LaunchedEffect(checkoutState.successMessage) {
        checkoutState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            checkoutViewModel.clearSuccessMessage()
        }
    }

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
                        "Checkout",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Progress indicator
                    Surface(
                        color = Color(239, 83, 80).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Final Step",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color(239, 83, 80),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Content
            when {
                cartState.isLoading -> {
                    LoadingState()
                }
                cartState.error != null -> {
                    ErrorState(cartState.error ?: "An error occurred")
                }
                cartState.cartItems.isEmpty() -> {
                    EmptyCartState(navController)
                }
                else -> {
                    CheckoutContent(
                        cartState = cartState,
                        checkoutState = checkoutState,
                        selectedDeliveryMethod = selectedDeliveryMethod,
                        onDeliveryMethodChange = { selectedDeliveryMethod = it },
                        onVoucherCodeChange = { checkoutViewModel.updateVoucherCode(it) },
                        onApplyVoucher = { checkoutViewModel.applyVoucher() },
                        onCheckout = { checkoutViewModel.showConfirmDialog(selectedDeliveryMethod) }
                    )
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        )

        // Confirmation dialog
        if (checkoutState.showConfirmDialog) {
            ConfirmationDialog(
                finalTotal = checkoutState.finalTotal,
                onConfirm = {
                    checkoutViewModel.checkout(selectedDeliveryMethod) {
                        checkoutViewModel.hideConfirmDialog()
                        navController.navigate(Destinations.HOME) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        }
                    }
                },
                onDismiss = { checkoutViewModel.hideConfirmDialog() }
            )
        }

        // Error dialog
        if (showErrorDialog) {
            ErrorDialog(
                message = errorMessage ?: "An error occurred",
                onDismiss = { showErrorDialog = false }
            )
        }
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
private fun EmptyCartState(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp),
                tint = Color.Gray.copy(alpha = 0.5f)
            )
            Text(
                text = "Your cart is empty",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Add some items to your cart to continue",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { navController.navigate(Destinations.HOME) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(28.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(239, 83, 80)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Back to Home",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CheckoutContent(
    cartState: com.example.coffee4n.ui.cart.CartState,
    checkoutState: CheckoutState,
    selectedDeliveryMethod: String,
    onDeliveryMethodChange: (String) -> Unit,
    onVoucherCodeChange: (String) -> Unit,
    onApplyVoucher: () -> Unit,
    onCheckout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Cart items list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(cartState.cartItems) { item ->
                EnhancedCartItemCard(
                    item = item,
                    isOutOfStock = item.product.stockQuantity < item.cartItem.quantity
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                EnhancedSummaryCard(
                    itemTotal = cartState.itemTotal,
                    tax = cartState.tax,
                    total = cartState.total,
                    appliedPromotion = checkoutState.appliedPromotion,
                    finalTotal = checkoutState.finalTotal
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                DeliveryMethodSection(
                    selectedMethod = selectedDeliveryMethod,
                    onMethodChange = onDeliveryMethodChange
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                VoucherSection(
                    voucherCode = checkoutState.voucherCode,
                    isApplying = checkoutState.isApplyingVoucher,
                    onVoucherCodeChange = onVoucherCodeChange,
                    onApplyVoucher = onApplyVoucher
                )
            }
        }

        // Bottom checkout button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "$${String.format("%.2f", checkoutState.finalTotal)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(239, 83, 80)
                        )
                    }

                    // Items count
                    Surface(
                        color = Color(239, 83, 80).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${cartState.cartItems.size} items",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color(239, 83, 80),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onCheckout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(239, 83, 80),
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = cartState.cartItems.isNotEmpty()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Payment,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Confirm & Pay",
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
fun EnhancedCartItemCard(
    item: CartItemWithProduct,
    isOutOfStock: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image
            Surface(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(80.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = item.product.imageUrl.ifEmpty { "https://picsum.photos/200" }
                    ),
                    contentDescription = item.product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (isOutOfStock) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Out of Stock",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format("%.2f", item.product.price)}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "× ${item.cartItem.quantity}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                if (item.cartItem.note != null && item.cartItem.note!!.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Note: ${item.cartItem.note}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = "$${String.format("%.2f", item.product.price * item.cartItem.quantity)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(239, 83, 80)
            )
        }
    }
}

@Composable
fun EnhancedSummaryCard(
    itemTotal: Double,
    tax: Double,
    total: Double,
    appliedPromotion: Promotion?,
    finalTotal: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Order Summary",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            SummaryRow("Subtotal", itemTotal)
            SummaryRow("Tax (2%)", tax)

            if (appliedPromotion != null) {
                SummaryRow(
                    "Discount (${appliedPromotion.code})",
                    -(total - finalTotal),
                    color = Color(0xFF4CAF50)
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFFE0E0E0)
            )

            SummaryRow(
                "Total",
                finalTotal,
                fontWeight = FontWeight.Bold,
                color = Color(239, 83, 80)
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: Double,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = fontWeight,
            color = color
        )
        Text(
            text = "$${String.format("%.2f", value)}",
            fontSize = 14.sp,
            fontWeight = fontWeight,
            color = color
        )
    }
}

@Composable
private fun DeliveryMethodSection(
    selectedMethod: String,
    onMethodChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Delivery Method",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DeliveryOption(
                title = "Pickup",
                description = "Pick up at store",
                icon = Icons.Default.Store,
                isSelected = selectedMethod == "PICKUP",
                modifier = Modifier.weight(1f)
            ) {
                onMethodChange("PICKUP")
            }

            DeliveryOption(
                title = "Shipping",
                description = "Delivery to address",
                icon = Icons.Default.LocalShipping,
                isSelected = selectedMethod == "SHIPPING",
                modifier = Modifier.weight(1f)
            ) {
                onMethodChange("SHIPPING")
            }
        }
    }
}

@Composable
private fun DeliveryOption(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) Color(239, 83, 80).copy(alpha = 0.1f) else Color.White,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = if (isSelected) Color(239, 83, 80) else Color.LightGray
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) Color(239, 83, 80) else Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(239, 83, 80) else Color.Black
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoucherSection(
    voucherCode: String,
    isApplying: Boolean,
    onVoucherCodeChange: (String) -> Unit,
    onApplyVoucher: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Have a voucher?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = voucherCode,
                onValueChange = onVoucherCodeChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter voucher code") },
                enabled = !isApplying,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(239, 83, 80),
                    unfocusedBorderColor = Color.LightGray
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = onApplyVoucher,
                enabled = !isApplying && voucherCode.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(239, 83, 80),
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp)
            ) {
                if (isApplying) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Apply")
                }
            }
        }
    }
}

@Composable
private fun ConfirmationDialog(
    finalTotal: Double,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Confirm Order",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Your order total is:",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "$${String.format("%.2f", finalTotal)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(239, 83, 80)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Would you like to proceed with payment?",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(239, 83, 80)
                )
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Gray
                )
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Information Required",
                fontWeight = FontWeight.Bold
            )
        },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(239, 83, 80)
                )
            ) {
                Text("OK")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}