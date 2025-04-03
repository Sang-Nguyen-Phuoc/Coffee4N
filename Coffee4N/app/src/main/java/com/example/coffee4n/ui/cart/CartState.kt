package com.example.coffee4n.ui.cart

import com.example.coffee4n.model.Product

// Trạng thái của giỏ hàng
data class CartState(
    val cartItems: List<CartItem> = emptyList(), // Danh sách sản phẩm trong giỏ
    val itemTotal: Double = 0.0, // Tổng giá trị sản phẩm
    val tax: Double = 0.0, // Thuế
    val total: Double = 0.0, // Tổng cộng (itemTotal + tax + delivery)
    val isLoading: Boolean = false, // Trạng thái tải dữ liệu
    val error: String? = null, // Thông báo lỗi
    val successMessage: String? = null, // Thông báo thành công
    val outOfStockItems: List<Int> = emptyList() // Danh sách ID sản phẩm hết hàng
)

// Đối tượng đại diện cho một sản phẩm trong giỏ hàng
data class CartItem(
    val product: Product, // Thông tin sản phẩm
    val quantity: Int // Số lượng
)