package com.example.coffee4n.repository

import com.google.firebase.database.FirebaseDatabase
import com.example.coffee4n.model.Booking
import com.example.coffee4n.model.database.BookingDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class BookingRepository(
    private val bookingDao: BookingDao,
    private val firebaseDatabase: FirebaseDatabase
) {
    suspend fun getAllBookingsFromLocal(): List<Booking> {
        return bookingDao.getAllBookings()
    }

    suspend fun getBookingsByUser(userId: Int): List<Booking> {
        return bookingDao.getBookingsByUser(userId)
    }

    private suspend fun syncBookingsFromRemote(userId: Int) {
        val snapshot = firebaseDatabase.getReference("bookings").child(userId.toString()).get().await()
        val bookings = snapshot.children.mapNotNull { it.getValue(Booking::class.java) }
        bookings.forEach { bookingDao.insertBooking(it) }
    }

    suspend fun addBooking(booking: Booking) {
        bookingDao.insertBooking(booking)
        firebaseDatabase.getReference("bookings").child(booking.userId.toString()).child(booking.id.toString()).setValue(booking).await()
    }

    suspend fun deleteBooking(id: Int, userId: Int) {
        bookingDao.deleteBooking(id)
        firebaseDatabase.getReference("bookings").child(userId.toString()).child(id.toString()).removeValue().await()
    }

    fun getBookingsFlow(userId: Int): Flow<List<Booking>> = flow {
        emit(bookingDao.getBookingsByUser(userId))
        syncBookingsFromRemote(userId)
        emit(bookingDao.getBookingsByUser(userId))
    }
}