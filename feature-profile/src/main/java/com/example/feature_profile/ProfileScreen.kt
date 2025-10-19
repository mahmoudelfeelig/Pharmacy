package com.example.pharmacy.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.example.pharmacy.core.data.FirestoreUserRepository
import com.example.pharmacy.core.domain.UserProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class) // add this
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val repo = remember { FirestoreUserRepository() }
    var displayName by remember { mutableStateOf("") }
    var extra by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    Scaffold(topBar = { TopAppBar(title = { Text("Profile") }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            OutlinedTextField(displayName, { displayName = it }, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(extra, { extra = it }, label = { Text("Extra notes") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                scope.launch {
                    val p = UserProfile(
                        uid = uid,
                        email = FirebaseAuth.getInstance().currentUser?.email ?: "",
                        displayName = displayName,
                        extraNotes = extra
                    )
                    repo.upsert(p).onSuccess { msg = "Saved" }.onFailure { msg = it.message }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Save") }
            msg?.let { Text(it) }
            TextButton(onClick = onBack) { Text("Back") }
        }
    }
}
