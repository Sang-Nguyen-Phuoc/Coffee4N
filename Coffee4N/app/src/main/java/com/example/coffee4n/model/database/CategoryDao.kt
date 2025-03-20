package com.example.coffee4n.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.coffee4n.model.Category

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT * FROM category WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("DELETE FROM category WHERE id = :id")
    suspend fun deleteCategory(id: Int)
}