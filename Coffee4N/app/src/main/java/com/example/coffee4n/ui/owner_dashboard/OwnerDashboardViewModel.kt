package com.example.coffee4n.ui.owner_dashboard

import OwnerRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.repository.EmployeeRepository
import com.example.coffee4n.repository.IngredientRepository
import com.example.coffee4n.repository.OrderItemRepository
import com.example.coffee4n.repository.ProductRepository
import com.example.coffee4n.session.OwnerSession
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OwnerDashboardViewModel : ViewModel() {
    private val _state = MutableStateFlow(OwnerDashboardState())
    val state: StateFlow<OwnerDashboardState> = _state.asStateFlow()

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val orderItemRepository = OrderItemRepository(
        firebaseDatabase = firebaseDatabase
    )
    private val productRepository = ProductRepository(firebaseDatabase)
    private val employeeRepository = EmployeeRepository(firebaseDatabase)
    private val ingredientRepository = IngredientRepository(firebaseDatabase)
    private val ownerRepository = OwnerRepository()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        // Load ingredient information to check low stock ingredients
        viewModelScope.launch {
            ingredientRepository.getIngredientsFlow().collectLatest { ingredients ->
                val lowStockIngredients = ingredients.count { it.quantity <= it.threshold }
                _state.value = _state.value.copy(lowStockItems = lowStockIngredients)
            }
        }

        viewModelScope.launch {
            employeeRepository.getEmployeesFlow().collectLatest { employees ->
                _state.value = _state.value.copy(
                    employeesPresent = employees.size
                )
            }
        }

        viewModelScope.launch {
            val ownerId = OwnerSession.ownerId
            ownerRepository.getOwner(ownerId).collect { owner ->
                if (owner != null) {
                    _state.value = _state.value.copy(
                        avatarUrl = owner.avatarUrl
                    )
                }
            }

        }
        // Giá trị mặc định cho các phần còn lại
        _state.value = _state.value.copy(
            ordersCount = 3,
            dailyRevenue = 150.75,
            bookedTables = 1
        )
    }
}