package com.example.coffee4n.ui.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffee4n.model.CartItem

@Composable
fun CartScreen(
    userId: Int,
    viewModel: CartViewModel = viewModel(),
    onCheckout: () -> Unit
) {
    val state = viewModel.state.collectAsState().value

    LaunchedEffect(Unit) {
        viewModel.loadCartItems(userId)
    }

    if (state.isLoading) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.cartItems) { cartItem ->
                    CartItemRow(cartItem, viewModel)
                }
            }
            Button(
                onClick = { viewModel.checkout(userId); onCheckout() },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            ) {
                Text("Thanh toán")
            }
        }
    }
}

@Composable
fun CartItemRow(cartItem: CartItem, viewModel: CartViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Product ID: ${cartItem.productId}", modifier = Modifier.weight(1f))
        IconButton(onClick = { viewModel.decreaseQuantity(cartItem) }) {
            Text("-")
        }
        Text(text = "${cartItem.quantity}", modifier = Modifier.padding(horizontal = 8.dp))
        IconButton(onClick = { viewModel.increaseQuantity(cartItem) }) {
            Text("+")
        }
        IconButton(onClick = { viewModel.removeItem(cartItem) }) {
            Text("Xóa")
        }
    }
}

@Preview
@Composable
fun CartScreenPreview() {
    val sampleItems = listOf(
        CartItem(id = 1, userId = 1, productId = 101, quantity = 2),
        CartItem(id = 2, userId = 1, productId = 102, quantity = 3)
    )
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(sampleItems) { cartItem ->
                CartItemRowPreview(cartItem)
            }
        }
        Button(
            onClick = {},
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
        ) {
            Text("Thanh toán")
        }
    }
}

@Preview
@Composable
fun CartItemRowPreview(cartItem: CartItem = CartItem(1, 1, 101, 2)) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Product ID: ${cartItem.productId}", modifier = Modifier.weight(1f))
        IconButton(onClick = {}) { Text("-") }
        Text(text = "${cartItem.quantity}", modifier = Modifier.padding(horizontal = 8.dp))
        IconButton(onClick = {}) { Text("+") }
        IconButton(onClick = {}) { Text("Xóa") }
    }
}