package com.example.coffee4n.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.example.coffee4n.model.BookingTable
import com.example.coffee4n.model.Table
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TableRepository(
    private val firebaseDatabase: FirebaseDatabase
) {
    private val tablesRef = firebaseDatabase.getReference("tables")
    private val bookingTablesRef = firebaseDatabase.getReference("bookingTables")

    // Quản lý Table
    suspend fun addTable(table: Table) {
        tablesRef.child(table.id.toString()).setValue(table).await()
    }

    suspend fun deleteTable(id: Int) {
        tablesRef.child(id.toString()).removeValue().await()
    }

    fun getTablesFlow(): Flow<List<Table>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tables = snapshot.children.mapNotNull { it.getValue(Table::class.java) }
                trySend(tables).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        tablesRef.addValueEventListener(listener)
        awaitClose { tablesRef.removeEventListener(listener) }
    }

    suspend fun getMaxTableId(): Int {
        val snapshot = tablesRef.get().await()
        val remoteTables = snapshot.children.mapNotNull { it.getValue(Table::class.java) }
        return remoteTables.maxByOrNull { it.id }?.id ?: 0
    }

    // Quản lý BookingTable
    suspend fun addBookingTable(bookingTable: BookingTable) {
        // Lấy id lớn nhất hiện có và tăng lên 1
        val maxId = getMaxBookingTableId()
        val newId = maxId + 1
        val bookingWithId = bookingTable.copy(id = newId)

        // Sử dụng id làm key trong Firebase
        bookingTablesRef.child(newId.toString()).setValue(bookingWithId).await()
    }

    fun getBookingTablesFlow(): Flow<List<BookingTable>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookingTables = snapshot.children.mapNotNull { it.getValue(BookingTable::class.java) }
                trySend(bookingTables).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        bookingTablesRef.addValueEventListener(listener)
        awaitClose { bookingTablesRef.removeEventListener(listener) }
    }

    suspend fun deleteBookingTable(id: Int) {
        bookingTablesRef.child(id.toString()).removeValue().await()
    }

    suspend fun updateBookingTable(bookingTable: BookingTable) {
        if (bookingTable.id <= 0) {
            throw IllegalArgumentException("BookingTable must have a valid ID (greater than 0) to be updated")
        } else {
            bookingTablesRef.child(bookingTable.id.toString()).setValue(bookingTable).await()
        }
    }

    suspend fun getMaxBookingTableId(): Int {
        val snapshot = bookingTablesRef.get().await()
        val remoteBookings = snapshot.children.mapNotNull { it.getValue(BookingTable::class.java) }
        return remoteBookings.maxByOrNull { it.id }?.id ?: 0
    }
}