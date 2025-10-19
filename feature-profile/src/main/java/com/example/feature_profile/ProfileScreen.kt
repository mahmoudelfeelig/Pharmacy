package com.example.feature_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core_data.FirestoreUserRepository
import com.example.core_domain.UserProfile
import com.example.core_ui.design.Spacing
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: ()->Unit) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val email = FirebaseAuth.getInstance().currentUser?.email.orEmpty()
    val repo = remember { FirestoreUserRepository() }

    var displayName by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    var extraNotes by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uid) {
        repo.get(uid).onSuccess { p ->
            displayName = p?.displayName.orEmpty()
            avatarUrl = p?.avatarUrl.orEmpty()
            extraNotes = p?.extraNotes.orEmpty()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(Spacing.lg).fillMaxWidth()) {

            // Avatar placeholder with initials
            val initial = (displayName.ifBlank { email }).firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            Box(
                modifier = Modifier
                    .size(88.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) { Text(initial, textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineMedium) }

            Spacer(Modifier.height(Spacing.lg))

            OutlinedTextField(displayName, { displayName = it }, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(Spacing.md))
            OutlinedTextField(avatarUrl, { avatarUrl = it }, label = { Text("Avatar URL") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(Spacing.md))
            OutlinedTextField(extraNotes, { extraNotes = it }, label = { Text("Extra Notes") }, minLines = 3, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(Spacing.lg))
            Button(
                onClick = {
                    scope.launch {
                        val p = UserProfile(uid = uid, email = email, displayName = displayName, avatarUrl = avatarUrl, extraNotes = extraNotes)
                        repo.upsert(p).onSuccess { msg = "Saved" }.onFailure { msg = it.message }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("Save") }

            msg?.let { Text(it, modifier = Modifier.padding(top = Spacing.md)) }
        }
    }
}
