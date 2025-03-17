package com.example.coffee4n.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.coffee4n.model.Attendance

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance")
    suspend fun getAllAttendances(): List<Attendance>

    @Query("SELECT * FROM attendance WHERE employeeId = :employeeId")
    suspend fun getAttendancesByEmployee(employeeId: Int): List<Attendance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Query("DELETE FROM attendance WHERE id = :id")
    suspend fun deleteAttendance(id: Int)
}