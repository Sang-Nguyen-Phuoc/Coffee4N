package com.example.coffee4n.ui.owner_employee

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.App
import com.example.coffee4n.model.Employee
import com.example.coffee4n.model.database.AppDatabase
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
        val database = AppDatabase.getDatabase(application)
        repository = EmployeeRepository(
            database.employeeDao()
        )
        loadEmployees()
    }

    fun loadEmployees() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // Collect initial employees from the repository's flow
                viewModelScope.launch {
                    repository.getEmployeesFlow().collect { employees ->
                        _state.update { it.copy(
                            employees = employees,
                            isLoading = false,
                            error = null
                        ) }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = "Failed to load employees: ${e.message}"
                ) }
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
            // Show validation error
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

                    // Force a UI refresh by updating the state directly
                    _state.update { it.copy(
                        showAddEditDialog = false,
                        // This forces the UI to update with the edited employee
                        employees = currentState.employees.map {
                            if (it.id == updatedEmployee.id) updatedEmployee else it
                        }
                    )}
                } else {
                    // Add new employee
                    val maxId = currentState.employees.maxByOrNull { it.id }?.id ?: 0
                    val newEmployee = Employee(
                        id = maxId + 1,
                        name = currentState.nameInput,
                        position = currentState.positionInput,
                        salary = salary,
                        hireDate = currentState.hireDateInput,
                        phone = currentState.phoneInput,
                        email = currentState.emailInput
                    )

                    repository.addEmployee(newEmployee)

                    // Force a UI refresh by updating the state directly
                    _state.update { it.copy(
                        showAddEditDialog = false,
                        // This forces the UI to update with the new employee
                        employees = currentState.employees + newEmployee
                    )}
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
                // loadEmployees() // No need to reload as the Flow will update automatically
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