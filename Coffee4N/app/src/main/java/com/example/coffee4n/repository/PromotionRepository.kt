package com.example.coffee4n.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.example.coffee4n.model.Promotion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class PromotionRepository(
    private val firebaseDatabase: FirebaseDatabase
) {

    suspend fun getPromotionById(id: Int): Promotion? {
        val snapshot = firebaseDatabase.getReference("promotions").get().await()
        val promotions = snapshot.children.mapNotNull { parsePromotion(it) }
        val promotion = promotions.firstOrNull { it.id == id }
        return promotion
    }

    suspend fun getPromotionByCode(code: String): Promotion? {
        val snapshot = firebaseDatabase.getReference("promotions").get().await()
        val promotions = snapshot.children.mapNotNull { parsePromotion(it) }
        val promotion = promotions.firstOrNull { it.code == code }
        return promotion
    }

    suspend fun addPromotion(promotion: Promotion) {
        val snapshot = firebaseDatabase.getReference("promotions").get().await()
        val promotions = snapshot.children.mapNotNull { parsePromotion(it) }.toMutableList()
        val existingIndex = promotions.indexOfFirst { it.id == promotion.id }
        if (existingIndex != -1) {
            promotions[existingIndex] = promotion
        } else {
            promotions.add(promotion)
        }
        firebaseDatabase.getReference("promotions").setValue(promotions).await()
    }

    suspend fun deletePromotion(id: Int) {
        val snapshot = firebaseDatabase.getReference("promotions").get().await()
        val promotions = snapshot.children.mapNotNull { parsePromotion(it) }.toMutableList()
        val updatedPromotions = promotions.filter { it.id != id }
        firebaseDatabase.getReference("promotions").setValue(updatedPromotions).await()
    }

    fun getPromotionsFlow(): Flow<List<Promotion>> = flow {
        val snapshot = firebaseDatabase.getReference("promotions").get().await()
        val promotions = snapshot.children.mapNotNull { parsePromotion(it) }
        emit(promotions)
    }

    private fun parsePromotion(snapshot: DataSnapshot): Promotion? {
        // Kiểm tra nếu snapshot là null hoặc không có giá trị
        if (!snapshot.exists()) {
            println("Snapshot is null or does not exist")
            return null
        }

        return try {
            val id = snapshot.child("id").getValue(Int::class.java) ?: return null
            val code = snapshot.child("code").getValue(String::class.java) ?: return null
            val description = snapshot.child("description").getValue(String::class.java) ?: return null
            val discountType = snapshot.child("discountType").getValue(String::class.java) ?: return null
            val discountValue = snapshot.child("discountValue").getValue(Double::class.java) ?: return null
            val startDateStr = snapshot.child("startDate").getValue(String::class.java) ?: return null
            val endDateStr = snapshot.child("endDate").getValue(String::class.java) ?: return null

            Promotion(
                id = id,
                code = code,
                description = description,
                discountType = discountType,
                discountValue = discountValue,
                startDate = Promotion.fromIsoString(startDateStr),
                endDate = Promotion.fromIsoString(endDateStr)
            )
        } catch (e: Exception) {
            println("Error parsing promotion: ${e.message}")
            null
        }
    }
}