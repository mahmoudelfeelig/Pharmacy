package com.example.feature_home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.core_ui.design.Dimens
import com.example.core_ui.design.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onProfile: ()->Unit, onMap: ()->Unit, onLogout: ()->Unit) {
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Pharmacy") }) }) { pad ->
        Column(
            Modifier.padding(pad).padding(Spacing.lg).fillMaxWidth()
        ) {
            Button(onClick = onMap, modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeight)) {
                Text("Find pharmacies")
            }
            Spacer(Modifier.height(Spacing.md))
            OutlinedButton(onClick = onProfile, modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeight)) {
                Text("Profile")
            }
            Spacer(Modifier.height(Spacing.md))
            TextButton(onClick = onLogout) { Text("Logout") }
        }
    }
}
