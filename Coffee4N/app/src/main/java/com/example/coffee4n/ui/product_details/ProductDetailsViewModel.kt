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
                    // Check if product exists
                    if (product == null) {
                        _productState.update { it.copy(
                            isLoading = false,
                            error = "Product not found"
                        )}
                        return@collect
                    }

                    // Check if the product is in favorites
                    val isFavorite = if (userId > 0) {
                        favoritesRepository.isProductInFavorites(userId, productId)
                    } else {
                        false
                    }

                    // Check if product is already in cart
                    if (userId > 0) {
                        val isInCart = cartRepository.isProductInCart(userId, productId)
                        _productState.update {
                            it.copy(
                                product = product,
                                isLoading = false,
                                error = null,
                                isFavorite = isFavorite,
                                isInCart = isInCart
                            )
                        }
                    } else {
                        _productState.update {
                            it.copy(
                                product = product,
                                isLoading = false,
                                error = null,
                                isFavorite = isFavorite
                            )
                        }
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
            _productState.update { it.copy(isAddingToCart = true) }

            try {
                val prefs = getApplication<Application>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val userId = prefs.getInt("userId", 0)

                if (userId > 0) {
                    // Validate stock availability
                    if (quantity > product.stockQuantity) {
                        _productState.update {
                            it.copy(
                                isAddingToCart = false,
                                error = "Sorry, only ${product.stockQuantity} items available.",
                                showSnackbar = true
                            )
                        }
                        return@launch
                    }

                    // Create a simplified CartItem with only necessary fields for Firebase
                    val cartItem = CartItem(
                        id = 0, // Will be auto-generated
                        userId = userId,
                        productId = product.id,
                        quantity = quantity
                    )

                    cartRepository.addToCart(userId, cartItem)

                    _productState.update {
                        it.copy(
                            isAddingToCart = false,
                            isInCart = true,
                            cartQuantity = quantity,
                            showSnackbar = true,
                            snackbarMessage = "${product.name} added to cart"
                        )
                    }
                } else {
                    _productState.update {
                        it.copy(
                            isAddingToCart = false,
                            showLoginDialog = true
                        )
                    }
                }
            } catch (e: Exception) {
                _productState.update {
                    it.copy(
                        isAddingToCart = false,
                        error = "Failed to add to cart: ${e.message}",
                        showSnackbar = true
                    )
                }
            }
        }
    }

    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            val prefs = getApplication<Application>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val userId = prefs.getInt("userId", 0)

            if (userId > 0) {
                try {
                    _productState.update { it.copy(isUpdatingFavorite = true) }

                    if (_productState.value.isFavorite) {
                        // Remove from favorites
                        favoritesRepository.removeFromFavorites(userId, product.id)
                        _productState.update {
                            it.copy(
                                isFavorite = false,
                                isUpdatingFavorite = false,
                                showSnackbar = true,
                                snackbarMessage = "Removed from favorites"
                            )
                        }
                    } else {
                        // Add to favorites
                        favoritesRepository.addToFavorites(userId, product)
                        _productState.update {
                            it.copy(
                                isFavorite = true,
                                isUpdatingFavorite = false,
                                showSnackbar = true,
                                snackbarMessage = "Added to favorites"
                            )
                        }
                    }
                } catch (e: Exception) {
                    _productState.update {
                        it.copy(
                            isUpdatingFavorite = false,
                            error = e.message,
                            showSnackbar = true
                        )
                    }
                }
            } else {
                _productState.update { it.copy(showLoginDialog = true) }
            }
        }
    }

    fun clearSnackbar() {
        _productState.update { it.copy(showSnackbar = false, snackbarMessage = null) }
    }

    fun dismissLoginDialog() {
        _productState.update { it.copy(showLoginDialog = false) }
    }
}

data class ProductDetailsState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val isAddingToCart: Boolean = false,
    val isUpdatingFavorite: Boolean = false,
    val error: String? = null,
    val isFavorite: Boolean = false,
    val isInCart: Boolean = false,
    val cartQuantity: Int = 0,
    val showSnackbar: Boolean = false,
    val snackbarMessage: String? = null,
    val showLoginDialog: Boolean = false
)