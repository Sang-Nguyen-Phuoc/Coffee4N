package com.example.coffee4n.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.coffee4n.model.Employee

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employee")
    suspend fun getAllEmployees(): List<Employee>

    @Query("SELECT * FROM employee WHERE id = :id")
    suspend fun getEmployeeById(id: Int): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    @Query("DELETE FROM employee WHERE id = :id")
    suspend fun deleteEmployee(id: Int)
}