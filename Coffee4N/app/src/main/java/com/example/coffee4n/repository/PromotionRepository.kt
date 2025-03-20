package com.example.coffee4n.repository

import com.google.firebase.database.FirebaseDatabase
import com.example.coffee4n.model.Promotion
import com.example.coffee4n.model.database.PromotionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class PromotionRepository(
    private val promotionDao: PromotionDao,
    private val firebaseDatabase: FirebaseDatabase
) {
    suspend fun getAllPromotionsFromLocal(): List<Promotion> {
        return promotionDao.getAllPromotions()
    }

    suspend fun getPromotionById(id: Int): Promotion? {
        return promotionDao.getPromotionById(id)
    }

    private suspend fun syncPromotionsFromRemote() {
        val snapshot = firebaseDatabase.getReference("promotions").get().await()
        val promotions = snapshot.children.mapNotNull { it.getValue(Promotion::class.java) }
        promotions.forEach { promotionDao.insertPromotion(it) }
    }

    suspend fun addPromotion(promotion: Promotion) {
        promotionDao.insertPromotion(promotion)
        firebaseDatabase.getReference("promotions").child(promotion.id.toString()).setValue(promotion).await()
    }

    suspend fun deletePromotion(id: Int) {
        promotionDao.deletePromotion(id)
        firebaseDatabase.getReference("promotions").child(id.toString()).removeValue().await()
    }

    fun getPromotionsFlow(): Flow<List<Promotion>> = flow {
        emit(promotionDao.getAllPromotions())
        syncPromotionsFromRemote()
        emit(promotionDao.getAllPromotions())
    }
}