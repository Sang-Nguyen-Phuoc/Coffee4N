package com.example.coffee4n.navigation

object Destinations {
    const val HOME = "home"
    const val DETAIL = "detail/{itemId}"

    fun detailRoute(itemId: String) = "detail/$itemId"
}