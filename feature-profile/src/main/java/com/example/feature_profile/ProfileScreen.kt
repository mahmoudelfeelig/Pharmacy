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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: ()->Unit, providedProfile: UserProfile? = null) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val email = FirebaseAuth.getInstance().currentUser?.email.orEmpty()
    val repo = remember { FirestoreUserRepository() }

    var profile by remember { mutableStateOf(providedProfile) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (profile == null) {
            loading = true
            repo.get(uid).onSuccess { p ->
                profile = p ?: UserProfile(uid = uid, email = email)
            }
            loading = false
        }
    }

    val data = profile ?: UserProfile(uid = uid, email = email)
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
            val initial = (data.displayName.ifNullOrBlank { email }).firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            Box(
                modifier = Modifier
                    .size(88.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) { Text(initial, textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineMedium) }

            Spacer(Modifier.height(Spacing.lg))
            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            InfoRow(label = "Email", value = data.email)
            InfoRow(label = "Name", value = data.displayName ?: "-")
            InfoRow(label = "Role", value = data.role.ifBlank { "patient" })
            InfoRow(label = "Gender", value = data.gender.ifBlank { "-" })
            InfoRow(label = "Notes", value = data.extraNotes ?: "-")
            InfoRow(label = "Password", value = "•••••••")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(Spacing.sm))
    }
}

private inline fun String?.ifNullOrBlank(fallback: () -> String) =
    if (this.isNullOrBlank()) fallback() else this
