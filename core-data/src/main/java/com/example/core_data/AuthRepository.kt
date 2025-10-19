package com.example.pharmacy.core.data
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<String>
    suspend fun register(email: String, password: String): Result<String>
    fun currentUserId(): String?
    fun signOut()
}
class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
): AuthRepository {
    override suspend fun signIn(e: String, p: String) = runCatching {
        auth.signInWithEmailAndPassword(e, p).await(); auth.currentUser!!.uid
    }
    override suspend fun register(e: String, p: String) = runCatching {
        auth.createUserWithEmailAndPassword(e, p).await(); auth.currentUser!!.uid
    }
    override fun currentUserId() = auth.currentUser?.uid
    override fun signOut() = auth.signOut()
}
