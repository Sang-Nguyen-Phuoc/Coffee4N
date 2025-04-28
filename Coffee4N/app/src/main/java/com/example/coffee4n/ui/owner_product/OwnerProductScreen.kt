package com.example.coffee4n.ui.owner_product

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.coffee4n.R
import com.example.coffee4n.model.Category
import com.example.coffee4n.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerProductScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: OwnerProductViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OwnerProductViewModel(context.applicationContext as Application) as T
            }
        }
    )

    val state by viewModel.state.collectAsState()
    var itemsToShow by remember { mutableStateOf(10) }

    val filteredProducts = state.products
        .filter { it.categoryId == state.selectedCategory || state.selectedCategory == 0 }
        .filter { it.name.contains(state.searchQuery, ignoreCase = true) }
        .let { list -> if (state.showBestSellers) list.filter { it.isBestSeller } else list }
        .sortedBy { if (state.sortAscending) it.price else -it.price }

    val displayProducts = filteredProducts.take(itemsToShow)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Product Management",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF313131)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF9F2ED)
                ),
                actions = {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFC67C4E),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .size(48.dp)
                            .offset(x = (-12).dp)
                    ) {
                        IconButton(onClick = { viewModel.showAddDialog() }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Product",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        },
        containerColor = Color(0xFFF9F2ED)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFC67C4E)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Search Bar
                    TextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search coffee") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp)),
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    viewModel.updateSearchQuery("")
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Clear search",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Tabs
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val categories = listOf(Category(0, "All", "")) + state.categories
                        items(categories) { category ->
                            val isSelected = category.id == state.selectedCategory
                            Button(
                                onClick = { viewModel.updateCategory(category.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFFC67C4E) else Color.Gray,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Text(category.name, fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sort & Best Seller Toggle
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray)
                                .clickable { viewModel.toggleSortOrder() }
                                .padding(vertical = 8.dp, horizontal = 14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Sort, contentDescription = "Sort")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (state.sortAscending) "Low to High" else "High to Low")
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Best Sellers", color = Color(0xFF313131))
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = state.showBestSellers,
                                onCheckedChange = { viewModel.toggleBestSellers() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFC67C4E),
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.LightGray
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Products count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Total Products:",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${filteredProducts.size}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF5A9280)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    // Product List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(displayProducts) { product ->
                            ProductCard(
                                product = product,
                                onEditClick = { viewModel.showEditDialog(product) },
                                onDeleteClick = { viewModel.showDeleteConfirmation(product) }
                            )
                        }
                        if (displayProducts.size < filteredProducts.size) {
                            item {
                                Button(
                                    onClick = {
                                        itemsToShow += 10
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFC67C4E),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(text = "Load More")
                                }
                            }
                        }
                    }
                }

                // Hiển thị dialog nếu showAddEditDialog là true
                if (state.showAddEditDialog) {
                    AddEditProductDialog(
                        isEdit = state.isEdit,
                        name = state.nameInput,
                        description = state.descriptionInput,
                        price = state.priceInput,
                        isBestSeller = state.isBestSellerInput,
                        categoryId = state.categoryIdInput,
                        stockQuantity = state.stockQuantityInput,
                        costPrice = state.costPriceInput,
                        imageUrl = state.imageUrl,
                        categoriesList = state.categories,
                        onNameChange = { viewModel.updateNameInput(it) },
                        onDescriptionChange = { viewModel.updateDescriptionInput(it) },
                        onPriceChange = { viewModel.updatePriceInput(it) },
                        onBestSellerChange = { viewModel.updateIsBestSellerInput(it) },
                        onCategoryChange = { viewModel.updateCategoryIdInput(it) },
                        onStockQuantityChange = { viewModel.updateStockQuantityInput(it) },
                        onCostPriceChange = { viewModel.updateCostPriceInput(it) },
                        onImageUrlChange = { viewModel.updateImageUrl(it) },
                        onSave = { viewModel.saveProduct() },
                        onDismiss = { viewModel.hideDialog() }
                    )
                }

                if (state.showDeleteConfirmation) {
                    state.selectedProduct?.let { product ->
                        ConfirmDeleteProductDialog(
                            productName = product.name,
                            onConfirm = {
                                viewModel.confirmDeleteProduct()
                            },
                            onDismiss = { viewModel.hideDialog() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(8.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD8E2DC))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Ảnh sản phẩm + Ngôi sao nếu là Best Seller
            Box(
                modifier = Modifier.size(80.dp)
            ) {
                // Box chứa ảnh + border
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                ) {
                    if (!product.imageUrl.isNullOrEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = product.imageUrl),
                            contentDescription = product.name,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_coffee),
                            contentDescription = product.name,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Ngôi sao Best Seller nằm trên border
                if (product.isBestSeller) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Best Seller",
                        tint = Color(red = 214, green = 193, blue = 6),
                        modifier = Modifier
                            .size(34.dp)
                            .align(Alignment.TopStart)
                            .offset(x = -16.dp, y = -16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF5A9280), fontWeight = FontWeight.Bold)
                )

                Text(
                    text = "$${product.price}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFC67C4E), fontWeight = FontWeight.Medium)
                )

                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .padding(0.dp)
                        .background(Color(0xFFECE4DB))
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFFC67C4E),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Delete button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .padding(0.dp)
                        .background(Color(0xFFFAE1DD))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE38B73),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}