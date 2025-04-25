package com.example.coffee4n.repository

import com.example.coffee4n.model.CartItem
import com.example.coffee4n.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.example.coffee4n.session.Models
import com.example.coffee4n.session.OwnerSession

class CartItemRepository(
    private val firebaseDatabase: FirebaseDatabase
) {
    private val cartItemRef = firebaseDatabase.getReference(OwnerSession.getReferencePath(model = Models.CartItem))

    suspend fun addCartItem(cartItem: CartItem) {
        val snapshot = cartItemRef.get().await()
        val cartItems = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }.toMutableList()
        val existingIndex = cartItems.indexOfFirst { it.id == cartItem.id && it.userId == cartItem.userId }
        if (existingIndex != -1) {
            cartItems[existingIndex] = cartItem
        } else {
            cartItems.add(cartItem)
        }
        cartItemRef.setValue(cartItems).await()
    }

    suspend fun deleteCartItem(id: Int, userId: Int) {
        val snapshot = cartItemRef.get().await()
        val cartItems = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }.toMutableList()
        val updatedCartItems = cartItems.filter { it.id != id || it.userId != userId }
        cartItemRef.setValue(updatedCartItems).await()
    }

    suspend fun clearCart(userId: Int) {
        val snapshot = cartItemRef.get().await()
        val cartItems = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }.toMutableList()
        val updatedCartItems = cartItems.filter { it.userId != userId }
        cartItemRef.setValue(updatedCartItems).await()
    }

    fun getCartItemsFlow(userId: Int): Flow<List<CartItem>> {
        return callbackFlow {
            val reference = cartItemRef
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val cartItems = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }
                        .filter { it.userId == userId }
                    println("DEBUG: CartItems for userId $userId: $cartItems")
                    trySend(cartItems).isSuccess
                }

                override fun onCancelled(error: DatabaseError) {
                    println("DEBUG: Error fetching cart items for userId $userId: ${error.message}")
                    close(error.toException())
                }
            }
            reference.addValueEventListener(listener)
            awaitClose { reference.removeEventListener(listener) }
        }
    }

    suspend fun deleteAllCartItems(userId: Int) {
        val snapshot = cartItemRef.get().await()
        val cartItems = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }.toMutableList()
        val updatedCartItems = cartItems.filter { it.userId != userId }
        cartItemRef.setValue(updatedCartItems).await()
    }

    suspend fun updateCartItemNote(id: Int, userId: Int, newNote: String?) {
        val snapshot = cartItemRef.get().await()
        val cartItems = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }.toMutableList()
        val index = cartItems.indexOfFirst { it.id == id && it.userId == userId }
        if (index != -1) {
            val updatedItem = cartItems[index].copy(note = newNote)
            cartItems[index] = updatedItem
            cartItemRef.setValue(cartItems).await()
        }
    }
}