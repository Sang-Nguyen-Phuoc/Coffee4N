package com.example.coffee4n.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.coffee4n.ui.login.LoginScreen
import com.example.coffee4n.ui.welcome.WelcomeScreen
import androidx.navigation.compose.composable

@Composable
fun RootNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Destinations.WELCOME) {
            WelcomeScreen(navController)
        }
        composable(Destinations.LOGIN) {
            LoginScreen(navController)
        }
        composable(Destinations.HOME) {
            AppNavHost(Destinations.HOME, navController)
        }
        composable(Destinations.OWNER_DASHBOARD) {
            OwnerNavHost(Destinations.OWNER_DASHBOARD, navController)
        }
    }
}
