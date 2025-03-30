package com.example.coffee4n.repository

import com.example.coffee4n.model.Product
import com.example.coffee4n.model.database.ProductDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProductRepository(private val productDao: ProductDao) {
    fun getAllProducts(): Flow<List<Product>> = flow {
        emit(productDao.getAllProducts())
    }

    suspend fun insertSampleProducts() {
        val productCount = productDao.getProductCount()
        if (productCount == 0) {
            val sampleProducts = listOf(
                // Espresso
                Product(name = "Espresso", description = "Strong and pure espresso.", price = 2.50, category = "Espresso"),
                Product(name = "Doppio Espresso", description = "Double shot of espresso, extra caffeine.", price = 3.00, category = "Espresso"),
                Product(name = "Ristretto", description = "Concentrated espresso with less water.", price = 2.75, category = "Espresso", isBestSeller = true),
                Product(name = "Lungo", description = "Longer brewed espresso with more water.", price = 2.60, category = "Espresso"),
                Product(name = "Americano", description = "Espresso diluted with hot water.", price = 3.00, category = "Espresso", isBestSeller = true),

                // Latte
                Product(name = "Caffè Latte", description = "Espresso with steamed milk and foam.", price = 3.50, category = "Latte", isBestSeller = true),
                Product(name = "Vanilla Latte", description = "Latte with vanilla flavor.", price = 3.75, category = "Latte"),
                Product(name = "Caramel Latte", description = "Latte with caramel sauce.", price = 3.80, category = "Latte", isBestSeller = true),
                Product(name = "Hazelnut Latte", description = "Latte with hazelnut flavor.", price = 3.90, category = "Latte"),
                Product(name = "Mocha Latte", description = "Latte mixed with chocolate.", price = 4.00, category = "Latte"),
                Product(name = "Matcha Latte", description = "Latte with matcha green tea powder.", price = 4.20, category = "Latte"),

                // Cappuccino
                Product(name = "Cappuccino", description = "Espresso with steamed milk and thick foam.", price = 3.75, category = "Cappuccino"),
                Product(name = "Dry Cappuccino", description = "Cappuccino with less milk.", price = 3.80, category = "Cappuccino", isBestSeller = true),
                Product(name = "Wet Cappuccino", description = "Cappuccino with more milk.", price = 3.85, category = "Cappuccino", isBestSeller = true),
                Product(name = "Vanilla Cappuccino", description = "Cappuccino with vanilla flavor.", price = 4.00, category = "Cappuccino", isBestSeller = true),
                Product(name = "Caramel Cappuccino", description = "Cappuccino with caramel syrup.", price = 4.10, category = "Cappuccino"),
                Product(name = "Chocolate Cappuccino", description = "Cappuccino with chocolate drizzle.", price = 4.15, category = "Cappuccino"),

                // Mocha
                Product(name = "Caffè Mocha", description = "Espresso with steamed milk, chocolate syrup, and whipped cream.", price = 4.50, category = "Mocha"),
                Product(name = "White Mocha", description = "Mocha made with white chocolate.", price = 4.75, category = "Mocha", isBestSeller = true),
                Product(name = "Peppermint Mocha", description = "Mocha with a hint of peppermint.", price = 4.80, category = "Mocha"),
                Product(name = "Caramel Mocha", description = "Mocha with caramel drizzle.", price = 4.85, category = "Mocha", isBestSeller = true),
                Product(name = "Hazelnut Mocha", description = "Mocha with hazelnut flavor.", price = 4.90, category = "Mocha"),

                // Tea
                Product(name = "Black Tea", description = "Classic black tea with bold flavor.", price = 2.50, category = "Tea"),
                Product(name = "Green Tea", description = "Refreshing and healthy green tea.", price = 2.75, category = "Tea", isBestSeller = true),
                Product(name = "Oolong Tea", description = "Rich and floral oolong tea.", price = 3.00, category = "Tea", isBestSeller = true),
                Product(name = "Earl Grey Tea", description = "Black tea with bergamot citrus flavor.", price = 3.20, category = "Tea", isBestSeller = true),
                Product(name = "Chai Tea", description = "Spiced black tea with cinnamon and ginger.", price = 3.40, category = "Tea"),
                Product(name = "Matcha Tea", description = "Traditional Japanese matcha green tea.", price = 3.60, category = "Tea"),
            )
            sampleProducts.forEach { productDao.insertProduct(it) }
        }
    }

    suspend fun clearProducts() {
        productDao.deleteAllProducts()
    }

    suspend fun deleteProductById(productId: Int) {
        productDao.deleteProductById(productId)
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProductById(product.id, product.name, product.description, product.price, product.category)
    }
}


//package com.example.coffee4n.repository
//
//import com.google.firebase.database.FirebaseDatabase
//import com.example.coffee4n.model.Product
//import com.example.coffee4n.model.database.ProductDao
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flow
//import kotlinx.coroutines.tasks.await
//
//class ProductRepository(
//    private val productDao: ProductDao,
//    private val firebaseDatabase: FirebaseDatabase
//) {
//    // Lấy tất cả sản phẩm từ local (RoomDB)
//    suspend fun getAllProductsFromLocal(): List<Product> {
//        return productDao.getAllProducts()
//    }
//
//    // Lấy tất cả sản phẩm từ remote (Firebase) và lưu vào local
//    suspend fun syncProductsFromRemote() {
//        val snapshot = firebaseDatabase.getReference("products").get().await()
//        val products = snapshot.children.mapNotNull { it.getValue(Product::class.java) }
//        products.forEach { product ->
//            productDao.insertProduct(product)
//        }
//    }
//
//    // Thêm sản phẩm mới (lưu vào local và đẩy lên Firebase)
//    suspend fun addProduct(product: Product) {
//        // Lưu vào local
//        productDao.insertProduct(product)
//        // Đẩy lên Firebase
//        firebaseDatabase.getReference("products").child(product.id.toString()).setValue(product).await()
//    }
//
//    // Lấy danh sách sản phẩm dưới dạng Flow để UI tự động cập nhật
//    fun getProductsFlow(): Flow<List<Product>> = flow {
//        emit(productDao.getAllProducts())
//        syncProductsFromRemote() // Đồng bộ từ remote
//        emit(productDao.getAllProducts()) // Cập nhật lại sau khi đồng bộ
//    }
//}