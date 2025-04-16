package com.example.coffee4n.ui.orders

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun OrdersScreen(navController: NavController) {
    Text(
        text = "Orders history",
        color = Color.White,
        fontSize = 30.sp
    )
}