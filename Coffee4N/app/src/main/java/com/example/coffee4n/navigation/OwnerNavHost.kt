package com.example.coffee4n.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.coffee4n.ui.owner_dashboard.OwnerDashboardScreen
import com.example.coffee4n.ui.owner_employee.OwnerEmployeeScreen
import com.example.coffee4n.ui.owner_table.OwnerTableScreen
import androidx.compose.ui.unit.sp
import com.example.coffee4n.ui.owner_product.OwnerProductScreen


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
                    icon = { Icon(Icons.Default.Coffee, contentDescription = "Products") },
                    label = {
                        Text(
                            text = "Products",
                        )
                    },
                    selected = currentRoute == Destinations.OWNER_PRODUCTS,
                    onClick = {
                        if (currentRoute == Destinations.OWNER_PRODUCTS) {
                            navController.navigate(Destinations.OWNER_DASHBOARD) { launchSingleTop = true }
                        } else {
                            navController.navigate(Destinations.OWNER_PRODUCTS) { launchSingleTop = true }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFC67C4E),
                        unselectedIconColor = Color(0xFF313131),
                        selectedTextColor = Color(0xFFC67C4E),
                        unselectedTextColor = Color(0xFF313131)
                    )
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Orders") },
                    label = { Text("Orders") },
                    selected = currentRoute == Destinations.OWNER_ORDERS,
                    onClick = {
                        if (currentRoute == Destinations.OWNER_ORDERS) {
                            navController.navigate(Destinations.OWNER_DASHBOARD) { launchSingleTop = true }
                        } else {
                            navController.navigate(Destinations.OWNER_ORDERS) { launchSingleTop = true }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFC67C4E),
                        unselectedIconColor = Color(0xFF313131),
                        selectedTextColor = Color(0xFFC67C4E),
                        unselectedTextColor = Color(0xFF313131)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventory") },
                    label = {
                        Text(
                            text = "Inventory",
                        )
                    },
                    selected = currentRoute == Destinations.OWNER_INVENTORY,
                    onClick = {
                        if (currentRoute == Destinations.OWNER_INVENTORY) {
                            navController.navigate(Destinations.OWNER_DASHBOARD) { launchSingleTop = true }
                        } else {
                            navController.navigate(Destinations.OWNER_INVENTORY) { launchSingleTop = true }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFC67C4E),
                        unselectedIconColor = Color(0xFF313131),
                        selectedTextColor = Color(0xFFC67C4E),
                        unselectedTextColor = Color(0xFF313131)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.People, contentDescription = "Employees") },
                    label = {
                        Text(
                            text = "Employees",
                        )
                    },
                    selected = currentRoute == Destinations.OWNER_EMPLOYEES,
                    onClick = {
                        if (currentRoute == Destinations.OWNER_EMPLOYEES) {
                            navController.navigate(Destinations.OWNER_DASHBOARD) { launchSingleTop = true }
                        } else {
                            navController.navigate(Destinations.OWNER_EMPLOYEES) { launchSingleTop = true }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFC67C4E),
                        unselectedIconColor = Color(0xFF313131),
                        selectedTextColor = Color(0xFFC67C4E),
                        unselectedTextColor = Color(0xFF313131)
                    )
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.TableChart, contentDescription = "Tables") },
                    label = {
                        Text(
                            text = "Tables",
                        )
                    },
                    selected = currentRoute == Destinations.OWNER_TABLES,
                    onClick = {
                        if (currentRoute == Destinations.OWNER_TABLES) {
                            navController.navigate(Destinations.OWNER_DASHBOARD) { launchSingleTop = true }
                        } else {
                            navController.navigate(Destinations.OWNER_TABLES) { launchSingleTop = true }
                        }
                    },
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
            composable(Destinations.OWNER_PRODUCTS) { OwnerProductScreen(navController) }
            composable(Destinations.OWNER_ORDERS) {
                Text("Orders Placeholder", color = Color(0xFF313131), modifier = Modifier.padding(innerPadding))
            }
            composable(Destinations.OWNER_INVENTORY) {
                Text("Inventory Placeholder", color = Color(0xFF313131), modifier = Modifier.padding(innerPadding))
            }
            composable(Destinations.OWNER_EMPLOYEES) { OwnerEmployeeScreen() }

            composable(Destinations.OWNER_TABLES) { OwnerTableScreen() }
        }
    }
}