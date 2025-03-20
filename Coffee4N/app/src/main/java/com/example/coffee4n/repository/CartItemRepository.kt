package com.example.coffee4n.repository

import com.google.firebase.database.FirebaseDatabase
import com.example.coffee4n.model.CartItem
import com.example.coffee4n.model.database.CartItemDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
        val snapshot = firebaseDatabase.getReference("cartitems").child(userId.toString()).get().await()
        val cartItems = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }
        cartItems.forEach { cartItemDao.insertCartItem(it) }
    }

    suspend fun addCartItem(cartItem: CartItem) {
        cartItemDao.insertCartItem(cartItem)
        firebaseDatabase.getReference("cartitems").child(cartItem.userId.toString()).child(cartItem.id.toString()).setValue(cartItem).await()
    }

    suspend fun deleteCartItem(id: Int, userId: Int) {
        cartItemDao.deleteCartItem(id)
        firebaseDatabase.getReference("cartitems").child(userId.toString()).child(id.toString()).removeValue().await()
    }

    suspend fun clearCart(userId: Int) {
        cartItemDao.deleteCartItemsByUser(userId)
        firebaseDatabase.getReference("cartitems").child(userId.toString()).removeValue().await()
    }

    fun getCartItemsFlow(userId: Int): Flow<List<CartItem>> = flow {
        emit(cartItemDao.getCartItemsByUser(userId))
        syncCartItemsFromRemote(userId)
        emit(cartItemDao.getCartItemsByUser(userId))
    }
}