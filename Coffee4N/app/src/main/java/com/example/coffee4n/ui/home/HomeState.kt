package com.example.coffee4n.ui.home

import com.example.coffee4n.model.Category
import com.example.coffee4n.model.Product

data class HomeState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: Int = 0,
    val searchQuery: String = "",
    val sortAscending: Boolean = true,
    val showBestSellers: Boolean = false,
    val showSnackbar: Boolean = false,
    val snackbarMessage: String? = null,
    val showLoginDialog: Boolean = false
)