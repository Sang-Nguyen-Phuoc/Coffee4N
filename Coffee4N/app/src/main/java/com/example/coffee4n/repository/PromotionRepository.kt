package com.example.coffee4n.repository

import com.google.firebase.database.DataSnapshot import com.google.firebase.database.FirebaseDatabase import com.example.coffee4n.model.Promotion
import com.example.coffee4n.session.LastIds
import com.example.coffee4n.session.Models
import com.example.coffee4n.session.OwnerSession
import kotlinx.coroutines.channels.awaitClose import kotlinx.coroutines.flow.Flow import kotlinx.coroutines.flow.callbackFlow import kotlinx.coroutines.tasks.await

class PromotionRepository( private val firebaseDatabase: FirebaseDatabase ) {
    private val promotionRef = firebaseDatabase.getReference(OwnerSession.getReferencePath(model = Models.Promotion))
    
    suspend fun getPromotionById(id: Int): Promotion? {
        val snapshot = promotionRef.get().await();
        return snapshot.children.mapNotNull { parsePromotion(it) }.firstOrNull { it.id == id } 
    }
    suspend fun getPromotionByCode(code: String): Promotion? {
        val snapshot = promotionRef.get().await()
        return snapshot.children.mapNotNull { parsePromotion(it) }.firstOrNull { it.code == code }
    }

    suspend fun addPromotion(promotion: Promotion): Promotion {
        val promotionsRef = promotionRef
        val snapshot = promotionsRef.get().await()
        val promotions = snapshot.children.mapNotNull { parsePromotion(it) }

        // Generate a new unique ID
        val newId = if (promotions.isEmpty()) 1 else promotions.maxOf { it.id } + 1

        // Create a new promotion with the generated ID
        val promotionWithId = promotion.copy(id = newId)

        // Add the new promotion to Firebase
        promotionsRef.push().setValue(promotionWithId).await()
        return promotionWithId
    }

    suspend fun deletePromotion(id: Int) {
        val promotionsRef = promotionRef
        val snapshot = promotionsRef.get().await()
        val index = snapshot.children.mapNotNull { parsePromotion(it) }.indexOfFirst { it.id == id }
        if (index != -1) {
            promotionsRef.child(index.toString()).removeValue().await()
        }
    }

    fun getPromotionsFlow(): Flow<List<Promotion>> = callbackFlow {
        val promotionsRef = promotionRef
        val listener = promotionsRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val promotions = snapshot.children.mapNotNull { parsePromotion(it) }
                trySend(promotions).isSuccess
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        })

        awaitClose { promotionsRef.removeEventListener(listener) }
    }

    private fun parsePromotion(snapshot: DataSnapshot): Promotion? {
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
            val isActive = snapshot.child("isActive").getValue(Boolean::class.java) ?: true

            val startDateTimestamp = snapshot.child("startDate").child("time").getValue(Long::class.java)
            val endDateTimestamp = snapshot.child("endDate").child("time").getValue(Long::class.java)

            val startDate = Promotion.fromFirebaseTimestamp(startDateTimestamp) ?: return null
            val endDate = Promotion.fromFirebaseTimestamp(endDateTimestamp) ?: return null

            Promotion(
                id = id,
                code = code,
                description = description,
                discountType = discountType,
                discountValue = discountValue,
                startDate = startDate,
                endDate = endDate,
                isActive = isActive
            )
        } catch (e: Exception) {
            println("Error parsing promotion: ${e.message}")
            null
        }
    }
}