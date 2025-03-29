package com.example.coffee4n.ui.owner_table

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.App
import com.example.coffee4n.model.Table
import com.example.coffee4n.repository.TableRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OwnerTableViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TableRepository
    private val _state = MutableStateFlow(OwnerTableState(isLoading = true))
    val state: StateFlow<OwnerTableState> = _state.asStateFlow()

    init {
        val app = application as App
        repository = TableRepository(
            tableDao = app.database.tableDao(),
            firebaseDatabase = FirebaseDatabase.getInstance()
        )
        loadTables()
    }

    fun loadTables() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                repository.getTablesFlow().collect { tables ->
                    _state.update {
                        it.copy(
                            tables = tables,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load tables: ${e.message}"
                    )
                }
            }
        }
    }

    fun openAddDialog() {
        _state.update {
            it.copy(
                showAddEditDialog = true,
                currentTable = null,
                tableNumberInput = "",
                capacityInput = "",
                statusInput = "AVAILABLE"
            )
        }
    }

    fun openEditDialog(table: Table) {
        _state.update {
            it.copy(
                showAddEditDialog = true,
                currentTable = table,
                tableNumberInput = table.tableNumber,
                capacityInput = table.capacity.toString(),
                statusInput = table.status
            )
        }
    }

    fun closeDialog() {
        _state.update { it.copy(showAddEditDialog = false, showDeleteConfirmation = false) }
    }

    fun updateTableNumberInput(tableNumber: String) {
        _state.update { it.copy(tableNumberInput = tableNumber) }
    }

    fun updateCapacityInput(capacity: String) {
        _state.update { it.copy(capacityInput = capacity) }
    }

    fun updateStatusInput(status: String) {
        _state.update { it.copy(statusInput = status) }
    }

    fun saveTable() {
        val currentState = _state.value
        val capacity = currentState.capacityInput.toIntOrNull() ?: 0

        if (currentState.tableNumberInput.isBlank() || capacity <= 0) {
            _state.update { it.copy(error = "Table number and capacity must be valid") }
            return
        }

        viewModelScope.launch {
            try {
                if (currentState.currentTable != null) {
                    // Update existing table
                    val updatedTable = currentState.currentTable.copy(
                        tableNumber = currentState.tableNumberInput,
                        capacity = capacity,
                        status = currentState.statusInput
                    )
                    repository.addTable(updatedTable)
                    _state.update {
                        it.copy(showAddEditDialog = false)
                    }
                } else {
                    // Add new table with a manually assigned ID
                    val maxId = repository.getMaxTableId() // Get the maximum ID
                    val newTable = Table(
                        id = maxId + 1, // Assign the next available ID
                        tableNumber = currentState.tableNumberInput,
                        capacity = capacity,
                        status = currentState.statusInput
                    )
                    repository.addTable(newTable)
                    _state.update {
                        it.copy(showAddEditDialog = false)
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to save table: ${e.message}") }
            }
        }
    }

    fun confirmDelete(table: Table) {
        _state.update {
            it.copy(
                showDeleteConfirmation = true,
                tableToDelete = table
            )
        }
    }

    fun deleteTable() {
        val currentState = _state.value
        val tableToDelete = currentState.tableToDelete ?: return

        viewModelScope.launch {
            try {
                repository.deleteTable(tableToDelete.id)
                _state.update {
                    it.copy(
                        showDeleteConfirmation = false,
                        tableToDelete = null
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to delete table: ${e.message}") }
            }
        }
    }
}