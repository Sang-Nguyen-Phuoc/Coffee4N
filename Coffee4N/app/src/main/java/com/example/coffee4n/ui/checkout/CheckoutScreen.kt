package com.example.coffee4n.ui.checkout

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

    // Show error dialog when errorMessage changes
    LaunchedEffect(checkoutState.errorMessage) {
        if (checkoutState.errorMessage != null) {
            errorMessage = checkoutState.errorMessage
            showErrorDialog = true
            checkoutViewModel.clearErrorMessage()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show success message as a Snackbar
    checkoutState.successMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            checkoutViewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "CHECKOUT",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B4E31)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
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
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFFAF3E0),
                contentColor = Color(0xFF6B4E31)
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Destinations.HOME) },
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Home",
                            tint = Color(0xFF6B4E31)
                        )
                    },
                    label = { Text("Home", color = Color(0xFF6B4E31)) }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { navController.navigate(Destinations.CART) },
                    icon = {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Cart",
                            tint = Color(0xFF6B4E31)
                        )
                    },
                    label = { Text("Cart", color = Color(0xFF6B4E31)) }
                )
            }
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
            AnimatedVisibility(
                visible = cartState.isLoading,
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
                visible = !cartState.isLoading && cartState.error != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        cartState.error ?: "An error occurred",
                        color = Color(0xFFE57373),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            AnimatedVisibility(
                visible = !cartState.isLoading && cartState.error == null && cartState.cartItems.isEmpty(),
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
                        text = "Your cart is empty",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8D7A55)
                    )
                }
            }

            AnimatedVisibility(
                visible = !cartState.isLoading && cartState.error == null && cartState.cartItems.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 16.dp)
                    ) {
                        items(cartState.cartItems) { item ->
                            CartItemCard(
                                item = item,
                                isOutOfStock = item.product.stockQuantity < item.cartItem.quantity
                            )
                        }
                    }

                    SummaryCard(
                        itemTotal = cartState.itemTotal,
                        tax = cartState.tax,
                        total = cartState.total,
                        appliedPromotion = checkoutState.appliedPromotion,
                        finalTotal = checkoutState.finalTotal
                    )

                    Text(
                        "Delivery Method",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B4E31),
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = selectedDeliveryMethod == "PICKUP",
                                onClick = { selectedDeliveryMethod = "PICKUP" },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF6B4E31),
                                    unselectedColor = Color(0xFF8D7A55)
                                )
                            )
                            Text(
                                "Pickup",
                                fontSize = 14.sp,
                                color = Color(0xFF6B4E31)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = selectedDeliveryMethod == "SHIPPING",
                                onClick = { selectedDeliveryMethod = "SHIPPING" },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF6B4E31),
                                    unselectedColor = Color(0xFF8D7A55)
                                )
                            )
                            Text(
                                "Shipping",
                                fontSize = 14.sp,
                                color = Color(0xFF6B4E31)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = checkoutState.voucherCode,
                        onValueChange = { checkoutViewModel.updateVoucherCode(it) },
                        label = { Text("Enter Voucher Code") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        enabled = !checkoutState.isApplyingVoucher,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6B4E31),
                            unfocusedBorderColor = Color(0xFF8D7A55),
                            focusedLabelColor = Color(0xFF6B4E31),
                            unfocusedLabelColor = Color(0xFF8D7A55),
                            cursorColor = Color(0xFF6B4E31)
                        )
                    )
                    Button(
                        onClick = { checkoutViewModel.applyVoucher() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6B4E31),
                            disabledContainerColor = Color(0xFFB0BEC5)
                        ),
                        enabled = !checkoutState.isApplyingVoucher
                    ) {
                        if (checkoutState.isApplyingVoucher) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Apply Voucher",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = { checkoutViewModel.showConfirmDialog(selectedDeliveryMethod) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 16.dp)
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6B4E31),
                            disabledContainerColor = Color(0xFFB0BEC5)
                        ),
                        enabled = cartState.cartItems.isNotEmpty()
                    ) {
                        Text(
                            "Proceed to Checkout",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Confirmation dialog
        if (checkoutState.showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { checkoutViewModel.hideConfirmDialog() },
                title = { Text("Confirm Checkout") },
                text = { Text("Total price: $${"%.2f".format(checkoutState.finalTotal)}") },
                confirmButton = {
                    TextButton(onClick = {
                        checkoutViewModel.checkout(selectedDeliveryMethod) {
                            checkoutViewModel.hideConfirmDialog()
                            navController.navigate(Destinations.HOME) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                            }
                        }
                    }) {
                        Text("Yes", color = Color(0xFF6B4E31))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { checkoutViewModel.hideConfirmDialog() }) {
                        Text("No", color = Color(0xFF8D7A55))
                    }
                },
                containerColor = Color(0xFFFAF3E0),
                titleContentColor = Color(0xFF6B4E31),
                textContentColor = Color(0xFF6B4E31)
            )
        }

        // Error dialog
        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = { Text("Information Required") },
                text = { Text(errorMessage ?: "An error occurred") },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = false }) {
                        Text("OK", color = Color(0xFF6B4E31))
                    }
                },
                containerColor = Color(0xFFFAF3E0),
                titleContentColor = Color(0xFF6B4E31),
                textContentColor = Color(0xFF6B4E31)
            )
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItemWithProduct,
    isOutOfStock: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(vertical = 8.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = item.product.imageUrl.ifEmpty { "https://fastly.picsum.photos/id/1011/200/200.jpg?hmac=ISwJXaLKDOtBGE_n3myoHUev_P_OH3zpWqLx0yHp0pY" }
                ),
                contentDescription = item.product.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clip(RoundedCornerShape(16.dp))
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.product.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${"%.2f".format(item.product.price)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Quantity: ${item.cartItem.quantity}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Text(
                    text = "$${"%.2f".format(item.product.price * item.cartItem.quantity)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B4E31)
                )
            }

            if (isOutOfStock) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE57373).copy(alpha = 0.1f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Out of Stock",
                        color = Color(0xFFE57373),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    itemTotal: Double,
    tax: Double,
    total: Double,
    appliedPromotion: Promotion?,
    finalTotal: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
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
                            Color(0xFFF8F1E9)
                        )
                    )
                )
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            SummaryRow("Item Total", itemTotal)
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
                color = Color(0xFFE0E0E0),
                thickness = 1.dp
            )
            SummaryRow(
                "Total",
                finalTotal,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B4E31)
            )
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: Double,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color(0xFF6B4E31)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = 16.sp,
            fontWeight = fontWeight,
            color = color
        )
        Text(
            "$${"%.2f".format(value)}",
            fontSize = 16.sp,
            fontWeight = fontWeight,
            color = color
        )
    }
}