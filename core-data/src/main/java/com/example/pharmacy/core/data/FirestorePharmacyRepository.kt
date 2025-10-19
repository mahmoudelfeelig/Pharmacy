package com.example.pharmacy.core.data

import com.example.pharmacy.core.domain.pharmacy.Pharmacy
import com.example.pharmacy.core.domain.pharmacy.PharmacyRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirestorePharmacyRepository : PharmacyRepository {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun streamAll(): Flow<List<Pharmacy>> = callbackFlow {
        val reg = db.collection("pharmacies").addSnapshotListener { snap, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            val list = snap?.documents?.mapNotNull { d ->
                val name = d.getString("name") ?: return@mapNotNull null
                val lat = d.getDouble("lat") ?: return@mapNotNull null
                val lon = d.getDouble("lon") ?: return@mapNotNull null
                Pharmacy(
                    id = d.id,
                    name = name,
                    lat = lat,
                    lon = lon,
                    address = d.getString("address"),
                    rating = d.getDouble("rating")
                )
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { reg.remove() }
    }
}
