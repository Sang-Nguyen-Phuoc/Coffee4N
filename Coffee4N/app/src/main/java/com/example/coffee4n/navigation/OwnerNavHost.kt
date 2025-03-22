package com.example.coffee4n.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.coffee4n.ui.owner.OwnerDashboardScreen

@Composable
fun OwnerNavHost(startDestination: String = Destinations.OWNER_ORDERS) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF9F2ED), // Background color
                contentColor = Color(0xFF313131) // Icon/text color
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Orders") },
                    label = { Text("Orders") },
                    selected = currentRoute == Destinations.OWNER_ORDERS,
                    onClick = { navController.navigate(Destinations.OWNER_ORDERS) { launchSingleTop = true } },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFC67C4E),
                        unselectedIconColor = Color(0xFF313131),
                        selectedTextColor = Color(0xFFC67C4E),
                        unselectedTextColor = Color(0xFF313131)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventory") },
                    label = { Text("Inventory") },
                    selected = currentRoute == Destinations.OWNER_INVENTORY,
                    onClick = { navController.navigate(Destinations.OWNER_INVENTORY) { launchSingleTop = true } },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFC67C4E),
                        unselectedIconColor = Color(0xFF313131),
                        selectedTextColor = Color(0xFFC67C4E),
                        unselectedTextColor = Color(0xFF313131)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.People, contentDescription = "Employees") },
                    label = { Text("Employees") },
                    selected = currentRoute == Destinations.OWNER_EMPLOYEES,
                    onClick = { navController.navigate(Destinations.OWNER_EMPLOYEES) { launchSingleTop = true } },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFC67C4E),
                        unselectedIconColor = Color(0xFF313131),
                        selectedTextColor = Color(0xFFC67C4E),
                        unselectedTextColor = Color(0xFF313131)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Analytics") },
                    label = { Text("Analytics") },
                    selected = currentRoute == Destinations.OWNER_ANALYTICS,
                    onClick = { navController.navigate(Destinations.OWNER_ANALYTICS) { launchSingleTop = true } },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFC67C4E),
                        unselectedIconColor = Color(0xFF313131),
                        selectedTextColor = Color(0xFFC67C4E),
                        unselectedTextColor = Color(0xFF313131)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.TableChart, contentDescription = "Tables") },
                    label = { Text("Tables") },
                    selected = currentRoute == Destinations.OWNER_TABLES,
                    onClick = { navController.navigate(Destinations.OWNER_TABLES) { launchSingleTop = true } },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFC67C4E),
                        unselectedIconColor = Color(0xFF313131),
                        selectedTextColor = Color(0xFFC67C4E),
                        unselectedTextColor = Color(0xFF313131)
                    )
                )
            }
        },
        containerColor = Color(0xFFF9F2ED) // Main background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destinations.OWNER_DASHBOARD) { OwnerDashboardScreen(navController) }
            composable(Destinations.OWNER_ORDERS) {
                Text("Orders Placeholder", color = Color(0xFF313131), modifier = Modifier.padding(innerPadding))
            }
            composable(Destinations.OWNER_INVENTORY) {
                Text("Inventory Placeholder", color = Color(0xFF313131), modifier = Modifier.padding(innerPadding))
            }
            composable(Destinations.OWNER_EMPLOYEES) {
                Text("Employees Placeholder", color = Color(0xFF313131), modifier = Modifier.padding(innerPadding))
            }
            composable(Destinations.OWNER_ANALYTICS) {
                Text("Analytics Placeholder", color = Color(0xFF313131), modifier = Modifier.padding(innerPadding))
            }
            composable(Destinations.OWNER_TABLES) {
                Text("Tables Placeholder", color = Color(0xFF313131), modifier = Modifier.padding(innerPadding))
            }
        }
    }
}