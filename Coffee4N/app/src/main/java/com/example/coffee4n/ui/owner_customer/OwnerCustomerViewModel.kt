package com.example.coffee4n.ui.owner_customer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.model.User
import com.example.coffee4n.repository.CustomerRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OwnerCustomerViewModel(application: Application) : AndroidViewModel(application) {
    private val customerRepository: CustomerRepository

    private val _state = MutableStateFlow(OwnerCustomerState(isLoading = true))
    val state: StateFlow<OwnerCustomerState> = _state.asStateFlow()

    init {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        customerRepository = CustomerRepository(firebaseDatabase)
        loadCustomers()
    }

    fun loadCustomers() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val customers = customerRepository.getCustomersFlow().collect { customers ->
                _state.update { it.copy(customers = customers, isLoading = false) }
            }
        }
    }

    fun updateSearchQuery(query: String) { _state.update { it.copy(searchQuery = query) } }
    fun onShowCustomerDetail(user: User) { _state.update { it.copy(showCustomerDetailDialog = true, selectedCustomer = user) }}
    fun onDismiss() { _state.update { it.copy(showCustomerDetailDialog = false) } }
}