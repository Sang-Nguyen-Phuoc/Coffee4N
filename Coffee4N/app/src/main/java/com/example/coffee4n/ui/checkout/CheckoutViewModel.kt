package com.example.coffee4n.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.coffee4n.model.Order
import com.example.coffee4n.model.OrderItem
import com.example.coffee4n.model.Promotion
import com.example.coffee4n.repository.OrderItemRepository
import com.example.coffee4n.repository.OrderRepository
import com.example.coffee4n.repository.PromotionRepository
import com.example.coffee4n.ui.cart.CartViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.random.Random

class CheckoutViewModel(
    private val cartViewModel: CartViewModel,
    private val promotionRepository: PromotionRepository,
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val userId: Int
) : ViewModel() {

    private val _state = MutableStateFlow(CheckoutState(finalTotal = cartViewModel.state.value.total))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            cartViewModel.state.collect { cartState ->
                if (_state.value.appliedPromotion == null) {
                    _state.update { it.copy(finalTotal = cartState.total) }
                }
            }
        }
    }

    fun updateVoucherCode(code: String) {
        _state.update { it.copy(voucherCode = code) }
    }

    fun applyVoucher() {
        viewModelScope.launch {
            _state.update { it.copy(isApplyingVoucher = true) }
            try {
                val code = _state.value.voucherCode.trim()
                if (code.isEmpty()) {
                    _state.update {
                        it.copy(
                            errorMessage = "Vui lòng nhập mã voucher",
                            isApplyingVoucher = false
                        )
                    }
                    return@launch
                }
                val promotion = promotionRepository.getPromotionByCode(code)
                when {
                    promotion == null -> _state.update {
                        it.copy(
                            errorMessage = "Voucher không tồn tại",
                            isApplyingVoucher = false
                        )
                    }
                    !promotion.isValid() -> _state.update {
                        it.copy(
                            errorMessage = "Voucher đã hết hạn",
                            isApplyingVoucher = false
                        )
                    }
                    else -> {
                        val cartState = cartViewModel.state.value
                        val finalTotal = calculateFinalTotal(cartState.total, promotion)
                        _state.update {
                            it.copy(
                                appliedPromotion = promotion,
                                finalTotal = finalTotal,
                                errorMessage = null,
                                isApplyingVoucher = false,
                                successMessage = "Voucher ${promotion.code} áp dụng thành công!"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        errorMessage = "Lỗi khi áp dụng voucher: ${e.message}",
                        isApplyingVoucher = false
                    )
                }
            }
        }
    }

    fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }

    fun showConfirmDialog() {
        _state.update { it.copy(showConfirmDialog = true) }
    }

    fun hideConfirmDialog() {
        _state.update { it.copy(showConfirmDialog = false) }
    }

    fun checkout(deliveryMethod: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val cartState = cartViewModel.state.value
                if (cartState.cartItems.isEmpty()) {
                    _state.update {
                        it.copy(
                            errorMessage = "Giỏ hàng rỗng, không thể thanh toán",
                            showConfirmDialog = false
                        )
                    }
                    return@launch
                }

                val orderId = Random.nextInt(1000000)
                val order = Order(
                    id = orderId,
                    userId = userId,
                    orderDate = Date(),
                    totalAmount = _state.value.finalTotal,
                    status = "PENDING",
                    deliveryMethod = deliveryMethod
                )
                orderRepository.addOrder(order)

                cartState.cartItems.forEachIndexed { index, item ->
                    val orderItem = OrderItem(
                        id = index,
                        orderId = orderId,
                        productId = item.product.id,
                        quantity = item.cartItem.quantity,
                        price = item.product.price
                    )
                    orderItemRepository.addOrderItem(orderItem)
                }

                cartViewModel.clearCart()
                _state.update {
                    it.copy(
                        showConfirmDialog = false
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        errorMessage = "Lỗi khi thanh toán: ${e.message}",
                        showConfirmDialog = false
                    )
                }
            }
        }
    }

    private fun calculateFinalTotal(total: Double, promotion: Promotion?): Double {
        if (promotion == null) return total
        return when (promotion.discountType) {
            "PERCENTAGE" -> total * (1 - promotion.discountValue / 100)
            "FIXED" -> maxOf(total - promotion.discountValue, 0.0)
            else -> total
        }
    }
}

class CheckoutViewModelFactory(
    private val cartViewModel: CartViewModel,
    private val promotionRepository: PromotionRepository,
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val userId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
            return CheckoutViewModel(
                cartViewModel,
                promotionRepository,
                orderRepository,
                orderItemRepository,
                userId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}