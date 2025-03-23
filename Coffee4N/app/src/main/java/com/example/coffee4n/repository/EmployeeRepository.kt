package com.example.coffee4n.repository

import com.example.coffee4n.model.Employee
import com.example.coffee4n.model.database.EmployeeDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class EmployeeRepository(
    private val employeeDao: EmployeeDao
) {
    // Get all employees from local database
    suspend fun getAllEmployees(): List<Employee> {
        return employeeDao.getAllEmployees()
    }

    // Get employee by ID
    suspend fun getEmployeeById(id: Int): Employee? {
        return employeeDao.getEmployeeById(id)
    }

    // Add new employee to local database
    suspend fun addEmployee(employee: Employee) {
        employeeDao.insertEmployee(employee)
    }

    // Update employee information
    suspend fun updateEmployee(employee: Employee) {
        employeeDao.insertEmployee(employee)
    }

    // Delete employee
    suspend fun deleteEmployee(id: Int) {
        employeeDao.deleteEmployee(id)
    }

    // Get employees as Flow for automatic UI updates
    fun getEmployeesFlow(): Flow<List<Employee>> = flow {
        emit(employeeDao.getAllEmployees())
    }

    // Get employee by ID as Flow
    fun getEmployeeFlow(id: Int): Flow<Employee?> = flow {
        emit(employeeDao.getEmployeeById(id))
    }
}