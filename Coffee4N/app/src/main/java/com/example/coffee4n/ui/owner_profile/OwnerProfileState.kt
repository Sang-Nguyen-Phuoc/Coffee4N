package com.example.coffee4n.ui.owner_profile

import com.example.coffee4n.model.Owner

data class OwnerProfileState (
    val isLoading: Boolean = true,
    val owner: Owner = Owner(),
    val email: String = "",
    val shopName: String = "",
    val shopAddress: String = "",
    val avatarUrl: String = "",
    val shopPhone: String = "",

    val showEditDialog: Boolean = false
)