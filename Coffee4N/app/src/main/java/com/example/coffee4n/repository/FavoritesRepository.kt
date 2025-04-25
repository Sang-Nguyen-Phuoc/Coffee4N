package com.example.coffee4n.repository

import com.example.coffee4n.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.example.coffee4n.session.LastIds
import com.example.coffee4n.session.Models
import com.example.coffee4n.session.OwnerSession

class FavoritesRepository(
    private val firebaseDatabase: FirebaseDatabase
) {
    private val favoritesRef = firebaseDatabase.getReference(OwnerSession.getReferencePath(model = Models.Favorite))

    fun getFavoritesFlow(userId: Int): Flow<List<Product>> = callbackFlow {
        val userFavoritesRef = favoritesRef.child(userId.toString())

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favorites = snapshot.children.mapNotNull { it.getValue(Product::class.java) }
                trySend(favorites).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        userFavoritesRef.addValueEventListener(listener)
        awaitClose { userFavoritesRef.removeEventListener(listener) }
    }

    suspend fun addToFavorites(userId: Int, product: Product) {
        favoritesRef.child(userId.toString()).child(product.id.toString()).setValue(product).await()
    }

    suspend fun removeFromFavorites(userId: Int, productId: Int) {
        favoritesRef.child(userId.toString()).child(productId.toString()).removeValue().await()
    }

    suspend fun isProductInFavorites(userId: Int, productId: Int): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val reference = favoritesRef.child(userId.toString()).child(productId.toString())
            reference.get().addOnSuccessListener { snapshot ->
                if (continuation.isActive) {
                    continuation.resume(snapshot.exists())
                }
            }.addOnFailureListener { exception ->
                if (continuation.isActive) {
                    continuation.resumeWithException(exception)
                }
            }
        }
    }
}