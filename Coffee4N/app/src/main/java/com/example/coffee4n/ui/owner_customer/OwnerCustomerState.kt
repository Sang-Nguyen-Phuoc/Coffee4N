package com.example.coffee4n.ui.owner_customer

import com.example.coffee4n.model.User

data class OwnerCustomerState (
    val isLoading: Boolean = false,
    val customers: List<User> = emptyList(),
    val searchQuery: String = "",
    val showCustomerDetailDialog: Boolean = false,
    val selectedCustomer: User = User()
)
