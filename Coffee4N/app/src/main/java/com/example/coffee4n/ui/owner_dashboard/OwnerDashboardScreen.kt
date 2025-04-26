package com.example.coffee4n.ui.owner_dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
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
                ),
                actions = {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFC67C4E),
                        shadowElevation = 6.dp,
                        modifier = Modifier.offset(x = (-12).dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clickable { navController.navigate(Destinations.OWNER_PROFILE) }
                        ) {
                            AsyncImage(
                                model = state.avatarUrl,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
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
            // Summary Cards
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
                    title = "Table Requests",
                    value = state.pendingTableRequests.toString(),
                    icon = Icons.Default.EventSeat,
                    backgroundColor = Color(0xFFD8E2DC),
                    contentColor = Color(0xFF5A9280),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Destinations.OWNER_TABLES) }
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

            // Business Insights Card
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

            // Booked Tables Card
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
                            text = "${state.bookedTables} of ${state.totalTables} tables booked",
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
                            text = if (state.totalTables > 0) {
                                "${(state.bookedTables.toFloat() / state.totalTables * 100).toInt()}%"
                            } else {
                                "0%"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFC67C4E),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Recent Activity Section
            TodaysOverviewCard(
                activities = state.activities,
                lowStockItems = state.lowStockItems,
                error = state.error,
                maxHeight = 300.dp // You can adjust this value based on your needs
            )

            Spacer(modifier = Modifier.height(16.dp))
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
fun ActivityItemRow(
    title: String,
    time: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF313131)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = if (time == "Critical") Color(0xFFE38B73) else Color(0xFF6D6D6D)
            )
        }
    }
}