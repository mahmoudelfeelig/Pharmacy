package com.example.feature_home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.core_data.FirestoreUserRepository
import com.example.core_domain.UserProfile
import com.example.core_ui.design.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientConsultationScreen(
    userProfile: UserProfile,
    onBack: () -> Unit,
    userRepo: FirestoreUserRepository = FirestoreUserRepository()
) {
    val online by userRepo.streamOnlinePharmacists().collectAsState(initial = emptyList())
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consultation") },
                navigationIcon = { IconButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier.padding(pad).padding(Spacing.lg).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Available pharmacists: ${online.size}")
            Text("Signed in as ${userProfile.displayName ?: userProfile.email}")
            Button(onClick = {
                Toast.makeText(context, "Call request sent (stub)", Toast.LENGTH_SHORT).show()
            }) {
                Text("Call pharmacist")
            }
        }
    }
}
