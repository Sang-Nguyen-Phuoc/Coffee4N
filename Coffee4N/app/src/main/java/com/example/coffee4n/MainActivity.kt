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
import androidx.navigation.compose.rememberNavController
import com.cloudinary.android.MediaManager
import com.example.coffee4n.model.CartItem
import com.example.coffee4n.navigation.AppNavHost
import com.example.coffee4n.navigation.OwnerNavHost
import com.example.coffee4n.navigation.Destinations
import com.example.coffee4n.navigation.RootNavHost
import com.example.coffee4n.session.OwnerSession
import com.example.coffee4n.ui.login.LoginScreen
import com.example.coffee4n.ui.theme.Coffee4NTheme
import com.example.coffee4n.ui.welcome.WelcomeScreen
import com.example.coffee4n.utils.LanguageManager
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved language preference
        val savedLanguage = LanguageManager.getSavedLanguage(this)
        LanguageManager.updateResources(this, savedLanguage)

        // Make the window draw under the status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Get the insets controller to manipulate the system bars
        val controller = WindowCompat.getInsetsController(window, window.decorView)

        // Set status bar text to white (false means dark icons aren't used, so light/white icons)
        controller.isAppearanceLightStatusBars = false

        // Get saved preferences
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

//        prefs.edit().remove("ownerId").remove("authToken").remove("userId").remove("isOwner").apply()

        val authToken = prefs.getString("authToken", null)
        val userId = prefs.getInt("userId", 0)
        val isOwner = prefs.getBoolean("isOwner", false)
        val ownerId = prefs.getString("ownerId", null)

        OwnerSession.ownerId = ownerId ?: ""

//         Determine start destination based on auth status
        val startDestination = when {
            ownerId == null -> Destinations.WELCOME
            isOwner -> Destinations.OWNER_DASHBOARD
            authToken.isNullOrEmpty() || userId == 0 -> Destinations.LOGIN
            else -> Destinations.HOME
        }

        setContent {
            Coffee4NTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    RootNavHost(navController, startDestination)
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = LanguageManager.getSavedLanguage(newBase)
        val context = newBase.createConfigurationContext(
            newBase.resources.configuration.apply {
                setLocale(java.util.Locale(savedLanguage))
            }
        )
        super.attachBaseContext(context)
    }

}
