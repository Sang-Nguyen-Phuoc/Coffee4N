package com.example.coffee4n.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.coffee4n.R
import com.example.coffee4n.model.Product
import com.example.coffee4n.model.database.AppDatabase
import com.example.coffee4n.repository.ProductRepository
import kotlinx.coroutines.flow.StateFlow


@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val productDao = AppDatabase.getDatabase(context).productDao()
    val viewModel: HomeViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(ProductRepository(productDao)) as T
            }
        }
    )
    viewModel.insertSampleProducts()
    val products = viewModel.products.collectAsState()

    val categories = listOf("All", "Espresso", "Latte", "Cappuccino", "Mocha", "Tea")

    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var sortAscending by remember { mutableStateOf(true) }
    var showBestSellers by remember { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }


    val filteredProducts = products.value
        .filter { it.category == selectedCategory || selectedCategory == "All" }
        .filter { it.name.contains(searchQuery, ignoreCase = true) } //
        .let { list -> if (showBestSellers) list.filter { it.isBestSeller } else list } //
        .sortedBy { if (sortAscending) it.price else -it.price } //

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Location Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = "Location")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Location, Bilzen, Tanjungbalai", fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search coffee") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(16.dp))

//        // Promo Banner
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(150.dp)
//                .clip(RoundedCornerShape(8.dp))
//                .background(Color(0xFFEDEDED))
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Column {
//                    Text(
//                        "PROMO",
//                        color = Color.Red,
//                        fontSize = 12.sp,
//                        modifier = Modifier
//                            .background(Color.White, RoundedCornerShape(4.dp))
//                            .padding(horizontal = 4.dp)
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Text("Buy one get one FREE", fontSize = 20.sp, fontWeight = FontWeight.Bold)
//                }
//                Spacer(modifier = Modifier.weight(1f))
//                Image(
//                    painter = painterResource(id = R.drawable.ic_coffee),
//                    contentDescription = "Promo Coffee",
//                    modifier = Modifier.size(80.dp)
//                )
//            }
//        }
//        Spacer(modifier = Modifier.height(16.dp))

        // Category Tabs
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                Button(
                    onClick = { selectedCategory = category },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(red = 52, green = 235, blue = 174) else Color.Gray,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(category, fontSize = 14.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Filter Options (Sort by Price & Best Sellers)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
                    .clickable { expanded = true }
                    .padding(vertical = 8.dp, horizontal = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt, // Icon lọc
                        contentDescription = "Sort Filter",
                        modifier = Modifier.size(24.dp) // Kích thước icon
                    )
                    Spacer(modifier = Modifier.width(4.dp)) // Khoảng cách giữa icon và text
                    Text(
                        text = if (sortAscending) "Low to High" else "High to Low",
                        fontSize = 14.sp, // Kích thước chữ tùy chỉnh
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Low to High") },
                    onClick = {
                        sortAscending = true
                        expanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("High to Low") },
                    onClick = {
                        sortAscending = false
                        expanded = false
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 🌟 Best Seller Toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Best Sellers",
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = showBestSellers,
                    onCheckedChange = { showBestSellers = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,          // Màu nút tròn khi bật
                        checkedTrackColor = Color(red = 52, green = 235, blue = 174),         // Màu nền khi bật
                        uncheckedThumbColor = Color.Gray,        // Màu nút tròn khi tắt
                        uncheckedTrackColor = Color.LightGray    // Màu nền khi tắt
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Product Grid View
        LazyVerticalGrid (
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredProducts) {product ->
                ProductCard(product)
            }
        }
    }
}


@Composable
fun ProductCard(product: Product) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(220.dp)
            .clickable { /* Navigate to product details */ }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Hình ảnh sản phẩm
                    Image(
                        painter = painterResource(id = R.drawable.ic_coffee),
                        contentDescription = product.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    // Mark star as Best Seller
                    if (product.isBestSeller) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Best Seller",
                            tint = Color(red = 214, green = 193, blue = 6),
                            modifier = Modifier
                                .size(36.dp)
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$${product.price}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { /* Add to cart */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Add to Cart")
                    }
                }
            }
        }
    }
}
