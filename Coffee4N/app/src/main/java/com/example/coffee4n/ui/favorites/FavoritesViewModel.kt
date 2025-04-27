package com.example.coffee4n.ui.favorites

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.model.Product
import com.example.coffee4n.repository.FavoritesRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {
    private val favoritesRepository: FavoritesRepository

    private val _state = MutableStateFlow(FavoritesState())
    val state: StateFlow<FavoritesState> = _state.asStateFlow()

    init {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        favoritesRepository = FavoritesRepository(firebaseDatabase)
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                val prefs = getApplication<Application>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val userId = prefs.getInt("userId", 0)

                if (userId > 0) {
                    _state.update { it.copy(isLoading = true) }
                    favoritesRepository.getFavoritesFlow(userId).collect { favorites ->
                        _state.update {
                            it.copy(
                                favorites = favorites,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                } else {
                    _state.update {
                        it.copy(showLoginDialog = true, isLoading = false)
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load favorites: ${e.message}"
                    )
                }
            }
        }
    }

    fun removeFromFavorites(product: Product) {
        viewModelScope.launch {
            try {
                val prefs = getApplication<Application>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val userId = prefs.getInt("userId", 0)

                if (userId > 0) {
                    favoritesRepository.removeFromFavorites(userId, product.id)
                    // The favorites list will be automatically updated through the Flow
                } else {
                    _state.update { it.copy(showLoginDialog = true) }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = "Failed to remove from favorites: ${e.message}")
                }
            }
        }
    }

    fun addToFavorites(product: Product) {
        viewModelScope.launch {
            try {
                val prefs = getApplication<Application>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val userId = prefs.getInt("userId", 0)

                if (userId > 0) {
                    favoritesRepository.addToFavorites(userId, product)
                    // The favorites list will be automatically updated through the Flow
                } else {
                    _state.update { it.copy(showLoginDialog = true) }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = "Failed to add to favorites: ${e.message}")
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun dismissLoginDialog() {
        _state.update { it.copy(showLoginDialog = false) }
    }
}

data class FavoritesState(
    val favorites: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showLoginDialog: Boolean = false
)