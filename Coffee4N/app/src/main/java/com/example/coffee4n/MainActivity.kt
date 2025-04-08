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
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.cloudinary.android.MediaManager
import com.example.coffee4n.model.CartItem
import com.example.coffee4n.navigation.AppNavHost
import com.example.coffee4n.navigation.OwnerNavHost
import com.example.coffee4n.navigation.Destinations
import com.example.coffee4n.ui.theme.Coffee4NTheme
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Make the window draw under the status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Get the insets controller to manipulate the system bars
        val controller = WindowCompat.getInsetsController(window, window.decorView)

        // Set status bar text to white (false means dark icons aren't used, so light/white icons)
        controller.isAppearanceLightStatusBars = false

        // Get saved preferences
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirstTime = prefs.getBoolean("isFirstTime", true)
        val authToken = prefs.getString("authToken", null)
        val userId = prefs.getInt("userId", 0)



        // Determine start destination based on auth status
        var startDestination = if (isFirstTime) {
            Destinations.WELCOME
        } else if (authToken != null && userId != 0) {
            // Initialize Firebase Auth if using token validation
            // Note: Full token validation implementation would be done here
            Destinations.HOME
        } else {
            Destinations.LOGIN
        }

        setContent {
            Coffee4NTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    AppNavHost(startDestination = startDestination)
//                    OwnerNavHost(startDestination = Destinations.OWNER_PRODUCTS)
//                    AppNavHost(startDestination = Destinations.HOME)
                }
            }
        }
    }
}
