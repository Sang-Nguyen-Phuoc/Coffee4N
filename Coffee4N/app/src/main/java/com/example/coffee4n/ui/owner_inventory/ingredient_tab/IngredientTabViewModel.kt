package com.example.coffee4n.ui.owner_inventory.ingredient_tab

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.model.Ingredient
import com.example.coffee4n.model.InventoryTransaction
import com.example.coffee4n.model.TransactionType
import com.example.coffee4n.repository.IngredientRepository
import com.example.coffee4n.repository.InventoryTransactionRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId

class IngredientTabViewModel(application: Application) : AndroidViewModel(application) {
    private val ingredientRepository : IngredientRepository
    private val inventoryTransactionRepository : InventoryTransactionRepository

    private val _state = MutableStateFlow(IngredientTabState(isLoading = true))
    val state: StateFlow<IngredientTabState> = _state.asStateFlow()

    init {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        ingredientRepository = IngredientRepository(firebaseDatabase)
        inventoryTransactionRepository = InventoryTransactionRepository(firebaseDatabase, ingredientRepository)
        loadIngredients()
    }

    fun loadIngredients() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val ingredients = ingredientRepository.getIngredientsFlow().collect { ingredients ->
                _state.update { it.copy(ingredients = ingredients, isLoading = false) }
            }

        }
    }

    fun onConfirmAddEditIngredient() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val state = _state.value

            if (state.isNew) {
                val newIngredient = Ingredient(
                    name = state.nameInput.trim(),
                    unit = state.unitInput.trim(),
                    quantity = 0,
                    threshold = state.thresholdInput,
                )
                try {
                    ingredientRepository.addIngredient(newIngredient)
                }
                catch (e: Exception) {
                    throw e
                }
            }
            else {
                val updateIngredient = state.selectedIngredient.copy(
                    name = state.nameInput.trim(),
                    unit = state.unitInput.trim(),
                    threshold = state.thresholdInput,
                )
                try {
                    ingredientRepository.updateIngredient(updateIngredient)
                }
                catch (e: Exception) {
                    throw e
                }
            }

            loadIngredients()
            _state.update { it.copy(isLoading = false, showAddEditIngredientDialog = false) }
        }
    }

    fun onConfirmDeleteIngredient() {
        viewModelScope.launch {
            val ingredientToDelete = _state.value.selectedIngredient
            ingredientToDelete?.let { ingredient ->
                try {
                    ingredientRepository.deleteIngredient(ingredient.id)
                    loadIngredients()
                    _state.update { it.copy(showConfirmDeleteIngredientDialog = false) }
                } catch (e: Exception) {
                    throw e
                }
            }
        }
    }

    fun onTransact() {
        viewModelScope.launch {
            val state = _state.value
            var newTransaction = InventoryTransaction(
                itemId = state.selectedIngredient.id,
                quantity = state.quantityInput,
                unitPrice = state.unitPriceInput,
                unit = state.selectedIngredient.unit,
                type = state.transactionType,
                timestamp = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            inventoryTransactionRepository.addTransaction(newTransaction)
            loadIngredients()
            _state.update { it.copy(showAddTransactionDialog = false) }
        }

    }

    fun showAddEditIngredientDialog(isNew: Boolean, ingredient: Ingredient = Ingredient()) {
        _state.update { it.copy(
            isNew = isNew,
            showAddEditIngredientDialog = true,
            selectedIngredient = ingredient,
            nameInput = ingredient.name,
            unitInput = ingredient.unit,
            thresholdInput = ingredient.threshold
        ) }
    }

    fun showConfirmDeleteIngredientDialog(ingredient: Ingredient) {
        _state.update { it.copy(
            selectedIngredient = ingredient,
            showConfirmDeleteIngredientDialog = true,
        ) }
    }

    fun showAddTransactionDialog(ingredient: Ingredient, type: TransactionType) {
        _state.update { it.copy(
            showAddTransactionDialog = true,
            selectedIngredient = ingredient,
            transactionType = type
        ) }
    }

    fun onDismiss() { _state.update { it.copy(
        showAddTransactionDialog = false,
        showAddEditIngredientDialog = false,
        showConfirmDeleteIngredientDialog = false )}
    }

    fun updateNameInput(name: String) { _state.update { it.copy(nameInput = name) } }
    fun updateUnitInput(unit: String) { _state.update { it.copy(unitInput = unit) } }
    fun updateThresholdInput(threshold: Int) { _state.update { it.copy(thresholdInput = threshold) } }
    fun updateQuantityInput(quantity: Int) { _state.update { it.copy(quantityInput = quantity) } }
    fun updateUnitPriceInput(price: Double) { _state.update { it.copy(unitPriceInput = price) } }
    fun updateSearchQuery(query: String) { _state.update { it.copy(searchQuery = query) } }
    fun updateSelectedFilter(filter: Int) { _state.update { it.copy(selectedFilter = filter) } }
}
