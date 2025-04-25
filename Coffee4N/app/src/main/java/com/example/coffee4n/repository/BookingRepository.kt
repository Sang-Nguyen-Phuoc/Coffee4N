package com.example.coffee4n.repository

import com.google.firebase.database.FirebaseDatabase
import com.example.coffee4n.model.Booking
import com.example.coffee4n.model.database.BookingDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import com.example.coffee4n.session.Models
import com.example.coffee4n.session.OwnerSession

class BookingRepository(
    private val bookingDao: BookingDao,
    private val firebaseDatabase: FirebaseDatabase
) {
    private val bookingRef = firebaseDatabase.getReference(OwnerSession.getReferencePath(model = "bookings"))
    suspend fun getAllBookingsFromLocal(): List<Booking> {
        return bookingDao.getAllBookings()
    }

    suspend fun getBookingsByUser(userId: Int): List<Booking> {
        return bookingDao.getBookingsByUser(userId)
    }

    private suspend fun syncBookingsFromRemote(userId: Int) {
        val snapshot = bookingRef.child(userId.toString()).get().await()
        val bookings = snapshot.children.mapNotNull { it.getValue(Booking::class.java) }
        bookings.forEach { bookingDao.insertBooking(it) }
    }

    suspend fun addBooking(booking: Booking) {
        bookingDao.insertBooking(booking)
        bookingRef.child(booking.userId.toString()).child(booking.id.toString()).setValue(booking).await()
    }

    suspend fun deleteBooking(id: Int, userId: Int) {
        bookingDao.deleteBooking(id)
        bookingRef.child(userId.toString()).child(id.toString()).removeValue().await()
    }

    fun getBookingsFlow(userId: Int): Flow<List<Booking>> = flow {
        emit(bookingDao.getBookingsByUser(userId))
        syncBookingsFromRemote(userId)
        emit(bookingDao.getBookingsByUser(userId))
    }
}