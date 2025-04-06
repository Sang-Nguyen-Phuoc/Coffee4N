package com.example.coffee4n.repository

import com.example.coffee4n.model.CartItem
import com.example.coffee4n.model.database.CartItemDao
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CartItemRepository(
    private val cartItemDao: CartItemDao,
    private val firebaseDatabase: FirebaseDatabase
) {
    suspend fun getAllCartItemsFromLocal(): List<CartItem> {
        return cartItemDao.getAllCartItems()
    }

    suspend fun getCartItemsByUser(userId: Int): List<CartItem> {
        return cartItemDao.getCartItemsByUser(userId)
    }

    private suspend fun syncCartItemsFromRemote(userId: Int) {
        val snapshot = firebaseDatabase.getReference("cartitems").get().await()
        val cartItems = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }
            .filter { it.userId == userId }
        cartItems.forEach { cartItemDao.insertCartItem(it) }
    }

    suspend fun addCartItem(cartItem: CartItem) {
        cartItemDao.insertCartItem(cartItem)
        val snapshot = firebaseDatabase.getReference("cartitems").get().await()
        val cartItems = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }.toMutableList()
        val existingIndex = cartItems.indexOfFirst { it.id == cartItem.id && it.userId == cartItem.userId }
        if (existingIndex != -1) {
            cartItems[existingIndex] = cartItem
        } else {
            cartItems.add(cartItem)
        }
        firebaseDatabase.getReference("cartitems").setValue(cartItems).await()
    }

    suspend fun deleteCartItem(id: Int, userId: Int) {
        cartItemDao.deleteCartItem(id)
        val snapshot = firebaseDatabase.getReference("cartitems").get().await()
        val cartItems = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }.toMutableList()
        val updatedCartItems = cartItems.filter { it.id != id || it.userId != userId }
        firebaseDatabase.getReference("cartitems").setValue(updatedCartItems).await()
    }

    suspend fun clearCart(userId: Int) {
        cartItemDao.deleteCartItemsByUser(userId)
        val snapshot = firebaseDatabase.getReference("cartitems").get().await()
        val cartItems = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }.toMutableList()
        val updatedCartItems = cartItems.filter { it.userId != userId }
        firebaseDatabase.getReference("cartitems").setValue(updatedCartItems).await()
    }

    fun getCartItemsFlow(userId: Int): Flow<List<CartItem>> {
        return callbackFlow {
            val reference = firebaseDatabase.getReference("cartitems")
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
}