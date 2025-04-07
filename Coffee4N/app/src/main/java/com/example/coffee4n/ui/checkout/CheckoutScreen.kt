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
import com.example.coffee4n.model.database.AppDatabase
import com.example.coffee4n.repository.CartItemRepository
import com.example.coffee4n.repository.OrderItemRepository
import com.example.coffee4n.repository.OrderRepository
import com.example.coffee4n.repository.ProductRepository
import com.example.coffee4n.repository.PromotionRepository
import com.example.coffee4n.ui.cart.CartItemWithProduct
import com.example.coffee4n.ui.cart.CartViewModel
import com.example.coffee4n.ui.cart.CartViewModelFactory
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(navController: NavController) {
    val context = LocalContext.current
    val userId = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        .getInt("userId", 0)

    // Khởi tạo RoomDB
    val database = AppDatabase.getDatabase(context)
    val cartItemRepository = CartItemRepository(
        cartItemDao = database.cartItemDao(),
        firebaseDatabase = FirebaseDatabase.getInstance()
    )
    val productRepository = ProductRepository(FirebaseDatabase.getInstance())
    val promotionRepository = PromotionRepository(
        promotionDao = database.promotionDao(),
        firebaseDatabase = FirebaseDatabase.getInstance()
    )
    val orderRepository = OrderRepository(
        orderDao = database.orderDao(),
        firebaseDatabase = FirebaseDatabase.getInstance()
    )
    val orderItemRepository = OrderItemRepository(
        orderItemDao = database.orderItemDao(),
        firebaseDatabase = FirebaseDatabase.getInstance()
    )

    // Khởi tạo CartViewModel
    val cartViewModel: CartViewModel = viewModel(
        factory = CartViewModelFactory(cartItemRepository, productRepository, userId)
    )
    val cartState by cartViewModel.state.collectAsState()

    // Khởi tạo CheckoutViewModel
    val checkoutViewModel: CheckoutViewModel = viewModel(
        factory = CheckoutViewModelFactory(
            cartViewModel = cartViewModel,
            promotionRepository = promotionRepository,
            orderRepository = orderRepository,
            orderItemRepository = orderItemRepository,
            userId = userId
        )
    )
    val checkoutState by checkoutViewModel.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Hiển thị thông báo thành công
    checkoutState.successMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            checkoutViewModel.clearSuccessMessage()
            delay(1000L)
            navController.navigate("home") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "CHECKOUT",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3E2723)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF3E2723),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF3E2723),
                    navigationIconContentColor = Color(0xFF3E2723)
                ),
                modifier = Modifier
                    .shadow(4.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5F5F5)
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
                        color = Color(0xFFD4A373),
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
                        color = Color(0xFF757575)
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
                                isOutOfStock = false
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

                    // Phần nhập voucher
                    OutlinedTextField(
                        value = checkoutState.voucherCode,
                        onValueChange = { checkoutViewModel.updateVoucherCode(it) },
                        label = { Text("Enter Voucher Code") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        enabled = !checkoutState.isApplyingVoucher,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD4A373),
                            unfocusedBorderColor = Color(0xFF757575),
                            focusedLabelColor = Color(0xFFD4A373),
                            cursorColor = Color(0xFFD4A373)
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
                            containerColor = Color(0xFFD4A373),
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
                    checkoutState.errorMessage?.let {
                        Text(
                            it,
                            color = Color(0xFFE57373),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Nút Checkout
                    Button(
                        onClick = { checkoutViewModel.showConfirmDialog() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 16.dp)
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD4A373),
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

        // Dialog xác nhận
        if (checkoutState.showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { checkoutViewModel.hideConfirmDialog() },
                title = { Text("Confirm Checkout") },
                text = { Text("Total price: $${"%.2f".format(checkoutState.finalTotal)}") },
                confirmButton = {
                    TextButton(onClick = {
                        checkoutViewModel.checkout {
                            checkoutViewModel.hideConfirmDialog()
                        }
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { checkoutViewModel.hideConfirmDialog() }) {
                        Text("No")
                    }
                }
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
            // Hình ảnh sản phẩm
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

            // Lớp phủ mờ
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
                // Thông tin sản phẩm
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

                // Tổng giá
                Text(
                    text = "$${"%.2f".format(item.product.price * item.cartItem.quantity)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4A373)
                )
            }

            // Hiển thị thông báo nếu sản phẩm hết hàng
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
                color = Color(0xFFD4A373)
            )
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: Double,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color(0xFF3E2723)
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