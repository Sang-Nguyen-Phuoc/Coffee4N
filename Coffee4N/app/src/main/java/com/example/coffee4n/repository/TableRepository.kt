package com.example.coffee4n.repository

import com.google.firebase.database.FirebaseDatabase
import com.example.coffee4n.model.Table
import com.example.coffee4n.model.database.TableDao
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.tasks.await

class TableRepository(
    private val tableDao: TableDao,
    private val firebaseDatabase: FirebaseDatabase
) {
    private suspend fun syncTablesFromRemote() {
        val snapshot = firebaseDatabase.getReference("tables").get().await()
        val remoteTables = snapshot.children.mapNotNull { it.getValue(Table::class.java) }
        val localTables = tableDao.getAllTables()

        remoteTables.forEach { remoteTable ->
            // Check if the remote table's ID conflicts with a local table
            val localTableWithSameId = localTables.find { it.id == remoteTable.id }
            if (localTableWithSameId == null) {
                // No conflict, insert the remote table
                tableDao.insertTable(remoteTable)
            } else {
                // Conflict: Skip or handle (e.g., update the local table with remote data)
                // For simplicity, let's skip the remote table if its ID is already in use
                println("ID conflict: Skipping remote table with ID ${remoteTable.id}")
            }
        }
    }

    suspend fun addTable(table: Table) {
        firebaseDatabase.getReference("tables").child(table.id.toString()).setValue(table).await()
    }

    suspend fun deleteTable(id: Int) {
        firebaseDatabase.getReference("tables").child(id.toString()).removeValue().await()
    }

    fun getTablesFlow(): Flow<List<Table>> = callbackFlow {
        val reference = firebaseDatabase.getReference("tables")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tables = snapshot.children.mapNotNull { it.getValue(Table::class.java) }
                trySend(tables).isSuccess // Emit the list of tables
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

    // New method to get the maximum ID from both Room and Firebase
    suspend fun getMaxTableId(): Int {
        val snapshot = firebaseDatabase.getReference("tables").get().await()
        val remoteTables = snapshot.children.mapNotNull { it.getValue(Table::class.java) }
        return remoteTables.maxByOrNull { it.id }?.id ?: 0
    }
}