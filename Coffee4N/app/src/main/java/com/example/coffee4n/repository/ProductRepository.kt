package com.example.coffee4n.repository

import com.example.coffee4n.model.Product
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resumeWithException

class ProductRepository(
    private val firebaseDatabase: FirebaseDatabase
) {
    private val productRef = firebaseDatabase.getReference("products")
    private val lastProductIdRef = firebaseDatabase.getReference("metadata/lastProductId")

    init {
        initializeDefaultProducts()
    }

    // Fetch products as a Flow
    fun getProductsFlow(): Flow<List<Product>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = snapshot.children.mapNotNull { it.getValue(Product::class.java) }
                trySend(products).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        productRef.addValueEventListener(listener)

        awaitClose {
            productRef.removeEventListener(listener)
        }
    }

    // Fetch a single product by ID as a Flow
    fun getProductFlow(id: Int): Flow<Product?> = callbackFlow {
        val reference = productRef.child(id.toString())
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val product = snapshot.getValue(Product::class.java)
                trySend(product).isSuccess
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

    // Add a new product with auto-increment ID
    suspend fun addProduct(product: Product) {
        val newId = getNextProductId()
        val newProduct = product.copy(id = newId)
        productRef.child(newId.toString()).setValue(newProduct).await()
    }

    // Update an existing product
    suspend fun updateProduct(product: Product) {
        productRef.child(product.id.toString()).setValue(product).await()
    }

    // Delete a product from Firebase
    suspend fun deleteProduct(id: Int) {
        productRef.child(id.toString()).removeValue().await()
    }

    // Get next product ID using metadata/lastProductId
    private suspend fun getNextProductId(): Int {
        return suspendCancellableCoroutine { continuation ->
            lastProductIdRef.runTransaction(object : Transaction.Handler {
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
                    if (continuation.isActive) {  // Kiểm tra coroutine có còn hoạt động không
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



    // Initialize default products if none exist
    private fun initializeDefaultProducts() {
        productRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                val defaultProducts = listOf(
                    // Espresso
                    Product(id = 1, name = "Espresso", description = "Strong and pure espresso.", price = 2.50, costPrice = 1.20, stockQuantity = 50, categoryId = 1),
                    Product(id = 2, name = "Doppio Espresso", description = "Double shot of espresso, extra caffeine.", price = 3.00, costPrice = 1.50, stockQuantity = 40, categoryId = 1),
                    Product(id = 3, name = "Ristretto", description = "Concentrated espresso with less water.", price = 2.75, costPrice = 1.30, stockQuantity = 35, categoryId = 1, isBestSeller = true),
                    Product(id = 4, name = "Lungo", description = "Longer brewed espresso with more water.", price = 2.60, costPrice = 1.25, stockQuantity = 45, categoryId = 1),
                    Product(id = 5, name = "Americano", description = "Espresso diluted with hot water.", price = 3.00, costPrice = 1.50, stockQuantity = 50, categoryId = 1, isBestSeller = true),

                    // Latte
                    Product(id = 6, name = "Caffè Latte", description = "Espresso with steamed milk and foam.", price = 3.50, costPrice = 1.80, stockQuantity = 60, categoryId = 2, isBestSeller = true),
                    Product(id = 7, name = "Vanilla Latte", description = "Latte with vanilla flavor.", price = 3.75, costPrice = 2.00, stockQuantity = 55, categoryId = 2),
                    Product(id = 8, name = "Caramel Latte", description = "Latte with caramel sauce.", price = 3.80, costPrice = 2.10, stockQuantity = 50, categoryId = 2, isBestSeller = true),
                    Product(id = 9, name = "Hazelnut Latte", description = "Latte with hazelnut flavor.", price = 3.90, costPrice = 2.20, stockQuantity = 45, categoryId = 2),
                    Product(id = 10, name = "Mocha Latte", description = "Latte mixed with chocolate.", price = 4.00, costPrice = 2.30, stockQuantity = 40, categoryId = 2),
                    Product(id = 11, name = "Matcha Latte", description = "Latte with matcha green tea powder.", price = 4.20, costPrice = 2.50, stockQuantity = 35, categoryId = 2),

                    // Cappuccino
                    Product(id = 12, name = "Cappuccino", description = "Espresso with steamed milk and thick foam.", price = 3.75, costPrice = 1.90, stockQuantity = 60, categoryId = 3),
                    Product(id = 13, name = "Dry Cappuccino", description = "Cappuccino with less milk.", price = 3.80, costPrice = 2.00, stockQuantity = 55, categoryId = 3, isBestSeller = true),
                    Product(id = 14, name = "Wet Cappuccino", description = "Cappuccino with more milk.", price = 3.85, costPrice = 2.10, stockQuantity = 50, categoryId = 3, isBestSeller = true),
                    Product(id = 15, name = "Vanilla Cappuccino", description = "Cappuccino with vanilla flavor.", price = 4.00, costPrice = 2.20, stockQuantity = 45, categoryId = 3, isBestSeller = true),
                    Product(id = 16, name = "Caramel Cappuccino", description = "Cappuccino with caramel syrup.", price = 4.10, costPrice = 2.30, stockQuantity = 40, categoryId = 3),
                    Product(id = 17, name = "Chocolate Cappuccino", description = "Cappuccino with chocolate drizzle.", price = 4.15, costPrice = 2.40, stockQuantity = 35, categoryId = 3),

                    // Mocha
                    Product(id = 18, name = "Caffè Mocha", description = "Espresso with steamed milk, chocolate syrup, and whipped cream.", price = 4.50, costPrice = 2.50, stockQuantity = 40, categoryId = 4),
                    Product(id = 19, name = "White Mocha", description = "Mocha made with white chocolate.", price = 4.75, costPrice = 2.60, stockQuantity = 35, categoryId = 4, isBestSeller = true),
                    Product(id = 20, name = "Peppermint Mocha", description = "Mocha with a hint of peppermint.", price = 4.80, costPrice = 2.70, stockQuantity = 30, categoryId = 4),
                    Product(id = 21, name = "Caramel Mocha", description = "Mocha with caramel drizzle.", price = 4.85, costPrice = 2.80, stockQuantity = 25, categoryId = 4, isBestSeller = true),
                    Product(id = 22, name = "Hazelnut Mocha", description = "Mocha with hazelnut flavor.", price = 4.90, costPrice = 2.90, stockQuantity = 20, categoryId = 4),

                    // Tea
                    Product(id = 23, name = "Black Tea", description = "Classic black tea with bold flavor.", price = 2.50, costPrice = 1.00, stockQuantity = 70, categoryId = 5),
                    Product(id = 24, name = "Green Tea", description = "Refreshing and healthy green tea.", price = 2.75, costPrice = 1.20, stockQuantity = 65, categoryId = 5, isBestSeller = true),
                    Product(id = 25, name = "Oolong Tea", description = "Rich and floral oolong tea.", price = 3.00, costPrice = 1.30, stockQuantity = 60, categoryId = 5, isBestSeller = true),
                    Product(id = 26, name = "Earl Grey Tea", description = "Black tea with bergamot citrus flavor.", price = 3.20, costPrice = 1.40, stockQuantity = 55, categoryId = 5, isBestSeller = true),
                    Product(id = 27, name = "Chai Tea", description = "Spiced black tea with cinnamon and ginger.", price = 3.40, costPrice = 1.50, stockQuantity = 50, categoryId = 5),
                    Product(id = 28, name = "Matcha Tea", description = "Traditional Japanese matcha green tea.", price = 3.60, costPrice = 1.70, stockQuantity = 45, categoryId = 5)
                )
                defaultProducts.forEach { product ->
                    productRef.child(product.id.toString()).setValue(product)
                }
                lastProductIdRef.setValue(28)
            }
        }
    }
}
