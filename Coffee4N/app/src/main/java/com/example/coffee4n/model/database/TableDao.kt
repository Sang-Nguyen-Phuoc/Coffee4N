package com.example.coffee4n.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.coffee4n.model.Table
import kotlinx.coroutines.flow.Flow

@Dao
interface TableDao {
    @Query("SELECT * FROM table_info")
    suspend fun getAllTables(): List<Table>

    @Query("SELECT * FROM table_info")
    fun getAllTablesFlow(): Flow<List<Table>> // New method for Flow

    @Query("SELECT * FROM table_info WHERE id = :id")
    suspend fun getTableById(id: Int): Table?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: Table)

    @Query("DELETE FROM table_info WHERE id = :id")
    suspend fun deleteTable(id: Int)
}