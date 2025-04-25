package com.example.coffee4n.repository

import com.example.coffee4n.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.coffee4n.model.CartItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.example.coffee4n.session.LastIds
import com.example.coffee4n.session.Models
import com.example.coffee4n.session.OwnerSession

class CartRepository(private val firebaseDatabase: FirebaseDatabase) {
    private val productsFlow = MutableStateFlow<List<Product>>(emptyList())
    init {
        firebaseDatabase.getReference(OwnerSession.getReferencePath(model = Models.Product)).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = snapshot.children.mapNotNull { it.getValue(Product::class.java) }
                productsFlow.value = products
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })
    }

    private val cartRef = firebaseDatabase.getReference(OwnerSession.getReferencePath(model = Models.CartItem))
    private val lastCartItemIdRef = firebaseDatabase.getReference(OwnerSession.getMetadataPath(lastModelId = LastIds.CartItem))

    fun getCartItemsFlow(userId: Int): Flow<List<CartItem>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cartItems = snapshot.children.mapNotNull {
                    it.getValue(CartItem::class.java)?.takeIf { item -> item.userId == userId }
                }
                trySend(cartItems).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        cartRef.addValueEventListener(listener)
        awaitClose { cartRef.removeEventListener(listener) }
    }

    suspend fun addToCart(userId: Int, cartItem: CartItem) {
        // Check if the product already exists in the cart
        val snapshot = cartRef.get().await()

        val existingCartItem = snapshot.children.mapNotNull {
            it.getValue(CartItem::class.java)
        }.find { it.productId == cartItem.productId && it.userId == userId }

        if (existingCartItem != null) {
            // Update quantity
            val updatedCartItem = existingCartItem.copy(quantity = existingCartItem.quantity + cartItem.quantity)
            cartRef.child(existingCartItem.id.toString()).setValue(updatedCartItem).await()
        } else {
            // Add new item with only the essential fields
            val newItemId = getNextCartItemId()
            val simpleCartItem = CartItem(
                id = newItemId,
                userId = userId,
                productId = cartItem.productId,
                quantity = cartItem.quantity,
            )
            cartRef.child(newItemId.toString()).setValue(simpleCartItem).await()
        }
    }

    suspend fun updateCartItemQuantity(userId: Int, cartItemId: Int, quantity: Int) {
        val snapshot = cartRef.child(cartItemId.toString()).get().await()
        val cartItem = snapshot.getValue(CartItem::class.java)

        if (cartItem != null && cartItem.userId == userId) {
            val updatedCartItem = cartItem.copy(quantity = quantity)
            cartRef.child(cartItemId.toString()).setValue(updatedCartItem).await()
        }
    }

    suspend fun removeCartItem(userId: Int, cartItemId: Int) {
        val snapshot = cartRef.child(cartItemId.toString()).get().await()
        val cartItem = snapshot.getValue(CartItem::class.java)

        if (cartItem != null && cartItem.userId == userId) {
            cartRef.child(cartItemId.toString()).removeValue().await()
        }
    }

    suspend fun clearCart(userId: Int) {
        val snapshot = cartRef.get().await()

        snapshot.children.forEach { childSnapshot ->
            val cartItem = childSnapshot.getValue(CartItem::class.java)
            if (cartItem != null && cartItem.userId == userId) {
                cartRef.child(childSnapshot.key!!).removeValue().await()
            }
        }
    }

    private suspend fun getNextCartItemId(): Int {
        val snapshot = lastCartItemIdRef.get().await()
        val lastId = snapshot.getValue(Int::class.java) ?: 0
        val nextId = lastId + 1
        lastCartItemIdRef.setValue(nextId).await()
        return nextId
    }

    suspend fun isProductInCart(userId: Int, productId: Int): Boolean {
        return try {
            val snapshot = cartRef.get().await()
            snapshot.children.any {
                val cartItem = it.getValue(CartItem::class.java)
                cartItem?.productId == productId && cartItem.userId == userId
            }
        } catch (e: Exception) {
            false
        }
    }

    fun getAllProductsFlow(): Flow<List<Product>> = productsFlow

    suspend fun getAllProducts(): List<Product> = productsFlow.value
}