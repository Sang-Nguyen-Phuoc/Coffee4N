package com.example.coffee4n.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val state = viewModel.state.collectAsState().value

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Coffee Menu", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (state.products.isEmpty()) {
            Text(text = "No products available")
        } else {
            state.products.forEach { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = product.description)
                        Text(text = "Price: ${product.price}")
                    }
                }
            }
        }
    }
}