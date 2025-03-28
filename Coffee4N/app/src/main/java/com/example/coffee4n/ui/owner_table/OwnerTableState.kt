package com.example.coffee4n.ui.owner_table

import com.example.coffee4n.model.Table

data class OwnerTableState(
    val tables: List<Table> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddEditDialog: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val tableToDelete: Table? = null,
    val currentTable: Table? = null, // For editing
    val tableNumberInput: String = "",
    val capacityInput: String = "",
    val statusInput: String = "AVAILABLE" // Default status
)