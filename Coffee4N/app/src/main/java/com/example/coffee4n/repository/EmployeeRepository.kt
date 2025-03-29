package com.example.coffee4n.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.coffee4n.model.Employee
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EmployeeRepository(
    private val firebaseDatabase: FirebaseDatabase
) {
    // Fetch employees directly from Firebase as a Flow
    fun getEmployeesFlow(): Flow<List<Employee>> = callbackFlow {
        val reference = firebaseDatabase.getReference("employees")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val employees = snapshot.children.mapNotNull { it.getValue(Employee::class.java) }
                trySend(employees).isSuccess // Emit the list of employees
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Close the Flow on error
            }
        }

        reference.addValueEventListener(listener)

        awaitClose {
            reference.removeEventListener(listener) // Clean up the listener when the Flow is closed
        }
    }

    // Fetch a single employee by ID as a Flow
    fun getEmployeeFlow(id: Int): Flow<Employee?> = callbackFlow {
        val reference = firebaseDatabase.getReference("employees").child(id.toString())
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val employee = snapshot.getValue(Employee::class.java)
                trySend(employee).isSuccess // Emit the employee
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Close the Flow on error
            }
        }

        reference.addValueEventListener(listener)

        awaitClose {
            reference.removeEventListener(listener) // Clean up the listener
        }
    }

    // Add or update an employee in Firebase
    suspend fun addEmployee(employee: Employee) {
        firebaseDatabase.getReference("employees").child(employee.id.toString()).setValue(employee).await()
    }

    // Update an employee in Firebase (same as add, since Firebase overwrites)
    suspend fun updateEmployee(employee: Employee) {
        firebaseDatabase.getReference("employees").child(employee.id.toString()).setValue(employee).await()
    }

    // Delete an employee from Firebase
    suspend fun deleteEmployee(id: Int) {
        firebaseDatabase.getReference("employees").child(id.toString()).removeValue().await()
    }

    // Get the maximum ID from Firebase
    suspend fun getMaxEmployeeId(): Int {
        val snapshot = firebaseDatabase.getReference("employees").get().await()
        val remoteEmployees = snapshot.children.mapNotNull { it.getValue(Employee::class.java) }
        return remoteEmployees.maxByOrNull { it.id }?.id ?: 0
    }
}