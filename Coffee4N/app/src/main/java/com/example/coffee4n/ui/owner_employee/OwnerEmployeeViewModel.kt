package com.example.coffee4n.ui.owner_employee

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.model.Employee
import com.example.coffee4n.repository.EmployeeRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OwnerEmployeeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EmployeeRepository
    private val _state = MutableStateFlow(OwnerEmployeeState(isLoading = true))
    val state: StateFlow<OwnerEmployeeState> = _state.asStateFlow()

    init {
        repository = EmployeeRepository(FirebaseDatabase.getInstance())
        loadEmployees()
    }

    fun loadEmployees() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                repository.getEmployeesFlow().collect { employees ->
                    _state.update {
                        it.copy(
                            employees = employees,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load employees: ${e.message}"
                    )
                }
            }
        }
    }

    fun openAddDialog() {
        _state.update {
            it.copy(
                showAddEditDialog = true,
                currentEmployee = null,
                nameInput = "",
                positionInput = "",
                salaryInput = "",
                hireDateInput = Date(),
                phoneInput = "",
                emailInput = ""
            )
        }
    }

    fun openEditDialog(employee: Employee) {
        _state.update {
            it.copy(
                showAddEditDialog = true,
                currentEmployee = employee,
                nameInput = employee.name,
                positionInput = employee.position,
                salaryInput = employee.salary.toString(),
                hireDateInput = employee.hireDate,
                phoneInput = employee.phone,
                emailInput = employee.email
            )
        }
    }

    fun closeDialog() {
        _state.update { it.copy(showAddEditDialog = false, showDeleteConfirmation = false) }
    }

    fun updateNameInput(name: String) {
        _state.update { it.copy(nameInput = name) }
    }

    fun updatePositionInput(position: String) {
        _state.update { it.copy(positionInput = position) }
    }

    fun updateSalaryInput(salary: String) {
        _state.update { it.copy(salaryInput = salary) }
    }

    fun updateHireDateInput(date: Date) {
        _state.update { it.copy(hireDateInput = date) }
    }

    fun updatePhoneInput(phone: String) {
        _state.update { it.copy(phoneInput = phone) }
    }

    fun updateEmailInput(email: String) {
        _state.update { it.copy(emailInput = email) }
    }

    fun saveEmployee() {
        val currentState = _state.value
        val salary = currentState.salaryInput.toDoubleOrNull() ?: 0.0

        if (currentState.nameInput.isBlank() || currentState.positionInput.isBlank()) {
            _state.update { it.copy(error = "Name and position cannot be empty") }
            return
        }

        viewModelScope.launch {
            try {
                if (currentState.currentEmployee != null) {
                    // Update existing employee
                    val updatedEmployee = currentState.currentEmployee.copy(
                        name = currentState.nameInput,
                        position = currentState.positionInput,
                        salary = salary,
                        hireDate = currentState.hireDateInput,
                        phone = currentState.phoneInput,
                        email = currentState.emailInput
                    )
                    repository.updateEmployee(updatedEmployee)
                    _state.update { it.copy(showAddEditDialog = false) }
                } else {
                    // Add new employee with a manually assigned ID
                    val maxId = repository.getMaxEmployeeId() // Get the maximum ID from Firebase
                    val newEmployee = Employee(
                        id = maxId + 1, // Assign the next available ID
                        name = currentState.nameInput,
                        position = currentState.positionInput,
                        salary = salary,
                        hireDate = currentState.hireDateInput,
                        phone = currentState.phoneInput,
                        email = currentState.emailInput
                    )
                    repository.addEmployee(newEmployee)
                    _state.update { it.copy(showAddEditDialog = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to save employee: ${e.message}") }
            }
        }
    }

    fun confirmDelete(employee: Employee) {
        _state.update {
            it.copy(
                showDeleteConfirmation = true,
                employeeToDelete = employee
            )
        }
    }

    fun deleteEmployee() {
        val currentState = _state.value
        val employeeToDelete = currentState.employeeToDelete ?: return

        viewModelScope.launch {
            try {
                repository.deleteEmployee(employeeToDelete.id)
                _state.update {
                    it.copy(
                        showDeleteConfirmation = false,
                        employeeToDelete = null
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to delete employee: ${e.message}") }
            }
        }
    }

    companion object {
        private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        fun formatDate(date: Date): String {
            return dateFormatter.format(date)
        }

        fun formatCurrency(amount: Double): String {
            return "$%.2f".format(amount)
        }
    }
}