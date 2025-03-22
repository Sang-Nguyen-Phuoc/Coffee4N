package com.example.coffee4n

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.coffee4n.navigation.AppNavHost
import com.example.coffee4n.navigation.Destinations
import com.example.coffee4n.navigation.OwnerNavHost
import com.example.coffee4n.ui.theme.Coffee4NTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirstTime = prefs.getBoolean("isFirstTime", true)
        val authToken = prefs.getString("authToken", null)
        val startDestination = if (isFirstTime) {
            Destinations.WELCOME
        } else if (authToken != null) {
            Destinations.HOME
        } else {
            Destinations.LOGIN
        }
        setContent {
            Coffee4NTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(startDestination = startDestination)
//                    OwnerNavHost(startDestination = Destinations.OWNER_DASHBOARD)
                }
            }
        }
    }
}