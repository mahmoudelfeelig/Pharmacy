package com.example.pharmacy.core.domain.cart

data class CartItem(
    val medId: String = "",
    val medName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
)
