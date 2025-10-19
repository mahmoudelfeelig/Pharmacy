package com.example.pharmacy.feature.home
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable fun HomeScreen(onProfile:()->Unit, onMap:()->Unit, onLogout:()->Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
        Button(onClick = onMap, modifier = Modifier.fillMaxWidth()) { Text("Map") }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onProfile, modifier = Modifier.fillMaxWidth()) { Text("Profile") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
    }
}
