package com.example.coffee4n.ui.owner_dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TodaysOverviewCard(
    activities: List<ActivityItem>,
    lowStockItems: Int,
    error: String?,
    maxHeight: Dp = 300.dp
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Overview",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF313131),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (lowStockItems > 0) {
                    Badge(
                        containerColor = Color(0xFFE38B73),
                        contentColor = Color.White
                    ) {
                        Text(text = lowStockItems.toString())
                    }
                }
            }

            error?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE57373),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            if (activities.isEmpty() && error == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent activity to display",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6D6D6D)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))

                // Use LazyColumn with a fixed height to make it scrollable
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxHeight),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Group activities by type
                    val inventoryAlerts = activities.filter { it.time == "Critical" }
                    val recentOrders = activities.filter { it.time != "Critical" }

                    // Show inventory alerts first
                    if (inventoryAlerts.isNotEmpty()) {
                        item {
                            Text(
                                text = "Inventory Alerts",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6D6D6D),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(inventoryAlerts) { activity ->
                            ActivityItemRow(
                                title = activity.title,
                                time = activity.time,
                                icon = activity.icon,
                                color = activity.color
                            )

                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color(0xFFEDEDED)
                            )
                        }
                    }

                    // Then show recent orders
                    if (recentOrders.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recent Orders",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6D6D6D),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(recentOrders) { activity ->
                            ActivityItemRow(
                                title = activity.title,
                                time = activity.time,
                                icon = activity.icon,
                                color = activity.color
                            )

                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color(0xFFEDEDED)
                            )
                        }
                    }
                }
            }
        }
    }
}