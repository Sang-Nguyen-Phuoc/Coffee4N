package com.example.coffee4n.repository

import com.google.firebase.database.FirebaseDatabase
import com.example.coffee4n.model.Attendance
import com.example.coffee4n.model.database.AttendanceDao
import com.example.coffee4n.session.OwnerSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class AttendanceRepository(
    private val attendanceDao: AttendanceDao,
    private val firebaseDatabase: FirebaseDatabase
) {
    suspend fun getAllAttendancesFromLocal(): List<Attendance> {
        return attendanceDao.getAllAttendances()
    }

    suspend fun getAttendancesByEmployee(employeeId: Int): List<Attendance> {
        return attendanceDao.getAttendancesByEmployee(employeeId)
    }

    private suspend fun syncAttendancesFromRemote(employeeId: Int) {
        val snapshot = firebaseDatabase.getReference(OwnerSession.getReferencePath(model = "attendances")).child(employeeId.toString()).get().await()
        val attendances = snapshot.children.mapNotNull { it.getValue(Attendance::class.java) }
        attendances.forEach { attendanceDao.insertAttendance(it) }
    }

    suspend fun addAttendance(attendance: Attendance) {
        attendanceDao.insertAttendance(attendance)
        firebaseDatabase.getReference(OwnerSession.getReferencePath(model = "attendances")).child(attendance.employeeId.toString()).child(attendance.id.toString()).setValue(attendance).await()
    }

    suspend fun deleteAttendance(id: Int, employeeId: Int) {
        attendanceDao.deleteAttendance(id)
        firebaseDatabase.getReference(OwnerSession.getReferencePath(model = "attendances")).child(employeeId.toString()).child(id.toString()).removeValue().await()
    }

    fun getAttendancesFlow(employeeId: Int): Flow<List<Attendance>> = flow {
        emit(attendanceDao.getAttendancesByEmployee(employeeId))
        syncAttendancesFromRemote(employeeId)
        emit(attendanceDao.getAttendancesByEmployee(employeeId))
    }
}