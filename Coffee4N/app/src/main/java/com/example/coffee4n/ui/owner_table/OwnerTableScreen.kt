package com.example.coffee4n.ui.owner_table

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Chair
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.TableBar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.coffee4n.model.Table

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerTableScreen(viewModel: OwnerTableViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    // Define colors
    val primaryColor = Color(0xFFC67C4E)
    val backgroundColor = Color(0xFFF9F2ED)
    val cardOverlayColor = Color.Black.copy(alpha = 0.5f)
    val availableColor = Color(0xFF5A9280)
    val bookedColor = Color(0xFFE38B73)
    val disabledColor = Color.Gray

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TableBar,
                            contentDescription = null,
                            tint = primaryColor
                        )
                        Text(
                            text = "Table Management",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF313131),
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier.padding(end = 24.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.openAddDialog() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(primaryColor, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Table",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    scrolledContainerColor = backgroundColor.copy(alpha = 0.95f)
                ),
                modifier = Modifier.shadow(elevation = 4.dp)
            )
        },
        // Remove the floatingActionButton parameter since we moved it to the top bar
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Rest of your existing code remains the same
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = primaryColor,
                    strokeWidth = 3.dp
                )
            } else if (state.error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TableBar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(
                        onClick = { viewModel.loadTablesAndBookings() },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Retry")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    itemsIndexed(state.tables) { index, table ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(durationMillis = 350, delayMillis = index * 50)) +
                                    slideInVertically(
                                        animationSpec = tween(durationMillis = 350, delayMillis = index * 50),
                                        initialOffsetY = { it / 2 }
                                    )
                        ) {
                            TableCard(
                                table = table,
                                viewModel = viewModel,
                                primaryColor = primaryColor,
                                availableColor = availableColor,
                                bookedColor = bookedColor
                            )
                        }
                    }
                }

                if (state.tables.isNotEmpty()) {
                    SummaryCard(
                        tables = state.tables,
                        availableColor = availableColor,
                        bookedColor = bookedColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.BottomCenter)
                    )
                }
            }
        }

        // Dialog hiển thị booking
        if (state.showBookingDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.closeBookingDialog() },
                title = { Text("Bookings for Table ${state.selectedTableBookings.firstOrNull()?.tableId ?: ""}") },
                text = {
                    if (state.selectedTableBookings.isEmpty()) {
                        Text("No pending bookings for this table.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.selectedTableBookings) { booking ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("Customer: ${booking.customerName}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Phone: ${booking.phoneNumber}", style = MaterialTheme.typography.bodyMedium)
                                        Text("People: ${booking.numberOfPeople}", style = MaterialTheme.typography.bodyMedium)
                                        Text("Time: ${booking.bookingTime}", style = MaterialTheme.typography.bodyMedium)
                                        booking.notes?.let { Text("Notes: $it", style = MaterialTheme.typography.bodySmall) }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.padding(top = 8.dp)
                                        ) {
                                            // Nút Confirm
                                            Button(
                                                onClick = { viewModel.confirmBooking(booking) },
                                                enabled = booking.status != "CONFIRMED", // Vô hiệu hóa nếu đã xác nhận
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (booking.status == "CONFIRMED") disabledColor else availableColor,
                                                    disabledContainerColor = disabledColor // Màu xám khi vô hiệu hóa
                                                )
                                            ) {
                                                Text(
                                                    text = if (booking.status == "CONFIRMED") "Confirmed" else "Confirm"
                                                )
                                            }

                                            // Nút Reject
                                            Button(
                                                onClick = { viewModel.rejectBooking(booking) },
                                                colors = ButtonDefaults.buttonColors(containerColor = bookedColor)
                                            ) {
                                                Text("Reject")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { viewModel.closeBookingDialog() }) {
                        Text("Close")
                    }
                }
            )
        }

        // Add/Edit Dialog
        if (state.showAddEditDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.closeDialog() },
                title = {
                    Text(
                        text = if (state.currentTable == null) "Add New Table" else "Edit Table ${state.currentTable?.tableNumber}",
                        fontWeight = FontWeight.Bold
                    )
                },
                icon = {
                    Icon(
                        imageVector = if (state.currentTable == null) Icons.Default.Add else Icons.Default.Edit,
                        contentDescription = null,
                        tint = primaryColor
                    )
                },
                containerColor = backgroundColor,
                iconContentColor = primaryColor,
                titleContentColor = Color(0xFF313131),
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // For the Table Number input
                        OutlinedTextField(
                            value = state.tableNumberInput,
                            onValueChange = { viewModel.updateTableNumberInput(it) },
                            label = { Text("Table Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedLabelColor = primaryColor
                            )
                        )

                        // For the Capacity input
                        OutlinedTextField(
                            value = state.capacityInput,
                            onValueChange = { viewModel.updateCapacityInput(it) },
                            label = { Text("Capacity") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Chair,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedLabelColor = primaryColor
                            )
                        )

                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // For the Status dropdown
                            OutlinedTextField(
                                value = state.statusInput,
                                onValueChange = { viewModel.updateStatusInput(it) },
                                label = { Text("Status") },
                                readOnly = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.TableBar,
                                        contentDescription = null,
                                        tint = if (state.statusInput == "AVAILABLE") availableColor else bookedColor
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    focusedLabelColor = primaryColor
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(backgroundColor)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .background(availableColor, CircleShape)
                                            )
                                            Text("AVAILABLE")
                                        }
                                    },
                                    onClick = {
                                        viewModel.updateStatusInput("AVAILABLE")
                                        expanded = false
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .background(bookedColor, CircleShape)
                                            )
                                            Text("BOOKED")
                                        }
                                    },
                                    onClick = {
                                        viewModel.updateStatusInput("BOOKED")
                                        expanded = false
                                    }
                                )
                            }
                        }

                        // For the Image URL input
                        OutlinedTextField(
                            value = state.imageUrlInput,
                            onValueChange = { viewModel.updateImageUrlInput(it) },
                            label = { Text("Image URL") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedLabelColor = primaryColor
                            )
                        )

                        // Image preview
                        if (state.imageUrlInput.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = state.imageUrlInput,
                                    contentDescription = "Image Preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.3f)
                                                )
                                            )
                                        )
                                )
                                Text(
                                    text = "Preview",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.saveTable() },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.closeDialog() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        if (state.showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { viewModel.closeDialog() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("Delete Table") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Are you sure you want to delete Table ${state.tableToDelete?.tableNumber}?")

                        // Show a preview of the table to be deleted
                        state.tableToDelete?.let { table ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = table.imageUrl.ifEmpty { "https://fastly.picsum.photos/id/1011/200/200.jpg?hmac=ISwJXaLKDOtBGE_n3myoHUev_P_OH3zpWqLx0yHp0pY" },
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .blur(2.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(cardOverlayColor)
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Table ${table.tableNumber}",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Capacity: ${table.capacity}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (table.status == "AVAILABLE") availableColor else bookedColor,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = table.status,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deleteTable() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { viewModel.closeDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun TableCard(
    table: Table,
    viewModel: OwnerTableViewModel,
    primaryColor: Color,
    availableColor: Color,
    bookedColor: Color
) {
    val state by viewModel.state.collectAsState()
    val pendingCount = state.pendingBookings[table.id] ?: 0 // Lấy số lượng pending bookings cho bàn này

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clickable { viewModel.openBookingDialog(table) }, // Thêm sự kiện nhấn
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = table.imageUrl.ifEmpty { "https://fastly.picsum.photos/id/1011/200/200.jpg?hmac=ISwJXaLKDOtBGE_n3myoHUev_P_OH3zpWqLx0yHp0pY" },
                contentDescription = "Table ${table.tableNumber} Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (table.status == "AVAILABLE") availableColor else bookedColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = table.status,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        if (pendingCount > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = "Pending Bookings",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = pendingCount.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.openEditDialog(table) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.0f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.confirmDelete(table) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.0f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Table ${table.tableNumber}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Chair,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )

                        Text(
                            text = "Capacity: ${table.capacity} people",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    tables: List<Table>,
    availableColor: Color,
    bookedColor: Color,
    modifier: Modifier = Modifier
) {
    val availableTables = tables.count { it.status == "AVAILABLE" }
    val bookedTables = tables.count { it.status == "BOOKED" }
    val totalCapacity = tables.sumOf { it.capacity }

    Card(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Tables Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Available tables
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = availableTables.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = availableColor
                    )
                    Text(
                        text = "Available",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // Booked tables
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = bookedTables.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = bookedColor
                    )
                    Text(
                        text = "Booked",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // Total capacity
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = totalCapacity.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Seats",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}