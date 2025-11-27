package com.example.pharmacy.core.data

import com.example.pharmacy.core.domain.cart.CartItem
import com.example.pharmacy.core.domain.cart.CartRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreCartRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CartRepository {

    private fun cartCol(userId: String) = db.collection("users").document(userId).collection("cart")
    private fun ordersCol(userId: String) = db.collection("users").document(userId).collection("orders")

    override fun streamCart(userId: String): Flow<List<CartItem>> = callbackFlow {
        val reg = cartCol(userId).addSnapshotListener { snap, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            val list = snap?.documents?.mapNotNull { d ->
                val medId = d.getString("medId") ?: return@mapNotNull null
                val price = d.getDouble("price") ?: d.getLong("price")?.toDouble() ?: 0.0
                val qty = d.getLong("quantity")?.toInt()
                    ?: d.getDouble("quantity")?.toInt() ?: 0
                CartItem(
                    medId = medId,
                    medName = d.getString("medName") ?: "",
                    price = price,
                    quantity = qty
                )
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    override suspend fun addItem(userId: String, item: CartItem) {
        val doc = cartCol(userId).document(item.medId)
        val existing = doc.get().await()
        val currentQty = existing.getLong("quantity")?.toInt()
            ?: existing.getDouble("quantity")?.toInt() ?: 0
        val merged = item.copy(quantity = currentQty + item.quantity)
        doc.set(itemMap(merged)).await()
    }

    override suspend fun updateItem(userId: String, item: CartItem) {
        cartCol(userId).document(item.medId).set(itemMap(item)).await()
    }

    override suspend fun removeItem(userId: String, medId: String) {
        cartCol(userId).document(medId).delete().await()
    }

    override suspend fun clearCart(userId: String) {
        val batch = db.batch()
        val docs = cartCol(userId).get().await()
        docs.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    override suspend fun placeOrder(userId: String, items: List<CartItem>) {
        val data = mapOf(
            "createdAt" to FieldValue.serverTimestamp(),
            "items" to items.map { itemMap(it) },
            "total" to items.sumOf { it.price * it.quantity }
        )
        ordersCol(userId).add(data).await()
    }

    private fun itemMap(item: CartItem) = mapOf(
        "medId" to item.medId,
        "medName" to item.medName,
        "price" to item.price,
        "quantity" to item.quantity
    )
}
