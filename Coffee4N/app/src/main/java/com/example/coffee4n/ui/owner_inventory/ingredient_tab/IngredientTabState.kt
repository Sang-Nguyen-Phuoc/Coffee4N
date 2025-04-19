package com.example.coffee4n.ui.owner_inventory.ingredient_tab

import com.example.coffee4n.model.Ingredient
import com.example.coffee4n.model.TransactionType

data class IngredientTabState(
    val isLoading: Boolean = false,
    val ingredients: List<Ingredient> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: Int = 0, // 0: All, 1: Enough, 2: Low in stock, 3: Out of stock
    val filterList: List<String> = listOf("All", "Enough", "Low in stock", "Out of stock"),


    val isNew: Boolean = true,
    val showAddEditIngredientDialog: Boolean = false,
    val showConfirmDeleteIngredientDialog: Boolean = false,
    val nameInput: String = "",
    val unitInput: String = "",
    val thresholdInput: Int = 0,

    val showAddTransactionDialog: Boolean = false,
    val selectedIngredient: Ingredient = Ingredient(),
    val quantityInput: Int = 0,
    val unitPriceInput: Double = 0.0,
    val transactionType: TransactionType = TransactionType.IMPORT,
)