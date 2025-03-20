package com.example.coffee4n.ui.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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