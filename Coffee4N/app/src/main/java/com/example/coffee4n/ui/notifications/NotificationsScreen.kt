package com.example.coffee4n.ui.notifications

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun NotificationsScreen(navController: NavController) {
    Text(
        "Notifications Screen",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.fillMaxSize()
    )
}