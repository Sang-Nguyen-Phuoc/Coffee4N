package com.example.coffee4n.navigation

object Destinations {
    const val BOOKING_TABLE = "booking_table"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val FAVORITES = "favorites"
    const val ORDERS = "orders"
    const val CART = "cart"
    const val NOTIFICATIONS = "notifications"
    const val CHECKOUT = "checkout"
    const val PROFILE = "profile"


    // Owner destinations
    const val OWNER_DASHBOARD = "owner_dashboard"
    const val OWNER_ORDERS = "owner_orders"
    const val OWNER_PRODUCTS = "owner_products"
    const val OWNER_INVENTORY = "owner_inventory"
    const val OWNER_EMPLOYEES = "owner_employees"
    const val OWNER_ANALYTICS = "owner_analytics"
    const val OWNER_TABLES = "owner_tables"

    // For creating navigation routes with parameters
    const val PRODUCT_DETAILS = "product_details/{productId}"
    fun productDetails(productId: Int) = "product_details/$productId"

}