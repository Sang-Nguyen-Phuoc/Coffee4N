package com.example.coffee4n.ui.owner_inventory

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.*
import com.example.coffee4n.ui.owner_inventory.ingredient_tab.IngredientTab
import com.example.coffee4n.ui.owner_inventory.ingredient_tab.IngredientTabViewModel
import com.example.coffee4n.ui.owner_inventory.transaction_tab.TransactionTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerInventoryScreen() {
    val context = LocalContext.current
    val viewModel: IngredientTabViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return IngredientTabViewModel(context.applicationContext as Application) as T
            }
        }
    )

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Ingredients", "Transactions")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Inventory Management",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF313131)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF9F2ED)),
                actions = {
                    if (selectedTabIndex == 0) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFC67C4E),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .size(48.dp)
                                .offset(x = (-12).dp)
                        ) {
                            IconButton(onClick = { viewModel.showAddEditIngredientDialog(isNew = true) }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Ingredient",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            )
        },
        containerColor = Color(0xFFF9F2ED)
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFFF9F2ED),
                contentColor = Color(0xFFC67C4E)
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTabIndex == index) Color(0xFFC67C4E) else Color.Gray,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> IngredientTab()
                1 -> TransactionTab()
            }
        }
    }
}


