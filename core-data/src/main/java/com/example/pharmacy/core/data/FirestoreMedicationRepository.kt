package com.example.pharmacy.core.data

import com.example.pharmacy.core.domain.medication.Medication
import com.example.pharmacy.core.domain.medication.MedicationRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreMedicationRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : MedicationRepository {

    private fun col() = db.collection("medications")

    override fun streamAll(): Flow<List<Medication>> = callbackFlow {
        val reg = col().addSnapshotListener { snap, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            val list = snap?.documents?.mapNotNull { d ->
                val name = d.getString("name") ?: return@mapNotNull null
                val price = d.getDouble("price") ?: d.getLong("price")?.toDouble() ?: 0.0
                val qty = d.getLong("quantity")?.toInt()
                    ?: d.getDouble("quantity")?.toInt() ?: 0
                Medication(
                    id = d.id,
                    name = name,
                    price = price,
                    quantity = qty,
                    imageUrl = d.getString("imageUrl"),
                    description = d.getString("description"),
                )
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    override suspend fun add(med: Medication) {
        val doc = if (med.id.isBlank()) col().document() else col().document(med.id)
        doc.set(med.toMap(doc.id)).await()
    }

    override suspend fun update(med: Medication) {
        val id = med.id.ifBlank { col().document().id }
        col().document(id).set(med.toMap(id)).await()
    }

    override suspend fun delete(id: String) {
        col().document(id).delete().await()
    }

    private fun Medication.toMap(id: String = this.id) = mapOf(
        "id" to id,
        "name" to name,
        "price" to price,
        "quantity" to quantity,
        "imageUrl" to imageUrl,
        "description" to description,
    )
}
