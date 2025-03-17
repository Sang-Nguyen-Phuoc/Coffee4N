package com.example.coffee4n.ui.home

import com.example.coffee4n.model.Product

data class HomeState(
    val products: List<Product> = emptyList()
)