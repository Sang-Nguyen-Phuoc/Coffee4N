package com.example.coffee4n.ui.owner_product

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.model.Product
import com.example.coffee4n.repository.CategoryRepository
import com.example.coffee4n.repository.ProductRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OwnerProductViewModel(application: Application) : AndroidViewModel(application) {
    private val productRepository: ProductRepository
    private val categoryRepository: CategoryRepository

    private val _state = MutableStateFlow(OwnerProductState(isLoading = true))
    val state: StateFlow<OwnerProductState> = _state.asStateFlow()

    init {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        productRepository = ProductRepository(firebaseDatabase)
        categoryRepository = CategoryRepository(firebaseDatabase)

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

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun updateCategory(category: Int) {
        _state.update { it.copy(selectedCategory = category) }
    }

    fun toggleSortOrder() {
        _state.update { it.copy(sortAscending = !it.sortAscending) }
    }

    fun toggleBestSellers() {
        _state.update { it.copy(showBestSellers = !it.showBestSellers) }
    }

    fun showAddDialog() {
        _state.update {
            it.copy(
                showAddEditDialog = true,
                isEdit = false,
                selectedProduct = null,
                nameInput = "",
                descriptionInput = "",
                priceInput = "",
                isBestSellerInput = false,
                categoryIdInput = 0,
                stockQuantityInput = "",
                costPriceInput = "",
                imageUrl = null
            )
        }
    }

    fun showEditDialog(product: Product) {
        _state.update {
            it.copy(
                showAddEditDialog = true,
                isEdit = true,
                selectedProduct = product,
                nameInput = product.name,
                descriptionInput = product.description,
                priceInput = product.price.toString(),
                isBestSellerInput = product.isBestSeller,
                categoryIdInput = product.categoryId,
                stockQuantityInput = product.stockQuantity.toString(),
                costPriceInput = product.costPrice.toString(),
                imageUrl = product.imageUrl
            )
        }
    }

    fun showDeleteConfirmation(product: Product) {
        _state.update {
            it.copy(
                showDeleteConfirmation = true,
                selectedProduct = product
            )
        }
    }

    fun hideDialog() {
        _state.update { it.copy(
            showAddEditDialog = false,
            showDeleteConfirmation = false,
            selectedProduct = null,
        ) }
    }

    fun updateNameInput(name: String) {
        _state.update { it.copy(nameInput = name) }
    }

    fun updateDescriptionInput(description: String) {
        _state.update { it.copy(descriptionInput = description) }
    }

    fun updatePriceInput(price: String) {
        _state.update { it.copy(priceInput = price) }
    }

    fun updateIsBestSellerInput(isBestSeller: Boolean) {
        _state.update { it.copy(isBestSellerInput = isBestSeller) }
    }

    fun updateCategoryIdInput(categoryId: Int) {
        _state.update { it.copy(categoryIdInput = categoryId) }
    }

    fun updateStockQuantityInput(stockQuantity: String) {
        _state.update { it.copy(stockQuantityInput = stockQuantity) }
    }

    fun updateCostPriceInput(costPrice: String) {
        _state.update { it.copy(costPriceInput = costPrice) }
    }

    fun updateImageUrl(imageUrl: String?) {
        _state.update { it.copy(imageUrl = imageUrl) }
    }

    fun saveProduct() {
        viewModelScope.launch {
            val state = _state.value
            val productId = if (state.isEdit) {
                state.selectedProduct?.id ?: 0 // Nếu chỉnh sửa, lấy id từ selectedProduct
            } else {
                0 // Nếu thêm mới, để repository tự tạo id
            }

            val product = Product(
                id = productId,
                name = state.nameInput,
                description = state.descriptionInput,
                price = state.priceInput.toDoubleOrNull() ?: 0.0,
                isBestSeller = state.isBestSellerInput,
                categoryId = state.categoryIdInput,
                stockQuantity = state.stockQuantityInput.toIntOrNull() ?: 0,
                costPrice = state.costPriceInput.toDoubleOrNull() ?: 0.0,
                imageUrl = state.imageUrl ?: ""
            )

            try {
                hideDialog()
                _state.update { it.copy(isLoading = true) }
                if (state.isEdit) {
                    productRepository.updateProduct(product)
                } else {
                    productRepository.addProduct(product)
                }
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to save product: ${e.message}", isLoading = false) }
            }
        }
    }

    fun confirmDeleteProduct() {
        viewModelScope.launch {
            val productToDelete = _state.value.selectedProduct
            productToDelete?.let { product ->
                try {
                    hideDialog()
                    _state.update { it.copy(isLoading = true) }
                    productRepository.deleteProduct(product.id)
                    _state.update { it.copy(isLoading = false) }
                } catch (e: Exception) {
                    _state.update { it.copy(error = "Failed to delete product: ${e.message}", isLoading = false) }
                }
            }
        }
    }
}