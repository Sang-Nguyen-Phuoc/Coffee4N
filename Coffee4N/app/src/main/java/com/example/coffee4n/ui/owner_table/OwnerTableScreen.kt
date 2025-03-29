package com.example.coffee4n.ui.owner_table

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffee4n.model.Table

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerTableScreen(viewModel: OwnerTableViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Table Management",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF313131)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF9F2ED))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = Color(0xFFC67C4E),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Table")
            }
        },
        containerColor = Color(0xFFF9F2ED)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.tables) { table ->
                        TableCard(table, viewModel)
                    }
                }
            }
        }

        // Add/Edit Dialog
        if (state.showAddEditDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.closeDialog() },
                title = { Text(if (state.currentTable == null) "Add Table" else "Edit Table") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = state.tableNumberInput,
                            onValueChange = { viewModel.updateTableNumberInput(it) },
                            label = { Text("Table Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.capacityInput,
                            onValueChange = { viewModel.updateCapacityInput(it) },
                            label = { Text("Capacity") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        ExposedDropdownMenuBox(
                            expanded = false,
                            onExpandedChange = {},
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = state.statusInput,
                                onValueChange = { viewModel.updateStatusInput(it) },
                                label = { Text("Status") },
                                readOnly = true,
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = false,
                                onDismissRequest = {}
                            ) {
                                DropdownMenuItem(
                                    text = { Text("AVAILABLE") },
                                    onClick = { viewModel.updateStatusInput("AVAILABLE") }
                                )
                                DropdownMenuItem(
                                    text = { Text("OCCUPIED") },
                                    onClick = { viewModel.updateStatusInput("OCCUPIED") }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.saveTable() }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        if (state.showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { viewModel.closeDialog() },
                title = { Text("Delete Table") },
                text = { Text("Are you sure you want to delete Table ${state.tableToDelete?.tableNumber}?") },
                confirmButton = {
                    TextButton(onClick = { viewModel.deleteTable() }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun TableCard(table: Table, viewModel: OwnerTableViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Table ${table.tableNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF313131)
                )
                Text(
                    text = "Capacity: ${table.capacity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6D6D6D)
                )
                Text(
                    text = "Status: ${table.status}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (table.status == "AVAILABLE") Color(0xFF5A9280) else Color(0xFFE38B73)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { viewModel.openEditDialog(table) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFFC67C4E))
                }
                IconButton(onClick = { viewModel.confirmDelete(table) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE38B73))
                }
            }
        }
    }
}