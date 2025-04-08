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

class CartRepository(private val firebaseDatabase: FirebaseDatabase) {
    private val productsFlow = MutableStateFlow<List<Product>>(emptyList())

    init {
        firebaseDatabase.getReference("products").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = snapshot.children.mapNotNull { it.getValue(Product::class.java) }
                productsFlow.value = products
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu cần
            }
        })
    }

    private val cartRef = firebaseDatabase.getReference("carts")
    private val lastCartItemIdRef = firebaseDatabase.getReference("metadata/lastCartItemId")

    fun getCartItemsFlow(userId: Int): Flow<List<CartItem>> = callbackFlow {
        val userCartRef = cartRef.child(userId.toString())

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cartItems = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }
                trySend(cartItems).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        userCartRef.addValueEventListener(listener)
        awaitClose { userCartRef.removeEventListener(listener) }
    }

    suspend fun addToCart(userId: Int, cartItem: CartItem) {
        // Check if the product already exists in the cart
        val userCartRef = cartRef.child(userId.toString())
        val snapshot = userCartRef.get().await()

        val existingCartItem = snapshot.children.mapNotNull {
            it.getValue(CartItem::class.java)
        }.find { it.productId == cartItem.productId }

        if (existingCartItem != null) {
            // Update quantity
            val updatedCartItem = existingCartItem.copy(quantity = existingCartItem.quantity + cartItem.quantity)
            userCartRef.child(existingCartItem.id.toString()).setValue(updatedCartItem).await()
        } else {
            // Add new item
            val newItemId = getNextCartItemId()
            val newCartItem = cartItem.copy(id = newItemId)
            userCartRef.child(newItemId.toString()).setValue(newCartItem).await()
        }
    }

    suspend fun updateCartItemQuantity(userId: Int, cartItemId: Int, quantity: Int) {
        val userCartRef = cartRef.child(userId.toString())
        val snapshot = userCartRef.child(cartItemId.toString()).get().await()
        val cartItem = snapshot.getValue(CartItem::class.java)

        if (cartItem != null) {
            val updatedCartItem = cartItem.copy(quantity = quantity)
            userCartRef.child(cartItemId.toString()).setValue(updatedCartItem).await()
        }
    }

    suspend fun removeCartItem(userId: Int, cartItemId: Int) {
        cartRef.child(userId.toString()).child(cartItemId.toString()).removeValue().await()
    }

    suspend fun clearCart(userId: Int) {
        cartRef.child(userId.toString()).removeValue().await()
    }

    private suspend fun getNextCartItemId(): Int {
        val snapshot = lastCartItemIdRef.get().await()
        val lastId = snapshot.getValue(Int::class.java) ?: 0
        val nextId = lastId + 1
        lastCartItemIdRef.setValue(nextId).await()
        return nextId
    }


    fun getAllProductsFlow(): Flow<List<Product>> = productsFlow

    suspend fun getAllProducts(): List<Product> = productsFlow.value
}