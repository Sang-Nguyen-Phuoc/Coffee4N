package com.example.coffee4n.repository

import com.example.coffee4n.model.Product
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

class ProductRepository(
    private val firebaseDatabase: FirebaseDatabase
) {
    private val productRef = firebaseDatabase.getReference(OwnerSession.getReferencePath(model = Models.Product))
    private val lastProductIdRef = firebaseDatabase.getReference(OwnerSession.getMetadataPath(lastModelId = LastIds.Product))

    init {
        initializeDefaultProducts()
    }

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
        awaitClose { productRef.removeEventListener(listener) }
    }

    fun getProductFlow(id: Int): Flow<Product?> = callbackFlow {
        val reference = productRef
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = snapshot.children.mapNotNull { it.getValue(Product::class.java) }
                val product = products.find { it.id == id }
                println("DEBUG: Product for productId $id: $product")
                trySend(product).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                println("DEBUG: Error fetching product for productId $id: ${error.message}")
                close(error.toException())
            }
        }
        reference.addValueEventListener(listener)
        awaitClose { reference.removeEventListener(listener) }
    }

    suspend fun addProduct(product: Product) {
        val newId = getNextProductId()
        val newProduct = product.copy(id = newId)
        productRef.child(newId.toString()).setValue(newProduct).await()
    }

    suspend fun updateProduct(product: Product) {
        val productSnapshot = productRef.child(product.id.toString()).get().await()
        if (productSnapshot.exists()) {
            productRef.child(product.id.toString()).setValue(product).await()
        } else {
            throw IllegalArgumentException("Product with id ${product.id} not found")
        }
    }


    suspend fun deleteProduct(id: Int) {
        val productSnapshot = productRef.child(id.toString()).get().await()
        if (productSnapshot.exists()) {
            productRef.child(id.toString()).removeValue().await()
        } else {
            throw IllegalArgumentException("Product with id $id not found")
        }
    }

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

    private fun initializeDefaultProducts() {
        productRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                val defaultProducts = listOf(
                    Product(isBestSeller = false, categoryId = 1, costPrice = 1.2, description = "Strong and pure espresso.", id = 1, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1743438709/Home/CoffeeProductImage/temp_image_1743438706805_w7fguo.jpg", name = "Espresso", price = 2.8, stockQuantity = 50),
                    Product(isBestSeller = false, categoryId = 1, costPrice = 1.5, description = "Double shot of espresso, extra caffeine.", id = 2, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745089522/Home/CoffeeProductImage/temp_image_1745089520303_iwpkok.jpg", name = "Doppio Espresso", price = 3.0, stockQuantity = 40),
                    Product(isBestSeller = true, categoryId = 1, costPrice = 1.3, description = "Concentrated espresso with less water.", id = 3, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1743438958/Home/CoffeeProductImage/temp_image_1743438954714_cyijpu.jpg", name = "Ristretto", price = 2.75, stockQuantity = 35),
                    Product(isBestSeller = false, categoryId = 1, costPrice = 1.25, description = "Longer brewed espresso with more water.", id = 4, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745089268/Home/CoffeeProductImage/temp_image_1745089266455_fre15p.jpg", name = "Lungo", price = 2.6, stockQuantity = 45),
                    Product(isBestSeller = true, categoryId = 1, costPrice = 1.5, description = "Espresso diluted with hot water.", id = 5, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745089617/Home/CoffeeProductImage/temp_image_1745089615179_iwul9h.jpg", name = "Americano", price = 3.0, stockQuantity = 50),
                    Product(isBestSeller = true, categoryId = 2, costPrice = 1.8, description = "Espresso with steamed milk and foam.", id = 6, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745090848/Home/CoffeeProductImage/temp_image_1745090848222_js4xr6.jpg", name = "Caffè Latte", price = 3.5, stockQuantity = 60),
                    Product(isBestSeller = false, categoryId = 2, costPrice = 2.0, description = "Latte with vanilla flavor.", id = 7, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745090840/Home/CoffeeProductImage/temp_image_1745090839480_gfu65w.jpg", name = "Vanilla Latte", price = 3.75, stockQuantity = 55),
                    Product(isBestSeller = true, categoryId = 2, costPrice = 2.1, description = "Latte with caramel sauce.", id = 8, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745090859/Home/CoffeeProductImage/temp_image_1745090858901_w9hbpl.jpg", name = "Caramel Latte", price = 3.8, stockQuantity = 50),
                    Product(isBestSeller = false, categoryId = 2, costPrice = 2.2, description = "Latte with hazelnut flavor.", id = 9, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745090542/Home/CoffeeProductImage/temp_image_1745090540779_n4c8ri.jpg", name = "Hazelnut Latte", price = 3.9, stockQuantity = 45),
                    Product(isBestSeller = false, categoryId = 2, costPrice = 2.3, description = "Latte mixed with chocolate.", id = 10, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745090707/Home/CoffeeProductImage/temp_image_1745090706531_llqdpr.jpg", name = "Mocha Latte", price = 4.0, stockQuantity = 40),
                    Product(isBestSeller = false, categoryId = 2, costPrice = 2.5, description = "Latte with matcha green tea powder.", id = 11, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745090694/Home/CoffeeProductImage/temp_image_1745090693210_zzv3wr.jpg", name = "Matcha Latte", price = 4.2, stockQuantity = 35),
                    Product(isBestSeller = true, categoryId = 3, costPrice = 2.0, description = "Cappuccino with less milk.", id = 12, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745091037/Home/CoffeeProductImage/temp_image_1745091034574_zfpwvi.jpg", name = "Dry Cappuccino", price = 3.8, stockQuantity = 55),
                    Product(isBestSeller = true, categoryId = 3, costPrice = 2.1, description = "Cappuccino with more milk.", id = 13, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745091045/Home/CoffeeProductImage/temp_image_1745091044891_fdry3s.jpg", name = "Wet Cappuccino", price = 3.85, stockQuantity = 50),
                    Product(isBestSeller = true, categoryId = 3, costPrice = 2.2, description = "Cappuccino with vanilla flavor.", id = 14, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745091194/Home/CoffeeProductImage/temp_image_1745091194267_neakhs.jpg", name = "Vanilla Cappuccino", price = 4.0, stockQuantity = 45),
                    Product(isBestSeller = false, categoryId = 3, costPrice = 2.3, description = "Cappuccino with caramel syrup.", id = 15, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745091185/Home/CoffeeProductImage/temp_image_1745091185231_nfqadd.jpg", name = "Caramel Cappuccino", price = 4.1, stockQuantity = 40),
                    Product(isBestSeller = false, categoryId = 3, costPrice = 2.4, description = "Cappuccino with chocolate drizzle.", id = 16, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745091176/Home/CoffeeProductImage/temp_image_1745091173935_xkgehp.jpg", name = "Chocolate Cappuccino", price = 4.15, stockQuantity = 35),
                    Product(isBestSeller = false, categoryId = 4, costPrice = 2.5, description = "Espresso with steamed milk, chocolate syrup, and whipped cream.", id = 17, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745091321/Home/CoffeeProductImage/temp_image_1745091319548_txrfik.jpg", name = "Caffè Mocha", price = 4.5, stockQuantity = 40),
                    Product(isBestSeller = true, categoryId = 4, costPrice = 2.6, description = "Mocha made with white chocolate.", id = 18, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745091328/Home/CoffeeProductImage/temp_image_1745091328343_ciqrz0.jpg", name = "White Mocha", price = 4.75, stockQuantity = 35),
                    Product(isBestSeller = false, categoryId = 4, costPrice = 2.7, description = "Mocha with a hint of peppermint.", id = 19, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745091448/Home/CoffeeProductImage/temp_image_1745091448565_zprwig.jpg", name = "Peppermint Mocha", price = 4.8, stockQuantity = 30),
                    Product(isBestSeller = true, categoryId = 4, costPrice = 2.8, description = "Mocha with caramel drizzle.", id = 20, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745091422/Home/CoffeeProductImage/temp_image_1745091421214_nqurlz.jpg", name = "Caramel Mocha", price = 4.85, stockQuantity = 25),
                    Product(isBestSeller = false, categoryId = 4, costPrice = 2.9, description = "Mocha with hazelnut flavor.", id = 21, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745091438/Home/CoffeeProductImage/temp_image_1745091437893_trikp8.jpg", name = "Hazelnut Mocha", price = 4.9, stockQuantity = 20),
                    Product(isBestSeller = false, categoryId = 5, costPrice = 1.0, description = "Classic black tea with bold flavor.", id = 22, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745089814/Home/CoffeeProductImage/temp_image_1745089812315_amhxwf.jpg", name = "Black Tea", price = 3.2, stockQuantity = 70),
                    Product(isBestSeller = true, categoryId = 5, costPrice = 1.2, description = "Refreshing and healthy green tea.", id = 23, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1743469133/Home/CoffeeProductImage/temp_image_1743469125813_paxglf.jpg", name = "Green Tea", price = 2.75, stockQuantity = 65),
                    Product(isBestSeller = true, categoryId = 5, costPrice = 1.3, description = "Rich and floral oolong tea.", id = 24, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745089763/Home/CoffeeProductImage/temp_image_1745089760914_spopqw.jpg", name = "Oolong Tea", price = 3.0, stockQuantity = 60),
                    Product(isBestSeller = true, categoryId = 5, costPrice = 1.4, description = "Black tea with bergamot citrus flavor.", id = 25, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745089854/Home/CoffeeProductImage/temp_image_1745089852113_qsths1.jpg", name = "Earl Grey Tea", price = 3.2, stockQuantity = 55),
                    Product(isBestSeller = false, categoryId = 5, costPrice = 1.5, description = "Spiced black tea with cinnamon and ginger.", id = 26, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745089967/Home/CoffeeProductImage/temp_image_1745089965076_s8a4p8.jpg", name = "Chai Tea", price = 3.4, stockQuantity = 50),
                    Product(isBestSeller = false, categoryId = 5, costPrice = 1.7, description = "Traditional Japanese matcha green tea.", id = 27, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745090072/Home/CoffeeProductImage/temp_image_1745090069653_ofvheq.jpg", name = "Matcha Tea", price = 3.6, stockQuantity = 45),
                    Product(isBestSeller = false, categoryId = 1, costPrice = 2.25, description = "Mixture of milk and coffee", id = 28, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1743469242/Home/CoffeeProductImage/temp_image_1743469239258_j78s8r.jpg", name = "Milk Tea", price = 3.1, stockQuantity = 50),
                    Product(isBestSeller = true, categoryId = 5, costPrice = 3.99, description = "A refreshing and aromatic tea infused with the sweet and tangy flavor of ripe peaches. This delightful beverage is perfect for cooling down on hot days, offering a balance of fruity sweetness and smooth tea notes.", id = 29, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1743438621/Home/CoffeeProductImage/temp_image_1743438617763_chr4ln.jpg", name = "Peach Tea", price = 4.5, stockQuantity = 50),
                    Product(isBestSeller = false, categoryId = 5, costPrice = 2.2, description = "A refreshing blend of brewed tea and zesty lemon, offering a perfect balance of tangy citrus flavor and subtle tea bitterness. Served hot or iced, it’s both soothing and invigorating.", id = 30, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745089153/Home/CoffeeProductImage/temp_image_1745089151273_v0gq2m.jpg", name = "Lemon Tea", price = 3.4, stockQuantity = 45),
                    Product(isBestSeller = true, categoryId = 4, costPrice = 3.0, description = "Combines the rich, bold flavor of mocha with the sweet, fruity notes of ripe strawberries. Blended with ice and milk, it’s a creamy, indulgent treat that energizes and satisfies.", id = 31, imageUrl = "https://res.cloudinary.com/dizp8jtoi/image/upload/v1745091961/Home/CoffeeProductImage/temp_image_1745091959635_i1xiva.jpg", name = "Strawberry Mocha Smothie", price = 4.3, stockQuantity = 19)
                )
                defaultProducts.forEach {
                    productRef.child(it.id.toString()).setValue(it)
                }
                lastProductIdRef.setValue(defaultProducts.maxOf { it.id })
            }
        }
    }
}