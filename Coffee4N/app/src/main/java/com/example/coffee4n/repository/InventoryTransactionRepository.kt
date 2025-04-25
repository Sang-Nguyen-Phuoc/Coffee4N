package com.example.coffee4n.repository

import com.example.coffee4n.model.InventoryTransaction
import com.example.coffee4n.model.TransactionType
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.example.coffee4n.session.LastIds
import com.example.coffee4n.session.Models
import com.example.coffee4n.session.OwnerSession

class InventoryTransactionRepository(
    private val firebaseDatabase: FirebaseDatabase,
    private val ingredientRepository: IngredientRepository
) {
    private val transactionRef = firebaseDatabase.getReference(OwnerSession.getReferencePath(model = Models.InventoryTransaction))
    private val lastTransactionIdRef = firebaseDatabase.getReference(OwnerSession.getMetadataPath(lastModelId = LastIds.InventoryTransaction))

    init {
        initializeDefaultTransactions()
    }

    fun getTransactionsFlow(): Flow<List<InventoryTransaction>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transactions = snapshot.children.mapNotNull {
                    it.getValue(InventoryTransaction::class.java)
                }
                trySend(transactions).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        transactionRef.addValueEventListener(listener)
        awaitClose { transactionRef.removeEventListener(listener) }
    }

    suspend fun addTransaction(transaction: InventoryTransaction) {
        val newId = getNextTransactionId()
        val newTransaction = transaction.copy(id = newId)
        transactionRef.child(newId.toString()).setValue(newTransaction).await()

        val delta = when (transaction.type) {
            TransactionType.IMPORT -> transaction.quantity
            TransactionType.EXPORT -> -transaction.quantity
        }
        ingredientRepository.changeQuantityById(transaction.itemId, delta)
    }

    suspend fun deleteTransaction(transactionId: Int) {
        val snapshot = transactionRef.child(transactionId.toString()).get().await()
        val transaction = snapshot.getValue(InventoryTransaction::class.java)
            ?: throw IllegalArgumentException("Transaction $transactionId not found")

        val delta = when (transaction.type) {
            TransactionType.IMPORT -> -transaction.quantity
            TransactionType.EXPORT -> transaction.quantity
        }
        transactionRef.child(transactionId.toString()).removeValue().await()
        ingredientRepository.changeQuantityById(transaction.itemId, delta)
    }

    suspend fun updateTransaction(transactionId: Int, updated: InventoryTransaction) {
        val snapshot = transactionRef.child(transactionId.toString()).get().await()
        val old = snapshot.getValue(InventoryTransaction::class.java)
            ?: throw IllegalArgumentException("Transaction $transactionId not found")

        val rollbackDelta = when (old.type) {
            TransactionType.IMPORT -> -old.quantity
            TransactionType.EXPORT -> old.quantity
        }

        val newDelta = when (updated.type) {
            TransactionType.IMPORT -> updated.quantity
            TransactionType.EXPORT -> -updated.quantity
        }

        val netDelta = rollbackDelta + newDelta

        transactionRef.child(transactionId.toString()).setValue(updated).await()
        ingredientRepository.changeQuantityById(updated.itemId, netDelta)
    }

    private suspend fun getNextTransactionId(): Int {
        return suspendCancellableCoroutine { continuation ->
            lastTransactionIdRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val currentId = currentData.getValue(Int::class.java) ?: 0
                    currentData.value = currentId + 1
                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (error != null) {
                        continuation.resumeWithException(error.toException())
                    } else {
                        continuation.resume(currentData?.getValue(Int::class.java) ?: 0)
                    }
                }
            })
        }
    }

    private fun initializeDefaultTransactions() {
        transactionRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                val currentTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val defaultTransactions = listOf(
                    InventoryTransaction(id = 1, itemId = 1, timestamp = currentTime, quantity = 50, unit = "bag", type = TransactionType.IMPORT, unitPrice = 2.08),
                    InventoryTransaction(id = 2, itemId = 2, timestamp = currentTime, quantity = 40, unit = "bag", type = TransactionType.IMPORT, unitPrice = 1.88),
                    InventoryTransaction(id = 3, itemId = 3, timestamp = currentTime, quantity = 60, unit = "carton", type = TransactionType.IMPORT, unitPrice = 1.25),
                    InventoryTransaction(id = 4, itemId = 4, timestamp = currentTime, quantity = 30, unit = "liter", type = TransactionType.IMPORT, unitPrice = 0.83),
                    InventoryTransaction(id = 5, itemId = 5, timestamp = currentTime, quantity = 20, unit = "liter", type = TransactionType.IMPORT, unitPrice = 1.04),
                    InventoryTransaction(id = 6, itemId = 6, timestamp = currentTime, quantity = 25, unit = "bottle", type = TransactionType.IMPORT, unitPrice = 0.63),
                    InventoryTransaction(id = 7, itemId = 7, timestamp = currentTime, quantity = 20, unit = "bottle", type = TransactionType.IMPORT, unitPrice = 0.71),
                    InventoryTransaction(id = 8, itemId = 8, timestamp = currentTime, quantity = 20, unit = "bottle", type = TransactionType.IMPORT, unitPrice = 0.75),
                    InventoryTransaction(id = 9, itemId = 9, timestamp = currentTime, quantity = 25, unit = "bottle", type = TransactionType.IMPORT, unitPrice = 0.83),
                    InventoryTransaction(id = 10, itemId = 10, timestamp = currentTime, quantity = 15, unit = "bottle", type = TransactionType.IMPORT, unitPrice = 0.92),
                    InventoryTransaction(id = 11, itemId = 11, timestamp = currentTime, quantity = 10, unit = "bottle", type = TransactionType.IMPORT, unitPrice = 0.67),
                    InventoryTransaction(id = 12, itemId = 12, timestamp = currentTime, quantity = 30, unit = "kg", type = TransactionType.IMPORT, unitPrice = 0.5),
                    InventoryTransaction(id = 13, itemId = 13, timestamp = currentTime, quantity = 20, unit = "can", type = TransactionType.IMPORT, unitPrice = 1.17),
                    InventoryTransaction(id = 14, itemId = 14, timestamp = currentTime, quantity = 40, unit = "bag", type = TransactionType.IMPORT, unitPrice = 1.08),
                    InventoryTransaction(id = 15, itemId = 15, timestamp = currentTime, quantity = 35, unit = "bag", type = TransactionType.IMPORT, unitPrice = 1.13),
                    InventoryTransaction(id = 16, itemId = 16, timestamp = currentTime, quantity = 30, unit = "bag", type = TransactionType.IMPORT, unitPrice = 1.04),
                    InventoryTransaction(id = 17, itemId = 17, timestamp = currentTime, quantity = 25, unit = "bag", type = TransactionType.IMPORT, unitPrice = 1.25),
                    InventoryTransaction(id = 18, itemId = 18, timestamp = currentTime, quantity = 20, unit = "bag", type = TransactionType.IMPORT, unitPrice = 1.29),
                    InventoryTransaction(id = 19, itemId = 19, timestamp = currentTime, quantity = 15, unit = "jar", type = TransactionType.IMPORT, unitPrice = 0.42),
                    InventoryTransaction(id = 20, itemId = 20, timestamp = currentTime, quantity = 15, unit = "jar", type = TransactionType.IMPORT, unitPrice = 0.46),
                    InventoryTransaction(id = 21, itemId = 21, timestamp = currentTime, quantity = 20, unit = "can", type = TransactionType.IMPORT, unitPrice = 0.92),
                    InventoryTransaction(id = 22, itemId = 22, timestamp = currentTime, quantity = 50, unit = "kg", type = TransactionType.IMPORT, unitPrice = 0.21),
                    InventoryTransaction(id = 23, itemId = 23, timestamp = currentTime, quantity = 200, unit = "pack", type = TransactionType.IMPORT, unitPrice = 0.13),
                    InventoryTransaction(id = 24, itemId = 24, timestamp = currentTime, quantity = 200, unit = "pack", type = TransactionType.IMPORT, unitPrice = 0.10),
                    InventoryTransaction(id = 25, itemId = 25, timestamp = currentTime, quantity = 150, unit = "pack", type = TransactionType.IMPORT, unitPrice = 0.08)
                )

                defaultTransactions.forEach { transaction ->
                    transactionRef.child(transaction.id.toString()).setValue(transaction)
                    lastTransactionIdRef.setValue(transaction.id)
                }
            }
        }
    }
}
