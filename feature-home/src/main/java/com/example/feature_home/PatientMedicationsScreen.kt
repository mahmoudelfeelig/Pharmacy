package com.example.feature_home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pharmacy.core.data.FirestoreCartRepository
import com.example.pharmacy.core.data.FirestoreMedicationRepository
import com.example.pharmacy.core.domain.cart.CartItem
import com.example.pharmacy.core.domain.medication.Medication
import com.example.core_ui.design.Spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientMedicationsScreen(
    userId: String,
    onCart: () -> Unit,
    onBack: () -> Unit,
    medRepo: FirestoreMedicationRepository = remember { FirestoreMedicationRepository() },
    cartRepo: FirestoreCartRepository = remember { FirestoreCartRepository() }
) {
    val meds by medRepo.streamAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val host = remember { SnackbarHostState() }
    var info by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(info) {
        info?.let { host.showSnackbar(it) }
        info = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medications") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    IconButton(onClick = onCart) { Text("Cart") }
                }
            )
        },
        snackbarHost = { SnackbarHost(host) }
    ) { pad ->
        if (meds.isEmpty()) {
            Column(
                Modifier.padding(pad).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) { Text("No medications found") }
            return@Scaffold
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            modifier = Modifier.padding(pad).padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            items(meds, key = { it.id }) { med ->
                MedicationCard(
                    med = med,
                    onAdd = {
                        scope.launch {
                            cartRepo.addItem(
                                userId,
                                CartItem(medId = med.id, medName = med.name, price = med.price, quantity = 1)
                            )
                            info = "Added ${med.name} to cart"
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MedicationCard(med: Medication, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(Modifier.padding(Spacing.md)) {
            Text(med.name, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            med.description?.takeIf { it.isNotBlank() }?.let {
                Text(it, maxLines = 2, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
            }
            Text("Price: ${med.price} EGP", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
            Text("Qty: ${med.quantity}", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(Spacing.md))
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("Add to cart") }
        }
    }
}
