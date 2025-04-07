package com.example.coffee4n.ui.checkout

import com.example.coffee4n.model.Promotion

data class CheckoutState(
    val finalTotal: Double = 0.0,
    val voucherCode: String = "",
    val appliedPromotion: Promotion? = null, // Đảm bảo kiểu là Promotion?
    val isApplyingVoucher: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showConfirmDialog: Boolean = false
)