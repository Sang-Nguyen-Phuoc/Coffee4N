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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    fun loadUser(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.getUserById(userId)?.let {
                    _user.value = it
                }
            } catch (e: Exception) {
                _error.value = "Failed to load user: ${e.message}"
            } finally {
                _isLoading.value = false
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