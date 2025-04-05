package com.example.coffee4n.ui.home

import android.app.Application
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.coffee4n.R
import com.example.coffee4n.model.Category
import com.example.coffee4n.model.Product
import com.example.coffee4n.model.database.AppDatabase
import com.example.coffee4n.navigation.Destinations
import com.example.coffee4n.repository.ProductRepository
import kotlinx.coroutines.flow.StateFlow


@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(context.applicationContext as Application) as T
            }
        }
    )

    val state by viewModel.state.collectAsState()


    val filteredProducts = state.products
        .filter { it.categoryId == state.selectedCategory || state.selectedCategory == 0 }
        .filter { it.name.contains(state.searchQuery, ignoreCase = true) }
        .let { list -> if (state.showBestSellers) list.filter { it.isBestSeller } else list }
        .sortedBy { if (state.sortAscending) it.price else -it.price }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // Location Bar
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = "Location")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Location, Bilzen, Tanjungbalai", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        TextField(
            value = state.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search coffee") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(8.dp))
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
            val categories = listOf(Category(0, "All", "")) + state.categories
            itemsIndexed(categories) { index, category ->
                val isSelected = category.id == state.selectedCategory
                Button(
                    onClick = { viewModel.updateCategory(category.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(52, 235, 174) else Color.Gray,
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
                Text("Best Sellers", color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = state.showBestSellers,
                    onCheckedChange = { viewModel.toggleBestSellers() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(52, 235, 174),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.LightGray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Booking Table Button
        Button(
            onClick = { navController.navigate(Destinations.BOOKING_TABLE) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .shadow(8.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD4A373),
                contentColor = Color.White
            )
        ) {
            Text(
                "Book a Table",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Product Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredProducts) { product ->
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
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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
