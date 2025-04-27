package com.example.coffee4n.ui.cart

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.coffee4n.navigation.Destinations
import com.example.coffee4n.repository.CartItemRepository
import com.example.coffee4n.repository.ProductRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController) {
    val context = LocalContext.current
    val userId = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        .getInt("userId", 0)

    if (userId == 0) {
        // Login required screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAF3E0)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 16.dp),
                    tint = Color(239, 83, 80)
                )

                Text(
                    text = "Sign in to view your cart",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Keep track of your favorite items and check out faster",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { navController.navigate(Destinations.LOGIN) },
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
    } else {
        val cartItemRepository = CartItemRepository(
            firebaseDatabase = FirebaseDatabase.getInstance()
        )
        val productRepository = ProductRepository(FirebaseDatabase.getInstance())
        val viewModel: CartViewModel = viewModel(
            factory = CartViewModelFactory(cartItemRepository, productRepository, userId)
        )
        val state by viewModel.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

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
                            "My Cart",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        if (state.cartItems.isNotEmpty()) {
                            Surface(
                                color = Color(239, 83, 80),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "${state.cartItems.size}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Content
                when {
                    state.isLoading -> {
                        LoadingState()
                    }
                    state.error != null -> {
                        ErrorState(state.error ?: "An error occurred", navController)
                    }
                    state.cartItems.isEmpty() -> {
                        EmptyCartState(navController)
                    }
                    else -> {
                        CartContent(
                            state = state,
                            viewModel = viewModel,
                            snackbarHostState = snackbarHostState,
                            navController = navController
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

            // Success message
            LaunchedEffect(state.successMessage) {
                state.successMessage?.let { message ->
                    snackbarHostState.showSnackbar(message)
                }
            }
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
private fun ErrorState(error: String, navController: NavController) {
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
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(239, 83, 80)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Go Back")
            }
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
                text = "Looks like you haven't added anything to your cart yet",
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
                    Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Browse Products",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CartContent(
    state: CartState,
    viewModel: CartViewModel,
    snackbarHostState: SnackbarHostState,
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Items list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(state.cartItems) { item ->
                EnhancedCartItemCard(
                    item = item,
                    onUpdateQuantity = { newQty ->
                        viewModel.updateQuantity(item, newQty, snackbarHostState)
                    },
                    onRemove = { viewModel.removeItem(item) },
                    onUpdateNote = { newNote ->
                        viewModel.updateCartItemNote(item, newNote)
                    },
                    isOutOfStock = state.outOfStockItems.contains(item.product.id)
                )
            }
        }

        // Bottom section with summary and checkout
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
                // Summary
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
                            text = "$${String.format("%.2f", state.total)}",
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
                            text = "${state.cartItems.size} items",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color(239, 83, 80),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Checkout button
                Button(
                    onClick = { navController.navigate(Destinations.CHECKOUT) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(239, 83, 80),
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = state.cartItems.isNotEmpty() && state.outOfStockItems.isEmpty()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.ShoppingCartCheckout,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Proceed to Checkout",
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
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit,
    onUpdateNote: (String?) -> Unit,
    isOutOfStock: Boolean
) {
    var showNoteDialog by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 0.98f else 1f,
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
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isExpanded = !isExpanded
            },
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

                // Price and total
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
                        text = "×${item.cartItem.quantity}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$${String.format("%.2f", item.product.price * item.cartItem.quantity)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(239, 83, 80)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quantity controls
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decrease button
                    Surface(
                        shape = CircleShape,
                        color = if (item.cartItem.quantity > 1 && !isOutOfStock)
                            Color(239, 83, 80).copy(alpha = 0.1f)
                        else
                            Color.Gray.copy(alpha = 0.1f),
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(enabled = item.cartItem.quantity > 1 && !isOutOfStock) {
                                onUpdateQuantity(item.cartItem.quantity - 1)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = if (item.cartItem.quantity > 1 && !isOutOfStock)
                                    Color(239, 83, 80)
                                else
                                    Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = "${item.cartItem.quantity}",
                        modifier = Modifier
                            .width(40.dp)
                            .padding(horizontal = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Increase button
                    Surface(
                        shape = CircleShape,
                        color = if (!isOutOfStock && item.cartItem.quantity < item.product.stockQuantity)
                            Color(239, 83, 80).copy(alpha = 0.1f)
                        else
                            Color.Gray.copy(alpha = 0.1f),
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(
                                enabled = !isOutOfStock && item.cartItem.quantity < item.product.stockQuantity
                            ) {
                                onUpdateQuantity(item.cartItem.quantity + 1)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = if (!isOutOfStock && item.cartItem.quantity < item.product.stockQuantity)
                                    Color(239, 83, 80)
                                else
                                    Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Note button
                    IconButton(
                        onClick = { showNoteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Add note",
                            tint = if (item.cartItem.note != null)
                                Color(239, 83, 80)
                            else
                                Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Note display
                AnimatedVisibility(
                    visible = item.cartItem.note != null && item.cartItem.note!!.isNotBlank()
                ) {
                    Text(
                        text = "Note: ${item.cartItem.note}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    // Note dialog
    if (showNoteDialog) {
        EnhancedNoteDialog(
            currentNote = item.cartItem.note,
            onConfirm = { newNote ->
                onUpdateNote(newNote)
                showNoteDialog = false
            },
            onDismiss = { showNoteDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedNoteDialog(
    currentNote: String?,
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var note by remember { mutableStateOf(currentNote ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Special Request",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Add any special instructions for this item",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("e.g., No sugar, extra hot...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(239, 83, 80),
                        unfocusedBorderColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(note.takeIf { it.isNotBlank() }) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(239, 83, 80)
                )
            ) {
                Text("Save")
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