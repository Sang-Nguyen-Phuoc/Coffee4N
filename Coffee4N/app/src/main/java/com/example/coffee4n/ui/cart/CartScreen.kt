package com.example.coffee4n.ui.cart

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.coffee4n.navigation.Destinations
import com.example.coffee4n.repository.CartRepository
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController? = null) {
    val viewModel: CartViewModel = viewModel(
        factory = CartViewModelFactory(CartRepository(FirebaseDatabase.getInstance()))
    )
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "YOUR CART",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController?.popBackStack() },
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                ),
                modifier = Modifier
                    .shadow(4.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF8F1E9) // Màu nền ấm áp, sang trọng
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = Color(0xFFD4A373), // Màu nâu vàng sang trọng
                        strokeWidth = 4.dp
                    )
                }
            }
            state.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        state.error!!,
                        color = Color(0xFFE57373),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxSize()
                ) {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(state.cartItems) { item ->
                            CartItemCard(
                                item = item,
                                onUpdateQuantity = { newQty -> viewModel.updateQuantity(item.product.id, newQty) },
                                onRemove = { viewModel.removeItem(item.product.id) },
                                isOutOfStock = state.outOfStockItems.contains(item.product.id)
                            )
                        }
                    }
                    SummaryCard(state.itemTotal, state.tax, state.total)
                    Button(
                        onClick = { navController?.navigate(Destinations.CHECKOUT) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 12.dp)
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD4A373),
                            disabledContainerColor = Color(0xFFB0BEC5)
                        ),
                        enabled = state.cartItems.isNotEmpty() && state.outOfStockItems.isEmpty()
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

        state.successMessage?.let { message ->
            LaunchedEffect(message) {
                snackbarHostState.showSnackbar(message)
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit,
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
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Hình ảnh làm nền
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
                    .background(Color.Black.copy(alpha = 0.3f)) // Lớp phủ đen mờ
                    .clip(RoundedCornerShape(16.dp))
            )

            // Nội dung trên hình ảnh
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        item.product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 2,
                        color = Color.White // Màu trắng để nổi bật trên nền mờ
                    )
                    if (isOutOfStock) {
                        Text(
                            "Out of Stock",
                            color = Color(0xFFE57373),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Column {
                            Text(
                                "$${item.product.price}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f), // Màu trắng mờ
                                fontSize = 14.sp
                            )
                            Text(
                                "$${"%.2f".format(item.product.price * item.quantity)}",
                                color = Color(0xFFD4A373),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuantityButton(
                        text = "-",
                        enabled = !isOutOfStock,
                        onClick = { onUpdateQuantity(item.quantity - 1) }
                    )
                    Text(
                        "${item.quantity}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    QuantityButton(
                        text = "+",
                        enabled = !isOutOfStock,
                        onClick = { onUpdateQuantity(item.quantity + 1) }
                    )
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFE57373).copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = Color(0xFFE57373),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun QuantityButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .shadow(if (enabled) 4.dp else 0.dp, CircleShape)
            .background(
                if (enabled) Color(0xFFD4A373) else Color(0xFFB0BEC5),
                CircleShape
            )
            .clickable(enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SummaryCard(itemTotal: Double, tax: Double, total: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            SummaryRow("Item Total", itemTotal)
            SummaryRow("Tax (2%)", tax)
            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFFE0E0E0),
                thickness = 1.dp
            )
            SummaryRow("Total", total, FontWeight.Bold, Color(0xFFD4A373))
        }
    }
}

@Composable
fun SummaryRow(label: String, value: Double, fontWeight: FontWeight = FontWeight.Normal, color: Color = Color(0xFF3E2723)) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 16.sp, fontWeight = fontWeight, color = color)
        Text(
            "$${"%.2f".format(value)}",
            fontSize = 16.sp,
            fontWeight = fontWeight,
            color = color
        )
    }
}
