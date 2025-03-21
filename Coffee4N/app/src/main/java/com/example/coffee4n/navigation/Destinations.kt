package com.example.coffee4n.navigation

object Destinations {
    const val HOME = "home"
    const val DETAIL = "detail/{itemId}"
    const val CART = "cart/{userId}"

    fun detailRoute(itemId: String) = "detail/$itemId"
}