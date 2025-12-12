package com.example.feature_home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core_domain.UserProfile
import com.example.core_ui.design.Dimens
import com.example.core_ui.design.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHomeScreen(
    profile: UserProfile,
    onMedications: () -> Unit,
    onCart: () -> Unit,
    onConsultation: () -> Unit,
    onMap: () -> Unit,
    onProfile: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Patient Home") }) }) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(Spacing.lg)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            UserBanner(profile)
            ActionGrid(
                primaryActions = listOf(
                    "Medications" to onMedications,
                    "Cart" to onCart,
                    "Medical consultation" to onConsultation
                ),
                secondaryActions = listOf(
                    "Find pharmacies" to onMap,
                    "Profile" to onProfile
                )
            )
            TextButton(onClick = onLogout) { Text("Logout") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacistHomeScreen(
    profile: UserProfile,
    onManageMeds: () -> Unit,
    onConsultation: () -> Unit,
    onMap: () -> Unit,
    onProfile: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Pharmacist Home") }) }) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(Spacing.lg)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            UserBanner(profile)
            ActionGrid(
                primaryActions = listOf(
                    "Manage medications" to onManageMeds,
                    "Medical consultation" to onConsultation
                ),
                secondaryActions = listOf(
                    "Find pharmacies" to onMap,
                    "Profile" to onProfile
                )
            )
            TextButton(onClick = onLogout) { Text("Logout") }
        }
    }
}

@Composable
private fun UserBanner(profile: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(Spacing.md)) {
            Text(profile.displayName ?: profile.email, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text("Role: ${profile.role.ifBlank { "patient" }}", style = MaterialTheme.typography.bodyMedium)
            if (profile.gender.isNotBlank()) {
                Text("Gender: ${profile.gender}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ActionGrid(
    primaryActions: List<Pair<String, () -> Unit>>,
    secondaryActions: List<Pair<String, () -> Unit>>
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        primaryActions.forEach { (label, action) ->
            Button(
                onClick = action,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text(label) }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            secondaryActions.forEach { (label, action) ->
                OutlinedButton(
                    onClick = action,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.buttonHeight)
                ) { Text(label) }
            }
        }
    }
}
