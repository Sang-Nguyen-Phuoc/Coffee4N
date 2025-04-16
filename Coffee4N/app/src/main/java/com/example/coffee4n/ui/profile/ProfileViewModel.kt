package com.example.coffee4n.ui.profile

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.model.User
import com.example.coffee4n.model.database.UserDao
import com.example.coffee4n.repository.UserRepository
import com.example.coffee4n.ui.owner_product.OwnerProductState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    application: Application,
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) : AndroidViewModel(application) {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val userRepository = UserRepository(userDao, firebaseAuth, firebaseDatabase)

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    fun updateUser(user: User) {
        _state.update { it.copy(
            id = user.id,
            email = user.email,
            username =  user.username,
            name = user.name,
            phone = user.phone,
            address = user.address,
        ) }
    }

    fun showEditDialog() {
        _state.update { it.copy(showEditDialog = true) }
    }

    fun hideEditDialog() {
        _state.update { it.copy(showEditDialog = false) }
    }

    fun updateUsername(username: String) {
        _state.update { it.copy(username = username) }
    }

    fun updateName(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun updatePhone(phone: String) {
        _state.update { it.copy(phone = phone) }
    }

    fun updateAddress(address: String) {
        _state.update { it.copy(address = address) }
    }

    fun loadUser(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.getUserById(userId)?.let {
                    _user.value = it
                    updateUser(it)
                }
            } catch (e: Exception) {
                _error.value = "Failed to load user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveUser() {
        viewModelScope.launch {
            _isLoading.value = true
            val state = _state.value
            _user.update { it?.copy(
                username = state.username,
                name = state.name,
                phone = state.phone,
                address = state.address
            ) }
            try {
                userRepository.updateUser(_user.value ?: User())
                hideEditDialog()
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun logout(context: Context) {
        firebaseAuth.signOut()
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit()
            .remove("authToken")
            .remove("userId")
            .apply()
    }

    class Factory(
        private val application: Application,
        private val userDao: UserDao,
        private val firebaseAuth: FirebaseAuth,
        private val firebaseDatabase: FirebaseDatabase
    ) : ViewModelProvider.AndroidViewModelFactory(application) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(application, userDao, firebaseAuth, firebaseDatabase) as T
        }
    }
}