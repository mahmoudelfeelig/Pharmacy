package com.example.feature_home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.core_data.FirestoreUserRepository
import com.example.core_domain.UserProfile
import com.example.core_ui.design.Spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacistConsultationScreen(
    userProfile: UserProfile,
    onAvailabilityChanged: (Boolean) -> Unit,
    onBack: () -> Unit,
    userRepo: FirestoreUserRepository = FirestoreUserRepository()
) {
    var online by remember { mutableStateOf(userProfile.online) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consultation status") },
                navigationIcon = { IconButton(onClick = onBack) { Text("Back") } }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier.padding(pad).padding(Spacing.lg).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Available for calls")
                    Text("Patients will see you when this is on.")
                }
                Switch(
                    checked = online,
                    onCheckedChange = { isOn ->
                        online = isOn
                        scope.launch {
                            userRepo.setAvailability(userProfile.uid, isOn)
                            onAvailabilityChanged(isOn)
                        }
                    }
                )
            }
        }
    }
}
