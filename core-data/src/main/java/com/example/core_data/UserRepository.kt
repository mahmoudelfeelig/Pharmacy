package com.example.core_data

import com.example.core_domain.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
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
            extraNotes = d.getString("extraNotes") ?: ""
        )
    }

    suspend fun upsert(p: UserProfile): Result<Unit> = runCatching {
        val data = mapOf(
            "email" to p.email,
            "displayName" to p.displayName,
            "avatarUrl" to p.avatarUrl,
            "extraNotes" to p.extraNotes
        )
        col().document(p.uid).set(data).await()
    }
}
