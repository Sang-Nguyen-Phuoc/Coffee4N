package com.example.coffee4n.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.coffee4n.model.Promotion

@Dao
interface PromotionDao {
    @Query("SELECT * FROM promotion")
    suspend fun getAllPromotions(): List<Promotion>

    @Query("SELECT * FROM promotion WHERE id = :id")
    suspend fun getPromotionById(id: Int): Promotion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotion(promotion: Promotion)

    @Query("DELETE FROM promotion WHERE id = :id")
    suspend fun deletePromotion(id: Int)

    @Query("SELECT * FROM promotion WHERE code = :code")
    suspend fun getPromotionByCode(code: String): Promotion?
}