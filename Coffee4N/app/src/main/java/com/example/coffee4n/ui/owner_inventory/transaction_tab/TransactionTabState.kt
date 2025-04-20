package com.example.coffee4n.ui.owner_inventory.transaction_tab

import com.example.coffee4n.model.InventoryTransaction

class TransactionDetail(
    val transaction: InventoryTransaction,
    val ingredientName: String
)

data class TransactionTabState (
    val isLoading: Boolean = false,
    val transactions: List<TransactionDetail> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: Int = 0,
    val filterList: List<String> = listOf("All", "Import", "Export"),
    val startDate: String = "",
    val endDate: String = "",
)
