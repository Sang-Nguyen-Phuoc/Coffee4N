package com.example.coffee4n.ui.product_details

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.model.CartItem
import com.example.coffee4n.model.Product
import com.example.coffee4n.repository.CartRepository
import com.example.coffee4n.repository.CategoryRepository
import com.example.coffee4n.repository.FavoritesRepository
import com.example.coffee4n.repository.ProductRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProductDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val productRepository: ProductRepository
    private val categoryRepository: CategoryRepository
    private val cartRepository: CartRepository
    private val favoritesRepository: FavoritesRepository

    private val _productState = MutableStateFlow(ProductDetailsState())
    val productState: StateFlow<ProductDetailsState> = _productState.asStateFlow()

    init {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        productRepository = ProductRepository(firebaseDatabase)
        categoryRepository = CategoryRepository(firebaseDatabase)
        cartRepository = CartRepository(firebaseDatabase)
        favoritesRepository = FavoritesRepository(firebaseDatabase)
    }

    fun loadProduct(productId: Int) {
        viewModelScope.launch {
            _productState.update { it.copy(isLoading = true) }

            try {
                val prefs = getApplication<Application>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val userId = prefs.getInt("userId", 0)

                // Get product details
                productRepository.getProductFlow(productId).collect { product ->
                    // Check if the product is in favorites
                    val isFavorite = if (userId > 0) {
                        favoritesRepository.isProductInFavorites(userId, productId)
                    } else {
                        false
                    }

                    _productState.update {
                        it.copy(
                            product = product,
                            isLoading = false,
                            error = null,
                            isFavorite = isFavorite
                        )
                    }
                }
            } catch (e: Exception) {
                _productState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An unknown error occurred"
                    )
                }
            }
        }
    }

    fun getCategoryName(categoryId: Int): String {
        return when (categoryId) {
            1 -> "Espresso"
            2 -> "Latte"
            3 -> "Cappuccino"
            4 -> "Mocha"
            5 -> "Tea"
            else -> "Coffee"
        }
    }

    fun addToCart(product: Product, quantity: Int) {
        viewModelScope.launch {
            try {
                val prefs = getApplication<Application>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val userId = prefs.getInt("userId", 0)

                if (userId > 0) {
                    val cartItem = CartItem(
                        id = 0, // Will be auto-generated
                        userId = userId,
                        productId = product.id,
                        quantity = quantity,
                        productName = product.name,
                        productPrice = product.price,
                        productImageUrl = product.imageUrl
                    )

                    cartRepository.addToCart(userId, cartItem)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            val prefs = getApplication<Application>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val userId = prefs.getInt("userId", 0)

            if (userId > 0) {
                try {
                    if (_productState.value.isFavorite) {
                        // Remove from favorites
                        favoritesRepository.removeFromFavorites(userId, product.id)
                    } else {
                        // Add to favorites
                        favoritesRepository.addToFavorites(userId, product)
                    }

                    _productState.update { it.copy(isFavorite = !it.isFavorite) }
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }
}

data class ProductDetailsState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFavorite: Boolean = false
)