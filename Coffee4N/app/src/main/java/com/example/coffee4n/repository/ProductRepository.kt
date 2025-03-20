package com.example.coffee4n.repository

import com.google.firebase.database.FirebaseDatabase
import com.example.coffee4n.model.Product
import com.example.coffee4n.model.database.ProductDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ProductRepository(
    private val productDao: ProductDao,
    private val firebaseDatabase: FirebaseDatabase
) {
    // Lấy tất cả sản phẩm từ local (RoomDB)
    suspend fun getAllProductsFromLocal(): List<Product> {
        return productDao.getAllProducts()
    }

    // Lấy tất cả sản phẩm từ remote (Firebase) và lưu vào local
    suspend fun syncProductsFromRemote() {
        val snapshot = firebaseDatabase.getReference("products").get().await()
        val products = snapshot.children.mapNotNull { it.getValue(Product::class.java) }
        products.forEach { product ->
            productDao.insertProduct(product)
        }
    }

    // Thêm sản phẩm mới (lưu vào local và đẩy lên Firebase)
    suspend fun addProduct(product: Product) {
        // Lưu vào local
        productDao.insertProduct(product)
        // Đẩy lên Firebase
        firebaseDatabase.getReference("products").child(product.id.toString()).setValue(product).await()
    }

    // Lấy danh sách sản phẩm dưới dạng Flow để UI tự động cập nhật
    fun getProductsFlow(): Flow<List<Product>> = flow {
        emit(productDao.getAllProducts())
        syncProductsFromRemote() // Đồng bộ từ remote
        emit(productDao.getAllProducts()) // Cập nhật lại sau khi đồng bộ
    }
}