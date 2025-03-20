package com.example.coffee4n.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.coffee4n.model.Booking

@Dao
interface BookingDao {
    @Query("SELECT * FROM booking")
    suspend fun getAllBookings(): List<Booking>

    @Query("SELECT * FROM booking WHERE userId = :userId")
    suspend fun getBookingsByUser(userId: Int): List<Booking>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking)

    @Query("DELETE FROM booking WHERE id = :id")
    suspend fun deleteBooking(id: Int)
}