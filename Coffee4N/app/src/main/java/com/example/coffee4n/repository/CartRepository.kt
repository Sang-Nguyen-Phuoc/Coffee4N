package com.example.coffee4n.repository

import com.example.coffee4n.model.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

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

    fun getAllProductsFlow(): Flow<List<Product>> = productsFlow

    suspend fun getAllProducts(): List<Product> = productsFlow.value
}