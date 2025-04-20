package com.example.coffee4n.ui.owner_product

import com.example.coffee4n.model.Category
import com.example.coffee4n.model.Product

data class OwnerProductState (
//    Load & Filter products
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: Int = 0,
    val searchQuery: String = "",
    val sortAscending: Boolean = true,
    val showBestSellers: Boolean = false,

//    Show/Hide dialog
    val showAddEditDialog: Boolean = false,
    val showDeleteConfirmation: Boolean = false,

//    Input values for dialog
    val isEdit: Boolean = false,
    val selectedProduct: Product? = null,
    val nameInput: String = "",
    val descriptionInput: String = "",
    val priceInput: String = "",
    val isBestSellerInput: Boolean = false,
    val categoryIdInput: Int = 0,
    val stockQuantityInput: String = "",
    val costPriceInput: String = "",
    val imageUrl: String? = null,
)