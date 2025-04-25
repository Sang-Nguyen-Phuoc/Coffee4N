package com.example.coffee4n.navigation

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.coffee4n.ui.booking_table.BookingTableScreen
import com.example.coffee4n.ui.cart.CartScreen
import com.example.coffee4n.ui.checkout.CheckoutScreen
import com.example.coffee4n.ui.favorites.FavoritesScreen
import com.example.coffee4n.ui.home.HomeScreen
import com.example.coffee4n.ui.login.LoginScreen
import com.example.coffee4n.ui.notifications.NotificationsScreen
import com.example.coffee4n.ui.orders.OrdersScreen
import com.example.coffee4n.ui.product_details.ProductDetailsScreen
import com.example.coffee4n.ui.profile.ProfileScreen
import com.example.coffee4n.ui.signup.SignupScreen
import com.example.coffee4n.ui.welcome.WelcomeScreen

@Composable
fun AppNavHost(startDestination: String = Destinations.HOME, parentNavController: NavHostController) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf(
        Destinations.HOME,
        Destinations.CART,
        Destinations.ORDERS,
        Destinations.NOTIFICATIONS,
        Destinations.PROFILE,
        Destinations.BOOKING_TABLE
    )

    val context = LocalContext.current
    var showLoginDialog by remember { mutableStateOf(false) }

    // This is our top-level layout
    Box(modifier = Modifier.fillMaxSize()) {
        // First, add a status bar background that's black
        Box(
            modifier = Modifier
                .windowInsetsPadding(
                    WindowInsets.statusBars.only(WindowInsetsSides.Top)
                )
                .background(Color.Black)
        )

        // Then the main app content with red background
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background, // This will be red from the theme
            contentColor = MaterialTheme.colorScheme.onBackground,
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home", Modifier.size(35.dp)) },
                            label = { Text("Home", fontSize = 10.sp) },
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
                            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", Modifier.size(35.dp)) },
                            label = { Text("Cart", fontSize = 10.sp) },
                            selected = currentRoute == Destinations.CART,
                            onClick = {
                                val userId = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                    .getInt("userId", 0)
                                println("Navigating to Cart with userId: $userId")
                                if (userId != 0) {
                                    navController.navigate(Destinations.CART) {
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
                            icon = { Icon(Icons.Default.Receipt, contentDescription = "Orders", Modifier.size(35.dp)) },
                            label = { Text("Orders", fontSize = 10.sp) },
                            selected = currentRoute == Destinations.ORDERS,
                            onClick = {
                                navController.navigate(Destinations.ORDERS) {
                                    popUpTo(Destinations.HOME) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications", Modifier.size(35.dp)) },
                            label = { Text("Notifications", fontSize = 10.sp) },
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
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile", Modifier.size(35.dp)) },
                            label = { Text("Profile", fontSize = 10.sp) },
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
            // The navigation content
            Surface(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.background // This ensures red background
            ) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable(Destinations.WELCOME) { WelcomeScreen(parentNavController) }
                    composable(Destinations.LOGIN) { LoginScreen(parentNavController) }
                    composable(Destinations.SIGNUP) { SignupScreen(parentNavController) }
                    composable(Destinations.HOME) { HomeScreen(navController) }
                    composable(Destinations.ORDERS) { OrdersScreen(parentNavController) }
                    composable(Destinations.CART) { CartScreen(navController) }
                    composable(Destinations.CHECKOUT) { CheckoutScreen(navController) }
                    composable(Destinations.NOTIFICATIONS) { NotificationsScreen(parentNavController) }
                    composable(Destinations.PROFILE) { ProfileScreen(parentNavController) }
                    composable(Destinations.BOOKING_TABLE) { BookingTableScreen(parentNavController) }

                    composable(
                        route = Destinations.PRODUCT_DETAILS,
                        arguments = listOf(
                            navArgument("productId") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val productId = backStackEntry.arguments?.getInt("productId") ?: 0
                        ProductDetailsScreen(navController = navController, productId = productId)
                    }
                }
            }
        }

        // Dialog should be outside of Scaffold
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