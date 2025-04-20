package com.example.coffee4n.ui.owner_dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.coffee4n.navigation.Destinations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(navController: NavController) {
    val viewModel: OwnerDashboardViewModel = viewModel()
    val state = viewModel.state.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Owner Dashboard",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF313131)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF9F2ED)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F2ED))
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary Cards (giữ nguyên)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardStatCard(
                    title = "Active Orders",
                    value = state.ordersCount.toString(),
                    icon = Icons.Default.ShoppingCart,
                    backgroundColor = Color(0xFFFAE1DD),
                    contentColor = Color(0xFFE38B73),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Destinations.OWNER_ORDERS) }
                )

                DashboardStatCard(
                    title = "Daily Revenue",
                    value = "$${state.dailyRevenue}",
                    icon = Icons.Default.AttachMoney,
                    backgroundColor = Color(0xFFD8E2DC),
                    contentColor = Color(0xFF5A9280),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Destinations.OWNER_ANALYTICS) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardStatCard(
                    title = "Low Stock",
                    value = state.lowStockItems.toString(),
                    icon = Icons.Default.Inventory,
                    backgroundColor = Color(0xFFECE4DB),
                    contentColor = Color(0xFFC67C4E),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Destinations.OWNER_INVENTORY) }
                )

                DashboardStatCard(
                    title = "Employees",
                    value = state.employeesPresent.toString(),
                    icon = Icons.Default.People,
                    backgroundColor = Color(0xFFE8E8E4),
                    contentColor = Color(0xFF6D6D6D),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Destinations.OWNER_EMPLOYEES) }
                )
            }

            // NEW: Business Insights Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { navController.navigate(Destinations.OWNER_INSIGHTS) },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF0F0F5)
                ),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.InsertChart,
                        contentDescription = "Insights",
                        tint = Color(0xFF7B61FF),
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Business Insights",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF313131)
                        )
                        Text(
                            text = "View product trends and performance analytics",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6D6D6D)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View",
                        tint = Color(0xFF7B61FF)
                    )
                }
            }

            // Booked Tables Card (giữ nguyên)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { navController.navigate(Destinations.OWNER_TABLES) },
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8EDEB)
                ),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = "Tables",
                        tint = Color(0xFFC67C4E),
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Booked Tables",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF313131)
                        )
                        Text(
                            text = state.bookedTables.toString() + " of 12 tables booked",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6D6D6D)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0xFFFAE1DD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${(state.bookedTables / 12.0 * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFC67C4E),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Recent Activity Section (giữ nguyên)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Today's Overview",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF313131),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    ActivityItem(
                        title = "New order #1089",
                        time = "10 minutes ago",
                        icon = Icons.Default.ShoppingCart,
                        color = Color(0xFFC67C4E)
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFFEDEDED)
                    )

                    ActivityItem(
                        title = "Inventory alert: Coffee beans low",
                        time = "25 minutes ago",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFE38B73)
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color(0xFFEDEDED)
                    )

                    ActivityItem(
                        title = "Table #3 reserved for 2:00 PM",
                        time = "1 hour ago",
                        icon = Icons.Default.EventSeat,
                        color = Color(0xFF5A9280)
                    )

                    TextButton(
                        onClick = { },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "View all activity",
                            color = Color(0xFFC67C4E)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF313131),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ActivityItem(
    title: String,
    time: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF313131)
            )

            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6D6D6D)
            )
        }
    }
}