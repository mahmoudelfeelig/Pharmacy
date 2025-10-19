package com.example.pharmacy.core.data
import com.google.firebase.firestore.FirebaseFirestore
import com.example.pharmacy.core.domain.UserProfile
import kotlinx.coroutines.tasks.await

interface UserRepository {
    suspend fun get(uid: String): Result<UserProfile>
    suspend fun upsert(p: UserProfile): Result<Void?>
}
class FirestoreUserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
): UserRepository {
    private val col = db.collection("users")
    override suspend fun get(uid: String) = runCatching {
        col.document(uid).get().await().toObject(UserProfile::class.java)!!.copy(uid = uid)
    }
    override suspend fun upsert(p: UserProfile) = runCatching { col.document(p.uid).set(p).await() }
}
