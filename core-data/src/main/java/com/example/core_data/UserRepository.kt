package com.example.core_data

import com.example.core_domain.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.Result

class FirestoreUserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun col() = db.collection("users")

    suspend fun get(uid: String): Result<UserProfile?> = runCatching {
        val d = col().document(uid).get().await()
        if (!d.exists()) null else UserProfile(
            uid = uid,
            email = d.getString("email") ?: "",
            displayName = d.getString("displayName") ?: "",
            avatarUrl = d.getString("avatarUrl") ?: "",
            extraNotes = d.getString("extraNotes") ?: "",
            role = d.getString("role") ?: "",
            gender = d.getString("gender") ?: "",
            online = d.getBoolean("online") ?: false,
            sipExtension = d.getString("sipExtension") ?: ""
        )
    }

    suspend fun upsert(p: UserProfile): Result<Unit> = runCatching {
        val data = mapOf(
            "email" to p.email,
            "displayName" to p.displayName,
            "avatarUrl" to p.avatarUrl,
            "extraNotes" to p.extraNotes,
            "role" to p.role,
            "gender" to p.gender,
            "online" to p.online,
            "sipExtension" to p.sipExtension
        )
        col().document(p.uid).set(data).await()
    }

    fun streamOnlinePharmacists(): Flow<List<UserProfile>> = callbackFlow {
        val reg = col()
            .whereEqualTo("role", "pharmacist")
            .whereEqualTo("online", true)
            .addSnapshotListener { snap, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { d ->
                    val uid = d.id
                    val email = d.getString("email") ?: return@mapNotNull null
                    UserProfile(
                        uid = uid,
                        email = email,
                        displayName = d.getString("displayName"),
                        avatarUrl = d.getString("avatarUrl"),
                        extraNotes = d.getString("extraNotes"),
                        role = d.getString("role") ?: "",
                        gender = d.getString("gender") ?: "",
                        online = d.getBoolean("online") ?: false,
                        sipExtension = d.getString("sipExtension") ?: ""
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun setAvailability(uid: String, online: Boolean): Result<Unit> = runCatching {
        col().document(uid).set(mapOf("online" to online), SetOptions.merge()).await()
    }
}
