package com.example.coffee4n.ui.booking_table

import com.example.coffee4n.model.BookingTable
import com.example.coffee4n.model.Table

data class BookingTableState(
    val tables: List<Table> = emptyList(),
    val bookingTables: List<BookingTable> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)