package com.example.feature_home

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onCallPharmacist: (String) -> Unit,
    userRepo: FirestoreUserRepository = FirestoreUserRepository(),
    sipDomainOrHost: String? = null
) {
    val online by userRepo.streamOnlinePharmacists().collectAsState(initial = emptyList())
    val context = LocalContext.current

    var micGranted by remember { mutableStateOf(false) }
    val micPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        micGranted = granted
    }

    LaunchedEffect(Unit) {
        micPermission.launch(Manifest.permission.RECORD_AUDIO)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consultation") },
                navigationIcon = { IconButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(Spacing.lg)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Available pharmacists: ${online.size}")
            Text("Signed in as ${userProfile.displayName ?: userProfile.email}")

            Button(
                onClick = {
                    if (!micGranted) {
                        micPermission.launch(Manifest.permission.RECORD_AUDIO)
                        return@Button
                    }

                    val target = online.firstOrNull()
                    val sipUser = target?.let { profile ->
                        val configured = profile.sipExtension.trim()
                        if (configured.isNotEmpty()) {
                            configured
                        } else {
                            profile.email.substringBefore("@").trim()
                        }
                    }.orEmpty()

                    if (target == null || sipUser.isBlank()) {
                        Toast.makeText(context, "No online pharmacist to call", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Build a valid SIP URI. If you know your PBX host or domain,
                    // pass it via sipDomainOrHost.
                    val host = sipDomainOrHost?.trim().orEmpty()
                    val sipAddress = if (host.isNotBlank()) {
                        "sip:$sipUser@$host"
                    } else {
                        "sip:$sipUser"
                    }

                    onCallPharmacist(sipAddress)
                    Toast.makeText(context, "Calling $sipUser", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text("Call pharmacist")
            }
        }
    }
}
