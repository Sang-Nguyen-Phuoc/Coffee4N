package com.example.coffee4n.ui.booking_table

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.model.BookingTable
import com.example.coffee4n.model.Table
import com.example.coffee4n.repository.TableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class BookingTableViewModel(
    private val tableRepository: TableRepository
) : ViewModel() {
    private val _state = MutableStateFlow(BookingTableState())
    val state: StateFlow<BookingTableState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _state.value = _state.value.copy(isLoading = true)

        // Kết hợp Flow của tables và bookingTables
        tableRepository.getTablesFlow()
            .combine(tableRepository.getBookingTablesFlow()) { tables, bookingTables ->
                Pair(tables, bookingTables)
            }
            .onEach { (tables, bookingTables) ->
                _state.value = _state.value.copy(
                    tables = tables,
                    bookingTables = bookingTables,
                    isLoading = false,
                    error = null
                )
            }
            .catch { e ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load data: ${e.message}"
                )
            }
            .launchIn(viewModelScope)
    }

    fun bookTable(tableId: Int, customerName: String, phoneNumber: String, numberOfPeople: Int, bookingTime: String, notes: String?) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                val bookingTable = BookingTable(
                    tableId = tableId,
                    customerName = customerName,
                    phoneNumber = phoneNumber,
                    numberOfPeople = numberOfPeople,
                    bookingTime = bookingTime,
                    notes = notes,
                    status = "PENDING"
                )
                tableRepository.addBookingTable(bookingTable)

                _state.value = _state.value.copy(
                    isLoading = false,
                    successMessage = "The system has received your request and will respond in a few minutes."
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to book table: ${e.message}"
                )
            }
        }
    }

    fun cancelBooking(bookingId: Int) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                tableRepository.deleteBookingTable(bookingId)
                _state.value = _state.value.copy(
                    isLoading = false,
                    successMessage = "Booking has been canceled successfully!"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to cancel booking: ${e.message}"
                )
            }
        }
    }

    fun editBooking(bookingId: Int, customerName: String, phoneNumber: String, numberOfPeople: Int, bookingTime: String, notes: String?) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                val updatedBooking = BookingTable(
                    id = bookingId,
                    tableId = _state.value.bookingTables.find { it.id == bookingId }?.tableId ?: return@launch,
                    customerName = customerName,
                    phoneNumber = phoneNumber,
                    numberOfPeople = numberOfPeople,
                    bookingTime = bookingTime,
                    notes = notes,
                    status = "PENDING" // Có thể giữ trạng thái cũ nếu cần
                )
                tableRepository.updateBookingTable(updatedBooking)
                _state.value = _state.value.copy(
                    isLoading = false,
                    successMessage = "Booking updated successfully!"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to update booking: ${e.message}"
                )
            }
        }
    }

    fun clearSuccessMessage() {
        _state.value = _state.value.copy(successMessage = null)
    }

    fun clearErrorMessage() {
        _state.value = _state.value.copy(error = null)
    }
}