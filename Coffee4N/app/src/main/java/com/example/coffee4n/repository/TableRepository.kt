package com.example.coffee4n.repository

import com.google.firebase.database.FirebaseDatabase
import com.example.coffee4n.model.Table
import com.example.coffee4n.model.database.TableDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class TableRepository(
    private val tableDao: TableDao,
    private val firebaseDatabase: FirebaseDatabase
) {
    suspend fun getAllTablesFromLocal(): List<Table> {
        return tableDao.getAllTables()
    }

    suspend fun getTableById(id: Int): Table? {
        return tableDao.getTableById(id)
    }

    private suspend fun syncTablesFromRemote() {
        val snapshot = firebaseDatabase.getReference("tables").get().await()
        val tables = snapshot.children.mapNotNull { it.getValue(Table::class.java) }
        tables.forEach { tableDao.insertTable(it) }
    }

    suspend fun addTable(table: Table) {
        tableDao.insertTable(table)
        firebaseDatabase.getReference("tables").child(table.id.toString()).setValue(table).await()
    }

    suspend fun deleteTable(id: Int) {
        tableDao.deleteTable(id)
        firebaseDatabase.getReference("tables").child(id.toString()).removeValue().await()
    }

    fun getTablesFlow(): Flow<List<Table>> = flow {
        emit(tableDao.getAllTables())
        syncTablesFromRemote()
        emit(tableDao.getAllTables())
    }
}