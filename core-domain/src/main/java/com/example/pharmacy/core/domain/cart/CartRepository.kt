package com.example.pharmacy.core.domain.cart

import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun streamCart(userId: String): Flow<List<CartItem>>
    suspend fun addItem(userId: String, item: CartItem)
    suspend fun updateItem(userId: String, item: CartItem)
    suspend fun removeItem(userId: String, medId: String)
    suspend fun clearCart(userId: String)
    suspend fun placeOrder(userId: String, items: List<CartItem>)
}
