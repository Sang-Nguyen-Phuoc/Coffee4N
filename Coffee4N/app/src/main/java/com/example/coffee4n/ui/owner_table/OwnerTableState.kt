package com.example.coffee4n.ui.owner_table

import com.example.coffee4n.model.BookingTable
import com.example.coffee4n.model.Table

data class OwnerTableState(
    val tables: List<Table> = emptyList(),
    val pendingBookings: Map<Int, Int> = emptyMap(), // Map từ tableId đến số lượng pending bookings
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddEditDialog: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val currentTable: Table? = null,
    val tableToDelete: Table? = null,
    val tableNumberInput: String = "",
    val capacityInput: String = "",
    val statusInput: String = "AVAILABLE",
    val imageUrlInput: String = "",
    val showBookingDialog: Boolean = false,
    val selectedTableBookings: List<BookingTable> = emptyList()
)