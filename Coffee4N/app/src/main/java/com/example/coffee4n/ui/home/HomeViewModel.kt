package com.example.coffee4n.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.App
import com.example.coffee4n.model.Product
import com.example.coffee4n.repository.ProductRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val productRepository: ProductRepository = ProductRepository(
        productDao = App.database.productDao(),
        firebaseDatabase = FirebaseDatabase.getInstance()
    )

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> get() = _state

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            productRepository.getProductsFlow().collectLatest { products ->
                _state.value = _state.value.copy(products = products)
            }
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            productRepository.addProduct(product)
        }
    }
}