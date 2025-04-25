package com.example.coffee4n.ui.owner_profile

import OwnerRepository
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.model.Owner
import com.example.coffee4n.session.OwnerSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OwnerProfileViewModel(application: Application): AndroidViewModel(application) {
    private val ownerRepository = OwnerRepository()

    private val _state = MutableStateFlow(OwnerProfileState(isLoading = true))
    val state: StateFlow<OwnerProfileState> = _state.asStateFlow()

    init {
        loadOwner()
    }

    fun loadOwner() {
        viewModelScope.launch {
            val ownerId = OwnerSession.ownerId

            ownerRepository.getOwner(ownerId).collect { owner ->
                _state.update { it.copy(
                    owner = owner ?: Owner(),
                    isLoading = false
                ) }
            }
        }
    }

    fun onShowEditDialog() {
        val owner = _state.value.owner
        _state.update { it.copy(
            email = owner.email,
            avatarUrl = owner.avatarUrl,
            shopName = owner.shopName,
            shopAddress = owner.shopAddress,
            shopPhone = owner.shopPhone,
            showEditDialog = true
        ) }
    }

    fun onSaveOwner() {
        val state = _state.value
        val updateOwner = Owner(
            ownerId = state.owner.ownerId,
            shopPhone = state.shopPhone,
            shopName = state.shopName,
            shopAddress = state.shopAddress,
            avatarUrl = state.avatarUrl
        )
        ownerRepository.updateOwner(updateOwner)
        onDismiss()
    }

    fun onDismiss() {
        _state.update { it.copy(showEditDialog = false) }
    }

    fun updateShopName(newName: String) {
        _state.update { it.copy(shopName = newName) }
    }

    fun updateShopAddress(newAddress: String) {
        _state.update { it.copy(shopAddress = newAddress) }
    }

    fun updateAvatarUrl(newUrl: String) {
        _state.update { it.copy(avatarUrl = newUrl) }
    }

    fun updateShopPhone(newPhone: String) {
        _state.update { it.copy(shopPhone = newPhone) }
    }
}