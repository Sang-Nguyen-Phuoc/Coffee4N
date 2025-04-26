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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
                    // Xóa các booking quá khứ
                    val currentTime = Calendar.getInstance().time
                    val formatter = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
                    val validBookings = bookings.filter { booking ->
                        try {
                            val bookingTime = formatter.parse(booking.bookingTime)
                            bookingTime != null && bookingTime.time >= currentTime.time - 30 * 60 * 1000
                        } catch (e: Exception) {
                            false
                        }
                    }

                    // Xóa các booking quá khứ từ Firebase
                    bookings.filter { it !in validBookings }.forEach { booking ->
                        booking.id?.let { bookingId ->
                            repository.deleteBookingTable(bookingId)
                        }
                    }

                    // Tính tổng số đơn đặt bàn (PENDING + CONFIRMED)
                    val bookingCount = validBookings
                        .filter { it.status in listOf("PENDING", "CONFIRMED") }
                        .groupBy { it.tableId }
                        .mapValues { it.value.size }

                    // Tính số lượng booking PENDING
                    val pendingBookingsMap = validBookings
                        .filter { it.status == "PENDING" }
                        .groupBy { it.tableId }
                        .mapValues { it.value.size }

                    // Cập nhật trạng thái bàn nếu không còn booking hợp lệ
                    val updatedTables = tables.map { table ->
                        val hasActiveBooking = validBookings.any { it.tableId == table.id && it.status == "CONFIRMED" }
                        if (!hasActiveBooking && table.status == "BOOKED") {
                            repository.addTable(table.copy(status = "AVAILABLE"))
                            table.copy(status = "AVAILABLE")
                        } else {
                            table
                        }
                    }

                    Pair(updatedTables, validBookings to Pair(pendingBookingsMap, bookingCount))
                }.collect { (tables, pair) ->
                    val (bookings, pairData) = pair
                    val (pendingBookings, bookingCount) = pairData
                    _state.update {
                        it.copy(
                            tables = tables,
                            allBookings = bookings, // Lưu tất cả bookings
                            pendingBookings = pendingBookings,
                            bookingCount = bookingCount,
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

    fun openBookingDialog(table: Table) {
        val bookingsForTable = _state.value.allBookings.filter { it.tableId == table.id }
        _state.update {
            it.copy(
                showBookingDialog = true,
                selectedTableBookings = bookingsForTable
            )
        }
    }

    fun closeBookingDialog() {
        _state.update { it.copy(showBookingDialog = false, selectedTableBookings = emptyList()) }
    }

    fun confirmBooking(booking: BookingTable) {
        viewModelScope.launch {
            try {
                val updatedBooking = booking.copy(status = "CONFIRMED")
                repository.updateBookingTable(updatedBooking)
                val table = _state.value.tables.find { it.id == booking.tableId }
                table?.let {
                    repository.addTable(it.copy(status = "BOOKED"))
                }
                _state.update { state ->
                    val updatedAllBookings = state.allBookings.map {
                        if (it.id == booking.id) updatedBooking else it
                    }
                    val updatedSelectedBookings = state.selectedTableBookings.map {
                        if (it.id == booking.id) updatedBooking else it
                    }
                    state.copy(
                        allBookings = updatedAllBookings,
                        selectedTableBookings = updatedSelectedBookings
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to confirm booking: ${e.message}") }
            }
        }
    }

    fun rejectBooking(booking: BookingTable) {
        viewModelScope.launch {
            try {
                val updatedBooking = booking.copy(status = "CANCELLED")
                repository.updateBookingTable(updatedBooking)
                _state.update { state ->
                    val updatedAllBookings = state.allBookings.map {
                        if (it.id == booking.id) updatedBooking else it
                    }
                    val updatedSelectedBookings = state.selectedTableBookings.map {
                        if (it.id == booking.id) updatedBooking else it
                    }
                    state.copy(
                        allBookings = updatedAllBookings,
                        selectedTableBookings = updatedSelectedBookings
                    )
                }
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
                    // When editing, keep the existing status
                    val updatedTable = currentState.currentTable.copy(
                        tableNumber = currentState.tableNumberInput,
                        capacity = capacity,
                        // Keep the original status instead of using statusInput
                        status = currentState.currentTable.status,
                        imageUrl = currentState.imageUrlInput
                    )
                    repository.addTable(updatedTable)
                    _state.update { it.copy(showAddEditDialog = false) }
                } else {
                    // For new tables, use "AVAILABLE" as default
                    val maxId = repository.getMaxTableId()
                    val newTable = Table(
                        id = maxId + 1,
                        tableNumber = currentState.tableNumberInput,
                        capacity = capacity,
                        status = "AVAILABLE", // Default status for new tables
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