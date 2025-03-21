package com.example.coffee4n.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.coffee4n.ui.home.HomeScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Destinations.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Destinations.HOME) {
            HomeScreen()
        }
        composable(Destinations.CART) {
            navBackStackEntry ->
            val userId = navBackStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
            CartScreen(
                userId = userId,
                onCheckout = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.HOME) { this.inclusive = true }
                    }
                }
            )
        }
    }
}