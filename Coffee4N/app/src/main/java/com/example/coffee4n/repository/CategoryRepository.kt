package com.example.coffee4n.repository

import com.example.coffee4n.model.Category
import com.example.coffee4n.session.LastIds
import com.example.coffee4n.session.Models
import com.example.coffee4n.session.OwnerSession
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resumeWithException

class CategoryRepository(
    private val firebaseDatabase: FirebaseDatabase
) {
    private val categoryRef = firebaseDatabase.getReference(OwnerSession.getReferencePath(model = Models.Category))
    private val lastCategoryIdRef = firebaseDatabase.getReference(OwnerSession.getMetadataPath(lastModelId = LastIds.Category))

    // Fetch categories as a Flow
    fun getCategoriesFlow(): Flow<List<Category>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categories = snapshot.children.mapNotNull { it.getValue(Category::class.java) }
                trySend(categories).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        categoryRef.addValueEventListener(listener)

        awaitClose {
            categoryRef.removeEventListener(listener)
        }
    }

    // Fetch a single category by ID as a Flow
    fun getCategoryFlow(id: Int): Flow<Category?> = callbackFlow {
        val reference = categoryRef.child(id.toString())
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val category = snapshot.getValue(Category::class.java)
                trySend(category).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        reference.addValueEventListener(listener)

        awaitClose {
            reference.removeEventListener(listener)
        }
    }

    // Add a new category with auto-increment ID
    suspend fun addCategory(category: Category) {
        val newId = getNextCategoryId()
        val newCategory = category.copy(id = newId)
        categoryRef.child(newId.toString()).setValue(newCategory).await()
    }

    // Update an existing category
    suspend fun updateCategory(category: Category) {
        categoryRef.child(category.id.toString()).setValue(category).await()
    }

    // Delete a category from Firebase
    suspend fun deleteCategory(id: Int) {
        categoryRef.child(id.toString()).removeValue().await()
    }

    // Get next category ID using metadata/lastCategoryId
    private suspend fun getNextCategoryId(): Int {
        return suspendCancellableCoroutine { continuation ->
            lastCategoryIdRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val lastId = mutableData.getValue(Int::class.java) ?: 0
                    val nextId = lastId + 1
                    mutableData.value = nextId
                    return Transaction.success(mutableData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (continuation.isActive) {
                        if (error != null) {
                            continuation.resumeWithException(error.toException())
                        } else {
                            val nextId = currentData?.getValue(Int::class.java) ?: 1
                            continuation.resume(nextId) {}
                        }
                    }
                }
            })
        }
    }
}
