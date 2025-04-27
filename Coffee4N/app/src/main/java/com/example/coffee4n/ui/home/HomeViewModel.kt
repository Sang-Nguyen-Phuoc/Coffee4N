package com.example.coffee4n.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.repository.ProductRepository
import com.example.coffee4n.model.Product
import com.example.coffee4n.model.CartItem
import com.example.coffee4n.repository.CategoryRepository
import com.example.coffee4n.repository.CartRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val productRepository: ProductRepository
    private val categoryRepository: CategoryRepository
    private val cartRepository: CartRepository

    private val _state = MutableStateFlow(HomeState(isLoading = true))
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        productRepository = ProductRepository(firebaseDatabase)
        categoryRepository = CategoryRepository(firebaseDatabase)
        cartRepository = CartRepository(firebaseDatabase)

        loadProducts()
        loadCategories()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                productRepository.getProductsFlow().collect { products ->
                    _state.update {
                        it.copy(
                            products = products,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load products: ${e.message}"
                    )
                }
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                categoryRepository.getCategoriesFlow().collect { categories ->
                    _state.update {
                        it.copy(
                            categories = categories,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load categories: ${e.message}"
                    )
                }
            }
        }
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            try {
                val prefs = getApplication<Application>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val userId = prefs.getInt("userId", 0)

                if (userId > 0) {
                    // Check if product is in stock
                    if (product.stockQuantity <= 0) {
                        _state.update {
                            it.copy(
                                error = "Sorry, this product is out of stock",
                                showSnackbar = true
                            )
                        }
                        return@launch
                    }

                    // Create cart item with quantity 1
                    val cartItem = CartItem(
                        id = 0, // Will be auto-generated
                        userId = userId,
                        productId = product.id,
                        quantity = 1
                    )

                    cartRepository.addToCart(userId, cartItem)

                    _state.update {
                        it.copy(
                            showSnackbar = true,
                            snackbarMessage = "${product.name} added to cart"
                        )
                    }
                } else {
                    _state.update {
                        it.copy(showLoginDialog = true)
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Failed to add to cart: ${e.message}",
                        showSnackbar = true
                    )
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun updateCategory(category: Int) {
        _state.value = _state.value.copy(selectedCategory = category)
    }

    fun toggleSortOrder() {
        _state.value = _state.value.copy(sortAscending = !_state.value.sortAscending)
    }

    fun toggleBestSellers() {
        _state.value = _state.value.copy(showBestSellers = !_state.value.showBestSellers)
    }

    fun clearSnackbar() {
        _state.update { it.copy(showSnackbar = false, snackbarMessage = null, error = null) }
    }

    fun dismissLoginDialog() {
        _state.update { it.copy(showLoginDialog = false) }
    }
}