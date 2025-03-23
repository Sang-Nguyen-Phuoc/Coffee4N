package com.example.coffee4n.ui.owner_employee

import com.example.coffee4n.model.Employee
import java.util.Date

data class OwnerEmployeeState(
    val employees: List<Employee> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddEditDialog: Boolean = false,
    val currentEmployee: Employee? = null,
    val nameInput: String = "",
    val positionInput: String = "",
    val salaryInput: String = "",
    val hireDateInput: Date = Date(),
    val phoneInput: String = "",
    val emailInput: String = "",
    val showDeleteConfirmation: Boolean = false,
    val employeeToDelete: Employee? = null
)