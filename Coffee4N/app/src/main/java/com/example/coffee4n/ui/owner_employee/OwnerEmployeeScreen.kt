package com.example.coffee4n.ui.owner_employee

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coffee4n.model.Employee
import com.example.coffee4n.ui.owner_employee.OwnerEmployeeViewModel.Companion.formatCurrency
import com.example.coffee4n.ui.owner_employee.OwnerEmployeeViewModel.Companion.formatDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerEmployeeScreen() {
    val context = LocalContext.current
    val viewModel: OwnerEmployeeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OwnerEmployeeViewModel(context.applicationContext as Application) as T
            }
        }
    )

    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Employee Management",
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
                    IconButton(onClick = { viewModel.openAddDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Employee",
                            tint = Color(0xFFC67C4E)
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFFF9F2ED)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFC67C4E)
                )
            } else if (state.employees.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = Color(0xFFC67C4E).copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No employees found",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF6D6D6D)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.openAddDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFC67C4E)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Employee")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Summary Cards
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = "Total Employees",
                            value = state.employees.size.toString(),
                            backgroundColor = Color(0xFFD8E2DC),
                            contentColor = Color(0xFF5A9280),
                            modifier = Modifier.weight(1f)
                        )

                        SummaryCard(
                            title = "Total Salary",
                            value = formatCurrency(state.employees.sumOf { it.salary }),
                            backgroundColor = Color(0xFFFAE1DD),
                            contentColor = Color(0xFFE38B73),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Employee List
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                text = "Staff List",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF313131),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.employees) { employee ->
                                    EmployeeListItem(
                                        employee = employee,
                                        onEdit = { viewModel.openEditDialog(employee) },
                                        onDelete = { viewModel.confirmDelete(employee) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Add/Edit Employee Dialog
            if (state.showAddEditDialog) {
                AddEditEmployeeDialog(
                    isEdit = state.currentEmployee != null,
                    name = state.nameInput,
                    position = state.positionInput,
                    salary = state.salaryInput,
                    hireDate = state.hireDateInput,
                    phone = state.phoneInput,
                    email = state.emailInput,
                    onNameChange = { viewModel.updateNameInput(it) },
                    onPositionChange = { viewModel.updatePositionInput(it) },
                    onSalaryChange = { viewModel.updateSalaryInput(it) },
                    onHireDateChange = { viewModel.updateHireDateInput(it) },
                    onPhoneChange = { viewModel.updatePhoneInput(it) },
                    onEmailChange = { viewModel.updateEmailInput(it) },
                    onSave = { viewModel.saveEmployee() },
                    onDismiss = { viewModel.closeDialog() }
                )
            }

            // Delete Confirmation Dialog
            if (state.showDeleteConfirmation) {
                DeleteConfirmationDialog(
                    employeeName = state.employeeToDelete?.name ?: "",
                    onConfirm = { viewModel.deleteEmployee() },
                    onDismiss = { viewModel.closeDialog() }
                )
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF313131)
            )
        }
    }
}

@Composable
fun EmployeeListItem(
    employee: Employee,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Employee avatar/initial
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFC67C4E).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = employee.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFC67C4E),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Employee details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = employee.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF313131),
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = employee.position,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6D6D6D)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatCurrency(employee.salary),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF5A9280)
                    )

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6D6D6D)
                    )

                    Text(
                        text = "Since ${formatDate(employee.hireDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6D6D6D)
                    )
                }

                Text(
                    text = employee.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6D6D6D),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }

            // Action buttons
            Row {
                // Edit button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFECE4DB))
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFFC67C4E),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFAE1DD))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE38B73),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEmployeeDialog(
    isEdit: Boolean,
    name: String,
    position: String,
    salary: String,
    hireDate: Date,
    phone: String,
    email: String,
    onNameChange: (String) -> Unit,
    onPositionChange: (String) -> Unit,
    onSalaryChange: (String) -> Unit,
    onHireDateChange: (Date) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = remember { Calendar.getInstance() }
    var showDatePicker by remember { mutableStateOf(false) }

    calendar.time = hireDate

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = hireDate.time
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onHireDateChange(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = if (isEdit) "Edit Employee" else "Add New Employee",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF313131),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC67C4E),
                        focusedLabelColor = Color(0xFFC67C4E),
                        cursorColor = Color(0xFFC67C4E)
                    )
                )

                OutlinedTextField(
                    value = position,
                    onValueChange = onPositionChange,
                    label = { Text("Position") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC67C4E),
                        focusedLabelColor = Color(0xFFC67C4E),
                        cursorColor = Color(0xFFC67C4E)
                    )
                )

                OutlinedTextField(
                    value = salary,
                    onValueChange = onSalaryChange,
                    label = { Text("Salary") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("$") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC67C4E),
                        focusedLabelColor = Color(0xFFC67C4E),
                        cursorColor = Color(0xFFC67C4E)
                    )
                )

                OutlinedTextField(
                    value = formatDate(hireDate),
                    onValueChange = { },
                    label = { Text("Hire Date") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Select Date",
                            tint = Color(0xFFC67C4E)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC67C4E),
                        focusedLabelColor = Color(0xFFC67C4E),
                        cursorColor = Color(0xFFC67C4E)
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC67C4E),
                        focusedLabelColor = Color(0xFFC67C4E),
                        cursorColor = Color(0xFFC67C4E)
                    )
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFC67C4E),
                        focusedLabelColor = Color(0xFFC67C4E),
                        cursorColor = Color(0xFFC67C4E)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC67C4E)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isEdit) "Update" else "Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF6D6D6D)
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    employeeName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "Confirm Deletion",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF313131),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete $employeeName? This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6D6D6D)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE38B73)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF6D6D6D)
                )
            ) {
                Text("Cancel")
            }
        }
    )
}