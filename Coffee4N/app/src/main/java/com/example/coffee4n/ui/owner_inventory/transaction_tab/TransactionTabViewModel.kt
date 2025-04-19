package com.example.coffee4n.ui.owner_inventory.transaction_tab

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.model.Ingredient
import com.example.coffee4n.model.InventoryTransaction
import com.example.coffee4n.model.TransactionType
import com.example.coffee4n.repository.IngredientRepository
import com.example.coffee4n.repository.InventoryTransactionRepository
import com.example.coffee4n.ui.owner_inventory.ingredient_tab.IngredientTabState
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId

class TransactionTabViewModel(application: Application) : AndroidViewModel(application) {
    private val ingredientRepository : IngredientRepository
    private val inventoryTransactionRepository : InventoryTransactionRepository

    private val _state = MutableStateFlow(TransactionTabState(isLoading = true))
    val state: StateFlow<TransactionTabState> = _state.asStateFlow()

    init {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        ingredientRepository = IngredientRepository(firebaseDatabase)
        inventoryTransactionRepository = InventoryTransactionRepository(firebaseDatabase, ingredientRepository)
        loadTransactions()
    }

    fun loadTransactions() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val transactions = inventoryTransactionRepository.getTransactionsFlow().collect { transactions ->
                var transactionsDetail = transactions.map { transac ->
                    TransactionDetail(
                        transaction = transac,
                        ingredientName = getIngredientName(transac.itemId)
                    )
                }
                _state.update { it.copy(transactions = transactionsDetail, isLoading = false) }
            }

        }
    }

    suspend fun getIngredientName(ingredientId: Int): String {
        return try {
            val ingredient = ingredientRepository.getIngredient(ingredientId)
            ingredient!!.name
        } catch (e: Exception) {
            "Not found"
        }
    }

    fun updateSearchQuery(query: String) { _state.update { it.copy(searchQuery = query) } }
    fun updateSelectedFilter(filter: Int) { _state.update { it.copy(selectedFilter = filter) } }
    fun updateStartDate(date: String) { _state.update { it.copy(startDate = date) } }
    fun updateEndDate(date: String) { _state.update { it.copy(endDate = date) } }

}