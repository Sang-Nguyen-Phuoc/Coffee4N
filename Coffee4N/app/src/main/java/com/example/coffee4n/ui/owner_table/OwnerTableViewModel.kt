package com.example.coffee4n.ui.owner_table

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.model.BookingTable
import com.example.coffee4n.model.Table
import com.example.coffee4n.repository.TableRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OwnerTableViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TableRepository
    private val _state = MutableStateFlow(OwnerTableState(isLoading = true))
    val state: StateFlow<OwnerTableState> = _state.asStateFlow()

    init {
        repository = TableRepository(
            firebaseDatabase = FirebaseDatabase.getInstance()
        )
        loadTablesAndBookings()
    }

    fun loadTablesAndBookings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val tablesFlow = repository.getTablesFlow()
                val bookingsFlow = repository.getBookingTablesFlow()

                tablesFlow.combine(bookingsFlow) { tables, bookings ->
                    val pendingBookingsMap = bookings
                        .filter { it.status == "PENDING" }
                        .groupBy { it.tableId }
                        .mapValues { it.value.size }
                    _state.update { it.copy(selectedTableBookings = bookings) } // Lưu tất cả bookings
                    Pair(tables, pendingBookingsMap)
                }.collect { (tables, pendingBookings) ->
                    _state.update {
                        it.copy(
                            tables = tables,
                            pendingBookings = pendingBookings,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load data: ${e.message}"
                    )
                }
            }
        }
    }

    // Mở dialog hiển thị booking của bàn
    fun openBookingDialog(table: Table) {
        val bookingsForTable = _state.value.selectedTableBookings.filter { it.tableId == table.id && it.status == "PENDING" }
        _state.update {
            it.copy(
                showBookingDialog = true,
                selectedTableBookings = bookingsForTable
            )
        }
    }

    // Đóng dialog booking
    fun closeBookingDialog() {
        _state.update { it.copy(showBookingDialog = false, selectedTableBookings = emptyList()) }
    }

    // Xác nhận booking
    fun confirmBooking(booking: BookingTable) {
        viewModelScope.launch {
            try {
                val updatedBooking = booking.copy(status = "CONFIRMED")
                repository.updateBookingTable(updatedBooking)
                // Cập nhật trạng thái bàn thành "BOOKED"
                val table = _state.value.tables.find { it.id == booking.tableId }
                table?.let {
                    repository.addTable(it.copy(status = "BOOKED"))
                }
                // Cập nhật lại danh sách booking trong dialog
                val updatedBookings = _state.value.selectedTableBookings.map {
                    if (it.id == booking.id) updatedBooking else it
                }
                _state.update { it.copy(selectedTableBookings = updatedBookings) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to confirm booking: ${e.message}") }
            }
        }
    }

    // Từ chối booking
    fun rejectBooking(booking: BookingTable) {
        viewModelScope.launch {
            try {
                val updatedBooking = booking.copy(status = "CANCELLED")
                repository.updateBookingTable(updatedBooking)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to reject booking: ${e.message}") }
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
                statusInput = "AVAILABLE",
                imageUrlInput = ""
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
                statusInput = table.status,
                imageUrlInput = table.imageUrl
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

    fun updateImageUrlInput(imageUrl: String) {
        _state.update { it.copy(imageUrlInput = imageUrl) }
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
                    val updatedTable = currentState.currentTable.copy(
                        tableNumber = currentState.tableNumberInput,
                        capacity = capacity,
                        status = currentState.statusInput,
                        imageUrl = currentState.imageUrlInput
                    )
                    repository.addTable(updatedTable)
                    _state.update { it.copy(showAddEditDialog = false) }
                } else {
                    val maxId = repository.getMaxTableId()
                    val newTable = Table(
                        id = maxId + 1,
                        tableNumber = currentState.tableNumberInput,
                        capacity = capacity,
                        status = currentState.statusInput,
                        imageUrl = currentState.imageUrlInput
                    )
                    repository.addTable(newTable)
                    _state.update { it.copy(showAddEditDialog = false) }
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