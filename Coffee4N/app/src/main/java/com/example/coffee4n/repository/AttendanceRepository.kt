package com.example.coffee4n.repository

import com.google.firebase.database.FirebaseDatabase
import com.example.coffee4n.model.Attendance
import com.example.coffee4n.session.OwnerSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class AttendanceRepository(
    private val firebaseDatabase: FirebaseDatabase
) {
    private val attendanceRef = firebaseDatabase.getReference(OwnerSession.getReferencePath(model = "attendances"))

    // Cache để lưu attendance đã fetch từ Firebase
    private val attendanceCache = mutableMapOf<Int, MutableList<Attendance>>()

    suspend fun getAllAttendancesFromLocal(): List<Attendance> {
        return attendanceCache.values.flatten()
    }

    suspend fun getAttendancesByEmployee(employeeId: Int): List<Attendance> {
        // Trả về từ cache nếu có
        attendanceCache[employeeId]?.let { return it }

        // Nếu không có, fetch từ Firebase
        syncAttendancesFromRemote(employeeId)
        return attendanceCache[employeeId] ?: emptyList()
    }

    private suspend fun syncAttendancesFromRemote(employeeId: Int) {
        val snapshot = attendanceRef.child(employeeId.toString()).get().await()
        val attendances = snapshot.children.mapNotNull { it.getValue(Attendance::class.java) }

        // Cập nhật cache
        attendanceCache[employeeId] = attendances.toMutableList()
    }

    suspend fun addAttendance(attendance: Attendance) {
        // Thêm vào cache
        val employeeAttendances = attendanceCache.getOrPut(attendance.employeeId) { mutableListOf() }
        employeeAttendances.add(attendance)

        // Thêm vào Firebase
        attendanceRef.child(attendance.employeeId.toString())
            .child(attendance.id.toString())
            .setValue(attendance).await()
    }

    suspend fun deleteAttendance(id: Int, employeeId: Int) {
        // Xóa khỏi cache
        attendanceCache[employeeId]?.removeIf { it.id == id }

        // Xóa khỏi Firebase
        attendanceRef.child(employeeId.toString())
            .child(id.toString())
            .removeValue().await()
    }

    fun getAttendancesFlow(employeeId: Int): Flow<List<Attendance>> = flow {
        // Emit từ cache trước nếu có
        attendanceCache[employeeId]?.let { emit(it) }

        // Sau đó fetch từ remote và emit lại
        syncAttendancesFromRemote(employeeId)
        emit(attendanceCache[employeeId] ?: emptyList())
    }

    // Xóa cache khi logout
    fun clearCache() {
        attendanceCache.clear()
    }

    // Cập nhật một attendance
    suspend fun updateAttendance(attendance: Attendance) {
        // Cập nhật trong cache
        val employeeAttendances = attendanceCache[attendance.employeeId]
        employeeAttendances?.let {
            val index = it.indexOfFirst { a -> a.id == attendance.id }
            if (index != -1) {
                it[index] = attendance
            } else {
                it.add(attendance)
            }
        }

        // Cập nhật trong Firebase
        attendanceRef.child(attendance.employeeId.toString())
            .child(attendance.id.toString())
            .setValue(attendance).await()
    }
}