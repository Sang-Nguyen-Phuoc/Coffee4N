package com.example.coffee4n.repository

import com.example.coffee4n.model.Ingredient
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resumeWithException

class IngredientRepository(
    private val firebaseDatabase: FirebaseDatabase
) {
    private val ingredientRef = firebaseDatabase.getReference("ingredients")
    private val lastIngredientIdRef = firebaseDatabase.getReference("metadata/lastIngredientId")

    init {
        initializeDefaultIngredients()
    }

    fun getIngredientsFlow(): Flow<List<Ingredient>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ingredients = snapshot.children.mapNotNull { it.getValue(Ingredient::class.java) }
                trySend(ingredients).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ingredientRef.addValueEventListener(listener)
        awaitClose { ingredientRef.removeEventListener(listener) }
    }

    suspend fun getIngredient(id: Int) : Ingredient? {
        val snapshot = ingredientRef.child(id.toString()).get().await()
        return snapshot.getValue(Ingredient::class.java)
    }

    suspend fun addIngredient(ingredient: Ingredient) {
        val newId = getNextIngredientId()
        val newIngredient = ingredient.copy(id = newId)
        ingredientRef.child(newId.toString()).setValue(newIngredient).await()
    }

    suspend fun updateIngredient(ingredient: Ingredient) {
        val ingredientSnapshot = ingredientRef.child(ingredient.id.toString()).get().await()
        if (ingredientSnapshot.exists()) {
            ingredientRef.child(ingredient.id.toString()).setValue(ingredient).await()
        } else {
            throw IllegalArgumentException("Ingredient with id ${ingredient.id} not found")
        }
    }

    suspend fun deleteIngredient(id: Int) {
        val ingredientSnapshot = ingredientRef.child(id.toString()).get().await()
        if (ingredientSnapshot.exists()) {
            ingredientRef.child(id.toString()).removeValue().await()
        } else {
            throw IllegalArgumentException("Ingredient with id $id not found")
        }
    }

    suspend fun changeQuantityById(id: Int, delta: Int) {
        val ingredientSnapshot = ingredientRef.child(id.toString()).get().await()
        val ingredient = ingredientSnapshot.getValue(Ingredient::class.java)
            ?: throw IllegalArgumentException("Ingredient with id $id not found")

        val newQuantity = (ingredient.quantity + delta).coerceAtLeast(0)
        val updatedIngredient = ingredient.copy(quantity = newQuantity)
        ingredientRef.child(id.toString()).setValue(updatedIngredient).await()
    }

    suspend fun getIngredientStatus(id: Int): String {
        val ingredientSnapshot = ingredientRef.child(id.toString()).get().await()
        val ingredient = ingredientSnapshot.getValue(Ingredient::class.java)
            ?: throw IllegalArgumentException("Ingredient with id $id not found")

        return when {
            ingredient.quantity == 0 -> "Out of stock"
            ingredient.quantity <= ingredient.threshold -> "Low in stock"
            else -> "Enough"
        }
    }


    private suspend fun getNextIngredientId(): Int {
        return suspendCancellableCoroutine { continuation ->
            lastIngredientIdRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val lastId = currentData.getValue(Int::class.java) ?: 0
                    val nextId = lastId + 1
                    currentData.value = nextId
                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    snapshot: DataSnapshot?
                ) {
                    if (continuation.isActive) {
                        if (error != null) {
                            continuation.resumeWithException(error.toException())
                        } else {
                            val id = snapshot?.getValue(Int::class.java) ?: 1
                            continuation.resume(id) {}
                        }
                    }
                }
            })
        }
    }

    private fun initializeDefaultIngredients() {
        ingredientRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                val defaultIngredients = listOf(
                    Ingredient(id = 1, name = "Espresso Coffee", unit = "bag", quantity = 50, threshold = 10),
                    Ingredient(id = 2, name = "Ground Coffee", unit = "bag", quantity = 40, threshold = 10),
                    Ingredient(id = 3, name = "Milk", unit = "carton", quantity = 60, threshold = 15),
                    Ingredient(id = 4, name = "Steamed Milk", unit = "liter", quantity = 30, threshold = 10),
                    Ingredient(id = 5, name = "Milk Foam", unit = "liter", quantity = 20, threshold = 5),
                    Ingredient(id = 6, name = "Vanilla Syrup", unit = "bottle", quantity = 25, threshold = 5),
                    Ingredient(id = 7, name = "Caramel Sauce", unit = "bottle", quantity = 20, threshold = 5),
                    Ingredient(id = 8, name = "Hazelnut Syrup", unit = "bottle", quantity = 20, threshold = 5),
                    Ingredient(id = 9, name = "Chocolate Syrup", unit = "bottle", quantity = 25, threshold = 5),
                    Ingredient(id = 10, name = "White Chocolate Sauce", unit = "bottle", quantity = 15, threshold = 3),
                    Ingredient(id = 11, name = "Peppermint Syrup", unit = "bottle", quantity = 10, threshold = 2),
                    Ingredient(id = 12, name = "Sugar", unit = "kg", quantity = 30, threshold = 5),
                    Ingredient(id = 13, name = "Whipped Cream", unit = "can", quantity = 20, threshold = 5),
                    Ingredient(id = 14, name = "Black Tea Leaves", unit = "bag", quantity = 40, threshold = 10),
                    Ingredient(id = 15, name = "Green Tea Leaves", unit = "bag", quantity = 35, threshold = 8),
                    Ingredient(id = 16, name = "Oolong Tea Leaves", unit = "bag", quantity = 30, threshold = 8),
                    Ingredient(id = 17, name = "Earl Grey Tea Leaves", unit = "bag", quantity = 25, threshold = 5),
                    Ingredient(id = 18, name = "Chai Tea Mix", unit = "bag", quantity = 20, threshold = 5),
                    Ingredient(id = 19, name = "Cinnamon Powder", unit = "jar", quantity = 15, threshold = 3),
                    Ingredient(id = 20, name = "Ginger Powder", unit = "jar", quantity = 15, threshold = 3),
                    Ingredient(id = 21, name = "Matcha Powder", unit = "can", quantity = 20, threshold = 5),
                    Ingredient(id = 22, name = "Ice", unit = "kg", quantity = 50, threshold = 10),
                    Ingredient(id = 23, name = "Cups", unit = "pack", quantity = 200, threshold = 50),
                    Ingredient(id = 24, name = "Lids", unit = "pack", quantity = 200, threshold = 50),
                    Ingredient(id = 25, name = "Straws", unit = "pack", quantity = 150, threshold = 40)
                )
                defaultIngredients.forEach {
                    ingredientRef.child(it.id.toString()).setValue(it)
                }
                lastIngredientIdRef.setValue(defaultIngredients.maxOf { it.id })
            }
        }
    }
}
