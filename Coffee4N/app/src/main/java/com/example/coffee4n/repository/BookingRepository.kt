package com.example.coffee4n.repository

import com.google.firebase.database.FirebaseDatabase
import com.example.coffee4n.model.Booking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import com.example.coffee4n.session.Models
import com.example.coffee4n.session.OwnerSession

class BookingRepository(
    private val firebaseDatabase: FirebaseDatabase
) {
    private val bookingRef = firebaseDatabase.getReference(OwnerSession.getReferencePath(model = "bookings"))

    // Cache để lưu booking đã fetch từ Firebase
    private val bookingCache = mutableMapOf<Int, MutableList<Booking>>()

    suspend fun getAllBookingsFromLocal(): List<Booking> {
        return bookingCache.values.flatten()
    }

    suspend fun getBookingsByUser(userId: Int): List<Booking> {
        // Trả về từ cache nếu có
        bookingCache[userId]?.let { return it }

        // Nếu không có, fetch từ Firebase
        syncBookingsFromRemote(userId)
        return bookingCache[userId] ?: emptyList()
    }

    private suspend fun syncBookingsFromRemote(userId: Int) {
        val snapshot = bookingRef.child(userId.toString()).get().await()
        val bookings = snapshot.children.mapNotNull { it.getValue(Booking::class.java) }

        // Cập nhật cache
        bookingCache[userId] = bookings.toMutableList()
    }

    suspend fun addBooking(booking: Booking) {
        // Thêm vào cache
        val userBookings = bookingCache.getOrPut(booking.userId) { mutableListOf() }
        userBookings.add(booking)

        // Thêm vào Firebase
        bookingRef.child(booking.userId.toString()).child(booking.id.toString()).setValue(booking).await()
    }

    suspend fun deleteBooking(id: Int, userId: Int) {
        // Xóa khỏi cache
        bookingCache[userId]?.removeIf { it.id == id }

        // Xóa khỏi Firebase
        bookingRef.child(userId.toString()).child(id.toString()).removeValue().await()
    }

    fun getBookingsFlow(userId: Int): Flow<List<Booking>> = flow {
        // Emit từ cache trước nếu có
        bookingCache[userId]?.let { emit(it) }

        // Sau đó fetch từ remote và emit lại
        syncBookingsFromRemote(userId)
        emit(bookingCache[userId] ?: emptyList())
    }

    // Xóa cache khi logout
    fun clearCache() {
        bookingCache.clear()
    }

    // Cập nhật một booking
    suspend fun updateBooking(booking: Booking) {
        // Cập nhật trong cache
        val userBookings = bookingCache[booking.userId]
        userBookings?.let {
            val index = it.indexOfFirst { b -> b.id == booking.id }
            if (index != -1) {
                it[index] = booking
            } else {
                it.add(booking)
            }
        }

        // Cập nhật trong Firebase
        bookingRef.child(booking.userId.toString()).child(booking.id.toString()).setValue(booking).await()
    }
}