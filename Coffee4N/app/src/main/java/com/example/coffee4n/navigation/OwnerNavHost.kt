package com.example.coffee4n.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.coffee4n.ui.insights.InsightsScreen
import com.example.coffee4n.ui.owner_customer.OwnerCustomerScreen
import com.example.coffee4n.ui.owner_dashboard.OwnerDashboardScreen
import com.example.coffee4n.ui.owner_employee.OwnerEmployeeScreen
import com.example.coffee4n.ui.owner_inventory.OwnerInventoryScreen
import com.example.coffee4n.ui.owner_orders.OwnerOrderScreen
import com.example.coffee4n.ui.owner_product.OwnerProductScreen
import com.example.coffee4n.ui.owner_profile.OwnerProfileScreen
import com.example.coffee4n.ui.owner_table.OwnerTableScreen

@Composable
fun OwnerNavHost(startDestination: String = Destinations.OWNER_DASHBOARD, parentNavController: NavHostController) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF9F2ED),
                contentColor = Color(0xFF313131)
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Coffee, contentDescription = "Products", Modifier.size(35.dp)) },
                    label = { Text("Products", fontSize = 9.7.sp) },
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
                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Order/Promo", Modifier.size(35.dp)) },
                    label = { Text("Promo/Ord", fontSize = 9.7.sp) },
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
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventory", Modifier.size(35.dp)) },
                    label = { Text("Inventory", fontSize = 9.7.sp) },
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
                    icon = { Icon(Icons.Default.People, contentDescription = "Employees", Modifier.size(35.dp)) },
                    label = { Text("Employees", fontSize = 9.7.sp) },
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
                    icon = { Icon(Icons.Default.Contacts, contentDescription = "Customers", Modifier.size(35.dp)) },
                    label = { Text("Customers", fontSize = 9.7.sp) },
                    selected = currentRoute == Destinations.OWNER_CUSTOMERS,
                    onClick = {
                        if (currentRoute == Destinations.OWNER_CUSTOMERS) {
                            navController.navigate(Destinations.OWNER_DASHBOARD) { launchSingleTop = true }
                        } else {
                            navController.navigate(Destinations.OWNER_CUSTOMERS) { launchSingleTop = true }
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
                    icon = { Icon(Icons.Default.TableChart, contentDescription = "Tables", Modifier.size(35.dp)) },
                    label = { Text("Tables", fontSize = 9.7.sp) },
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
        containerColor = Color(0xFFF9F2ED)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destinations.OWNER_DASHBOARD) { OwnerDashboardScreen(navController) }
            composable(Destinations.OWNER_PRODUCTS) { OwnerProductScreen(parentNavController) }
            composable(Destinations.OWNER_ORDERS) { OwnerOrderScreen(parentNavController) }
            composable(Destinations.OWNER_CUSTOMERS) { OwnerCustomerScreen() }
            composable(Destinations.OWNER_INVENTORY) { OwnerInventoryScreen() }
            composable(Destinations.OWNER_EMPLOYEES) { OwnerEmployeeScreen() }
            composable(Destinations.OWNER_INSIGHTS) { InsightsScreen(parentNavController) }
            composable(Destinations.OWNER_TABLES) { OwnerTableScreen() }
            composable(Destinations.OWNER_PROFILE) { OwnerProfileScreen(parentNavController) }
        }
    }
}