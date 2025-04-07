package com.example.coffee4n.navigation

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.TableBar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.coffee4n.ui.booking_table.BookingTableScreen
import com.example.coffee4n.ui.favorites.FavoritesScreen
import com.example.coffee4n.ui.home.HomeScreen
import com.example.coffee4n.ui.login.LoginScreen
import com.example.coffee4n.ui.notifications.NotificationsScreen
import com.example.coffee4n.ui.cart.CartScreen
import com.example.coffee4n.ui.profile.ProfileScreen
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
        Destinations.NOTIFICATIONS,
        Destinations.PROFILE,
        Destinations.BOOKING_TABLE
    )

    val context = LocalContext.current

    var showLoginDialog by remember { mutableStateOf(false) }

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
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                        label = { Text("Favorites") },
                        selected = currentRoute == Destinations.FAVORITES,
                        onClick = {
                            navController.navigate(Destinations.FAVORITES) {
                                popUpTo(Destinations.HOME) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                        label = { Text("Cart") },
                        selected = currentRoute == Destinations.CART,
                        onClick = {
                            val userId = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                .getInt("userId", 0)
                            println("Navigating to Cart with userId: $userId")
                            if (userId != 0) {
                                navController.navigate(Destinations.CART) { // Bỏ userId khỏi route
                                    popUpTo(Destinations.HOME) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            } else {
                                showLoginDialog = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                        label = { Text("Notifications") },
                        selected = currentRoute == Destinations.NOTIFICATIONS,
                        onClick = {
                            navController.navigate(Destinations.NOTIFICATIONS) {
                                popUpTo(Destinations.HOME) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        selected = currentRoute == Destinations.PROFILE,
                        onClick = {
                            navController.navigate(Destinations.PROFILE) {
                                popUpTo(Destinations.HOME) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
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
            composable(Destinations.CART) { // Bỏ userId khỏi route
                CartScreen(navController = navController)
            }
            composable(Destinations.CHECKOUT) { }
            composable(Destinations.NOTIFICATIONS) { NotificationsScreen(navController) }
            composable(Destinations.PROFILE) { ProfileScreen(navController) }
            composable(Destinations.BOOKING_TABLE) { BookingTableScreen(navController) }
        }

        if (showLoginDialog) {
            AlertDialog(
                onDismissRequest = { showLoginDialog = false },
                title = { Text("Login Required") },
                text = { Text("Login to view cart") },
                confirmButton = {
                    TextButton(onClick = {
                        navController.navigate(Destinations.LOGIN)
                        showLoginDialog = false
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLoginDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}