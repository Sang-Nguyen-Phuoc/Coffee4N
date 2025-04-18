package com.example.coffee4n.ui.cart

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.coffee4n.navigation.Destinations
import com.example.coffee4n.repository.CartItemRepository
import com.example.coffee4n.repository.ProductRepository
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController) {
    val context = LocalContext.current
    val userId = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        .getInt("userId", 0)

    if (userId == 0) {
        // Nếu userId = 0, hiển thị thông báo yêu cầu đăng nhập
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Please login to view your cart",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate(Destinations.LOGIN) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4A373)),
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
        // Nếu có userId, hiển thị giỏ hàng
        val cartItemRepository = CartItemRepository(
            firebaseDatabase = FirebaseDatabase.getInstance()
        )
        val productRepository = ProductRepository(FirebaseDatabase.getInstance())
        val viewModel: CartViewModel = viewModel(
            factory = CartViewModelFactory(cartItemRepository, productRepository, userId)
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
                    visible = state.isLoading,
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
                    visible = !state.isLoading && state.error == null && state.cartItems.isEmpty(),
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
                    visible = !state.isLoading && state.error == null && state.cartItems.isNotEmpty(),
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
                            items(state.cartItems) { item ->
                                CartItemCard(
                                    item = item,
                                    onUpdateQuantity = { newQty -> viewModel.updateQuantity(item, newQty) },
                                    onRemove = { viewModel.removeItem(item) },
                                    onUpdateNote = { newNote -> viewModel.updateCartItemNote(item, newNote) },
                                    isOutOfStock = state.outOfStockItems.contains(item.product.id)
                                )
                            }
                        }
                        SummaryCard(state.itemTotal, state.tax, state.total)
                        Button(
                            onClick = { navController.navigate(Destinations.CHECKOUT) },
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
}

@Composable
fun CartItemCard(
    item: CartItemWithProduct,
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit,
    onUpdateNote: (String?) -> Unit,
    isOutOfStock: Boolean
) {
    var showNoteDialog by remember { mutableStateOf(false) }

    if (showNoteDialog) {
        NoteDialog(
            currentNote = item.cartItem.note,
            onConfirm = { newNote ->
                onUpdateNote(newNote)
                showNoteDialog = false
            },
            onDismiss = { showNoteDialog = false }
        )
    }

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
                        maxLines = 1,
                        color = Color.White
                    )
                    item.cartItem.note?.takeIf { it.isNotBlank() }?.let { note ->
                        Text(
                            "Note: $note",
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic,
                            fontSize = 14.sp,
                            maxLines = 1,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { showNoteDialog = true }
                        )
                    } ?: Text(
                        "Add note",
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { showNoteDialog = true }
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
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                            Text(
                                "$${"%.2f".format(item.product.price * item.cartItem.quantity)}",
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
                        onClick = { onUpdateQuantity(item.cartItem.quantity - 1) }
                    )
                    Text(
                        "${item.cartItem.quantity}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    QuantityButton(
                        text = "+",
                        enabled = !isOutOfStock,
                        onClick = { onUpdateQuantity(item.cartItem.quantity + 1) }
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
fun NoteDialog(
    currentNote: String?,
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var note by remember { mutableStateOf(currentNote ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Note") },
        text = {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Special Request (e.g., No sugar)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(note.takeIf { it.isNotBlank() }) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
        Text(label, fontSize = 16.sp, fontWeight = fontWeight, color = color)
        Text(
            "$${"%.2f".format(value)}",
            fontSize = 16.sp,
            fontWeight = fontWeight,
            color = color
        )
    }
}