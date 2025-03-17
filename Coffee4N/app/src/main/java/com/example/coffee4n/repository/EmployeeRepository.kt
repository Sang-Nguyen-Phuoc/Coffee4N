package com.example.coffee4n.repository

import com.google.firebase.database.FirebaseDatabase
import com.example.coffee4n.model.Employee
import com.example.coffee4n.model.database.EmployeeDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class EmployeeRepository(
    private val employeeDao: EmployeeDao,
    private val firebaseDatabase: FirebaseDatabase
) {
    // Lấy tất cả nhân viên từ local (RoomDB)
    suspend fun getAllEmployeesFromLocal(): List<Employee> {
        return employeeDao.getAllEmployees()
    }

    // Lấy nhân viên theo ID từ local
    suspend fun getEmployeeById(id: Int): Employee? {
        return employeeDao.getEmployeeById(id)
    }

    // Đồng bộ danh sách nhân viên từ Firebase
    private suspend fun syncEmployeesFromRemote() {
        val snapshot = firebaseDatabase.getReference("employees").get().await()
        val employees = snapshot.children.mapNotNull { it.getValue(Employee::class.java) }
        employees.forEach { employeeDao.insertEmployee(it) }
    }

    // Thêm nhân viên mới (lưu vào local và đẩy lên Firebase)
    suspend fun addEmployee(employee: Employee) {
        employeeDao.insertEmployee(employee)
        firebaseDatabase.getReference("employees").child(employee.id.toString()).setValue(employee).await()
    }

    // Cập nhật thông tin nhân viên
    suspend fun updateEmployee(employee: Employee) {
        employeeDao.insertEmployee(employee)
        firebaseDatabase.getReference("employees").child(employee.id.toString()).setValue(employee).await()
    }

    // Xóa nhân viên
    suspend fun deleteEmployee(id: Int) {
        employeeDao.deleteEmployee(id)
        firebaseDatabase.getReference("employees").child(id.toString()).removeValue().await()
    }

    // Lấy danh sách nhân viên dưới dạng Flow để UI tự động cập nhật
    fun getEmployeesFlow(): Flow<List<Employee>> = flow {
        emit(employeeDao.getAllEmployees())
        syncEmployeesFromRemote()
        emit(employeeDao.getAllEmployees())
    }

    // Lấy nhân viên theo ID dưới dạng Flow
    fun getEmployeeFlow(id: Int): Flow<Employee?> = flow {
        emit(employeeDao.getEmployeeById(id))
        syncEmployeesFromRemote()
        emit(employeeDao.getEmployeeById(id))
    }
}