package com.example.coffee4n.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.coffee4n.ui.favorites.FavoritesScreen
import com.example.coffee4n.ui.home.HomeScreen
import com.example.coffee4n.ui.login.LoginScreen
import com.example.coffee4n.ui.notifications.NotificationsScreen
import com.example.coffee4n.ui.cart.CartScreen
import com.example.coffee4n.ui.signup.SignupScreen
import com.example.coffee4n.ui.welcome.WelcomeScreen

@Composable
fun AppNavHost(startDestination: String) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf(
        Destinations.HOME,
        Destinations.FAVORITES,
        Destinations.CART,
        Destinations.NOTIFICATIONS
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == Destinations.HOME,
                        onClick = {
                            navController.navigate(Destinations.HOME) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                        label = { Text("Favorites") },
                        selected = currentRoute == Destinations.FAVORITES,
                        onClick = { navController.navigate(Destinations.FAVORITES) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                        label = { Text("Cart") },
                        selected = currentRoute == Destinations.CART,
                        onClick = { navController.navigate(Destinations.CART) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                        label = { Text("Notifications") },
                        selected = currentRoute == Destinations.NOTIFICATIONS,
                        onClick = { navController.navigate(Destinations.NOTIFICATIONS) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destinations.WELCOME) { WelcomeScreen(navController) }
            composable(Destinations.LOGIN) { LoginScreen(navController) }
            composable(Destinations.SIGNUP) { SignupScreen(navController) }
            composable(Destinations.HOME) { HomeScreen(navController) }
            composable(Destinations.FAVORITES) { FavoritesScreen(navController) }
            composable(Destinations.CART) { CartScreen(navController) }
            composable(Destinations.NOTIFICATIONS) { NotificationsScreen(navController) }
        }
    }
}

