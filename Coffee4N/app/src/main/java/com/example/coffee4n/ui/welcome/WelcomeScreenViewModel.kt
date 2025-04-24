package com.example.coffee4n.ui.welcome

import OwnerRepository
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.model.Owner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class WelcomeScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val ownerRepository = OwnerRepository()

    private val _owners = MutableStateFlow<List<Owner>>(emptyList())
    val owners: StateFlow<List<Owner>> = _owners.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchOwners()
    }

    private fun fetchOwners() {
        viewModelScope.launch {
            ownerRepository.getOwnersFlow().collect { ownersList ->
                    _isLoading.value = false
                    _owners.value = ownersList
                }
        }
    }
}