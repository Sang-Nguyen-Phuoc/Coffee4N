package com.example.coffee4n.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(navController: NavController) {
    val viewModel: InsightsViewModel = viewModel()
    val state = viewModel.state.collectAsState().value

    // Period selector options
    val periods = listOf("Last 7 Days", "Last 30 Days", "This Month", "Last Month", "This Year")
    var selectedPeriod by remember { mutableStateOf(periods[0]) }
    var showPeriodSelector by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {

        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F2ED))
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE6DED5)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Period: $selectedPeriod",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF313131)
                    )

                    // Replace the TextButton with IconButton using the calendar icon
                    IconButton(
                        onClick = { showPeriodSelector = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color(0xFFC67C4E)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Change Period",
                            tint = Color(0xFFC67C4E)
                        )
                    }
                }
            }

            // Scrollable content with charts
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total Revenue Summary Card
                TotalRevenueSummary(
                    totalRevenue = state.dailyRevenueData.sumOf { it.amount },
                    selectedPeriod = selectedPeriod,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                // Order Items Chart
                OrderItemsChart(
                    orderItemStats = state.orderItemStats,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(vertical = 4.dp)
                )

                // Revenue by Day Card
                RevenueByDayCard(
                    revenueData = state.dailyRevenueData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(vertical = 4.dp)
                )

                // Peak Hours Card
                PeakHoursCard(
                    peakHoursData = state.peakHoursData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(vertical = 4.dp)
                )
            }
        }

        // Period selector dialog
        if (showPeriodSelector) {
            AlertDialog(
                onDismissRequest = { showPeriodSelector = false },
                title = {
                    Text(
                        "Select Period",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        periods.forEach { period ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selectedPeriod == period)
                                            Color(0xFFF2E9E1)
                                        else
                                            Color.Transparent
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedPeriod == period,
                                    onClick = {
                                        selectedPeriod = period
                                        showPeriodSelector = false
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFFC67C4E)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = period,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF313131)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showPeriodSelector = false }
                    ) {
                        Text(
                            "Close",
                            color = Color(0xFFC67C4E)
                        )
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun TotalRevenueSummary(
    totalRevenue: Double,
    selectedPeriod: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFC67C4E)
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Revenue",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE6DED5),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "$${String.format("%.2f", totalRevenue)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = selectedPeriod,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE6DED5),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun OrderItemsChart(
    orderItemStats: List<OrderItemStat>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Most Ordered Products",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF313131),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (orderItemStats.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        color = Color(0xFF6D6D6D)
                    )
                }
            } else {
                val maxCount = orderItemStats.maxOfOrNull { it.count } ?: 1

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    orderItemStats.forEach { stat ->
                        HorizontalBarChartItem(
                            productName = stat.productName,
                            count = stat.count,
                            maxCount = maxCount,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalBarChartItem(
    productName: String,
    count: Int,
    maxCount: Int,
    modifier: Modifier = Modifier
) {
    val barWidthFraction = if (maxCount > 0) count.toFloat() / maxCount else 0f

    Row(
        modifier = modifier.height(30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product name on the left
        Text(
            text = productName,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6D6D6D),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(90.dp)
        )

        // Bar in the middle
        Box(
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
        ) {
            // Background bar (light color)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFECE4DB))
            )

            // Actual data bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(barWidthFraction)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFC67C4E))
            )
        }

        // Count value on the right
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6D6D6D),
            textAlign = TextAlign.End,
            modifier = Modifier
                .padding(start = 8.dp)
                .width(30.dp)
        )
    }
}

@Composable
fun RevenueByDayCard(
    revenueData: List<DailyRevenue>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Weekly Revenue",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF313131),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (revenueData.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        color = Color(0xFF6D6D6D)
                    )
                }
            } else {
                // Simple bar chart for revenue data
                val maxRevenue = revenueData.maxOfOrNull { it.amount } ?: 1.0

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    revenueData.forEach { dailyRevenue ->
                        RevenueBarChartItem(
                            dayName = dailyRevenue.dayName,
                            amount = dailyRevenue.amount,
                            maxAmount = maxRevenue,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueBarChartItem(
    dayName: String,
    amount: Double,
    maxAmount: Double,
    modifier: Modifier = Modifier
) {
    val barWidthFraction = if (maxAmount > 0) (amount / maxAmount).toFloat() else 0f

    Row(
        modifier = modifier.height(30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day name on the left
        Text(
            text = dayName,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6D6D6D),
            modifier = Modifier.width(60.dp)
        )

        // Bar in the middle
        Box(
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
        ) {
            // Background bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFD8E2DC))
            )

            // Actual data bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(barWidthFraction)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF5A9280))
            )
        }

        // Amount value on the right
        Text(
            text = "$${String.format("%.2f", amount)}",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6D6D6D),
            textAlign = TextAlign.End,
            modifier = Modifier
                .padding(start = 8.dp)
                .width(70.dp)
        )
    }
}

@Composable
fun PeakHoursCard(
    peakHoursData: List<HourlyData>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Peak Business Hours",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF313131),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (peakHoursData.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        color = Color(0xFF6D6D6D)
                    )
                }
            } else {
                val maxOrders = peakHoursData.maxOfOrNull { it.orderCount } ?: 1

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    peakHoursData.forEach { hourData ->
                        HourlyBarChartItem(
                            hourRange = hourData.hourRange,
                            orderCount = hourData.orderCount,
                            maxOrders = maxOrders,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyBarChartItem(
    hourRange: String,
    orderCount: Int,
    maxOrders: Int,
    modifier: Modifier = Modifier
) {
    val barWidthFraction = if (maxOrders > 0) orderCount.toFloat() / maxOrders else 0f

    Row(
        modifier = modifier.height(30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hour range on the left
        Text(
            text = hourRange,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6D6D6D),
            modifier = Modifier.width(80.dp)
        )

        // Bar in the middle
        Box(
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
        ) {
            // Background bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFECE4DB))
            )

            // Actual data bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(barWidthFraction)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE38B73))
            )
        }

        // Order count on the right
        Text(
            text = orderCount.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6D6D6D),
            textAlign = TextAlign.End,
            modifier = Modifier
                .padding(start = 8.dp)
                .width(30.dp)
        )
    }
}