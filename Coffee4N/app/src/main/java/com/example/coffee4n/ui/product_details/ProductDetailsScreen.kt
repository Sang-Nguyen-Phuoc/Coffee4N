package com.example.coffee4n.ui.product_details

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.coffee4n.R
import com.example.coffee4n.model.Product
import com.example.coffee4n.navigation.Destinations
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    navController: NavController,
    productId: Int,
    parentNavController: NavController,
    viewModel: ProductDetailsViewModel = viewModel()
) {
    val context = LocalContext.current
    val productState by viewModel.productState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var quantity by remember { mutableStateOf(1) }
    val scrollState = rememberScrollState()

    // Animations
    val addToCartScale = animateFloatAsState(
        targetValue = if (productState.isAddingToCart) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    LaunchedEffect(productState.showSnackbar) {
        if (productState.showSnackbar) {
            val message = productState.snackbarMessage ?: productState.error ?: "Action completed"
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    // Loading state
    if (productState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                color = Color(239, 83, 80),
                strokeWidth = 4.dp
            )
        }
        return
    }

    // Error state
    if (productState.error != null && !productState.showSnackbar) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Error",
                    tint = Color(239, 83, 80),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = productState.error ?: "Unknown error",
                    fontSize = 16.sp,
                    color = Color.Gray
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
        return
    }

    val product = productState.product ?: return


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF3E0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Enhanced Product Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(Color.White)
            ) {
                if (!product.imageUrl.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(model = product.imageUrl),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_coffee),
                        contentDescription = product.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(48.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                // Gradient overlay for better contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.7f),
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                ),
                                startY = 0f,
                                endY = 1000f
                            )
                        )
                )

                // Best Seller Badge - Enhanced
                if (product.isBestSeller) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFFFD700)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Best Seller",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }


            }

            // Product Info Section - Enhanced
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color(0xFFFAF3E0)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Product Name and Quantity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            // Category
                            Surface(
                                modifier = Modifier.padding(top = 8.dp),
                                color = Color(239, 83, 80).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = viewModel.getCategoryName(product.categoryId),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = Color(239, 83, 80),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Quantity Selector - Enhanced
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                IconButton(
                                    onClick = { if (quantity > 1) quantity-- },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Remove,
                                        contentDescription = "Decrease",
                                        tint = Color(239, 83, 80)
                                    )
                                }

                                Text(
                                    text = quantity.toString(),
                                    modifier = Modifier
                                        .width(40.dp)
                                        .padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(
                                    onClick = {
                                        if (quantity < product.stockQuantity) quantity++
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Increase",
                                        tint = Color(239, 83, 80)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stock Status and In Cart Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = if (product.stockQuantity > 0)
                                Color(0xFF4CAF50).copy(alpha = 0.1f)
                            else
                                Color.Red.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (product.stockQuantity > 0)
                                    "In Stock (${product.stockQuantity})"
                                else
                                    "Out of Stock",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = if (product.stockQuantity > 0)
                                    Color(0xFF4CAF50)
                                else
                                    Color.Red,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (productState.isInCart) {
                            Surface(
                                color = Color(0xFF2196F3).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "In Cart",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = Color(0xFF2196F3),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Price and Rating Row - Enhanced
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Price",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "$${String.format("%.2f", product.price)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(239, 83, 80)
                            )
                        }

                        // Rating - Enhanced
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFF8E1)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "4.5",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF795548)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(128)",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Description Section - Enhanced
                    Text(
                        text = "Description",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = product.description,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = Color(0xFF424242)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Additional Information - Enhanced
                    Text(
                        text = "Additional Information",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            EnhancedInfoRow(
                                icon = Icons.Default.Category,
                                title = "Category",
                                value = viewModel.getCategoryName(product.categoryId),
                                iconTint = Color(239, 83, 80)
                            )

                            Divider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color(0xFFE0E0E0)
                            )

                            EnhancedInfoRow(
                                icon = Icons.Default.Inventory2,
                                title = "Stock",
                                value = "${product.stockQuantity} items",
                                iconTint = if (product.stockQuantity > 0)
                                    Color(0xFF4CAF50)
                                else
                                    Color.Red
                            )

                            Divider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color(0xFFE0E0E0)
                            )

                            EnhancedInfoRow(
                                icon = Icons.Default.LocalShipping,
                                title = "Delivery",
                                value = "Same Day Delivery",
                                iconTint = Color(0xFF2196F3)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Similar Products Section
                    SimilarProductsSection(
                        similarProducts = productState.similarProducts,
                        onProductClick = { productId ->
                            navController.navigate(Destinations.productDetails(productId))
                        }
                    )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Similar Products Section
                        SimilarProductsSection(
                            similarProducts = productState.similarProducts,
                            onProductClick = { productId ->
                                navController.navigate(Destinations.productDetails(productId))
                            }
                        )

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }



        // Back button - Enhanced
        FloatingActionButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp)
                .align(Alignment.TopStart),
            containerColor = Color.White.copy(alpha = 0.9f),
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }

        // Favorite button - Enhanced
        FloatingActionButton(
            onClick = { viewModel.toggleFavorite(product) },
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp)
                .align(Alignment.TopEnd),
            containerColor = Color.White.copy(alpha = 0.9f),
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            if (productState.isUpdatingFavorite) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(239, 83, 80),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = if (productState.isFavorite) Icons.Default.Favorite
                    else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (productState.isFavorite) Color(239, 83, 80) else Color.Gray
                )
            }
        }
        // Enhanced Bottom Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Price",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "$${String.format("%.2f", product.price * quantity)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(239, 83, 80)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                AnimatedVisibility(visible = productState.isInCart) {
                    FilledTonalButton(
                        onClick = { navController.navigate(Destinations.CART) },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFE3F2FD),
                            contentColor = Color(0xFF1976D2)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Cart")
                    }
                }

                Button(
                    onClick = { viewModel.addToCart(product, quantity) },
                    enabled = product.stockQuantity > 0 && !productState.isAddingToCart,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .scale(addToCartScale.value),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(239, 83, 80),
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (productState.isAddingToCart) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (productState.isInCart) Icons.Default.Add
                                else Icons.Default.ShoppingCart,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (productState.isInCart) "Add More" else "Add to Cart",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Login Dialog
    if (productState.showLoginDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLoginDialog() },
            title = { Text("Login Required") },
            text = { Text("You need to login to add items to your cart.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissLoginDialog()
                        parentNavController.navigate(Destinations.LOGIN)
                    }
                ) {
                    Text("Login")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLoginDialog() }) {
                    Text("Cancel")
                }
            }
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .padding(bottom = 90.dp)
        )
    }

}

@Composable
fun EnhancedInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    iconTint: Color = Color.Gray
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = iconTint.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
fun SimilarProductsSection(
    similarProducts: List<Product>,
    onProductClick: (Int) -> Unit
) {
    if (similarProducts.isNotEmpty()) {
        Text(
            text = "Similar Products",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(
                items = similarProducts,
                key = { product -> product.id }
            ) { product ->
                SimilarProductCard(
                    product = product,
                    onClick = { onProductClick(product.id) }
                )
            }
        }
    }
}

@Composable
fun SimilarProductCard(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                if (!product.imageUrl.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(model = product.imageUrl),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_coffee),
                        contentDescription = product.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "$${String.format("%.2f", product.price)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(239, 83, 80),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}