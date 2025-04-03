package com.example.coffee4n

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.cloudinary.android.MediaManager
import com.example.coffee4n.navigation.AppNavHost
import com.example.coffee4n.navigation.OwnerNavHost
import com.example.coffee4n.navigation.Destinations
import com.example.coffee4n.ui.theme.Coffee4NTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = false

        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirstTime = prefs.getBoolean("isFirstTime", true)
        val authToken = prefs.getString("authToken", null)
        val userRole = prefs.getString("userRole", "customer")
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
                ) {
//                    AppNavHost(startDestination = startDestination)
//                    OwnerNavHost(startDestination = Destinations.OWNER_PRODUCTS)
                    AppNavHost(startDestination = Destinations.HOME)
                }
            }
        }
    }

}