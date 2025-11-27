package com.example.feature_home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.core_ui.design.Spacing
import com.example.pharmacy.core.data.FirestoreMedicationRepository
import com.example.pharmacy.core.domain.medication.Medication
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacistMedicationsScreen(
    onBack: () -> Unit,
    medRepo: FirestoreMedicationRepository = remember { FirestoreMedicationRepository() }
) {
    val meds by medRepo.streamAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val host = remember { SnackbarHostState() }

    var editing by remember { mutableStateOf<Medication?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    LaunchedEffect(editing) {
        showEditor = editing != null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage medications") },
                navigationIcon = { IconButton(onClick = onBack) { Text("Back") } }
            )
        },
        snackbarHost = { SnackbarHost(host) }
    ) { pad ->
        Column(
            modifier = Modifier.padding(pad).padding(Spacing.lg).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Button(onClick = { editing = Medication() }, modifier = Modifier.fillMaxWidth()) {
                Text("Add medication")
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                items(meds, key = { it.id }) { med ->
                    MedicationAdminCard(
                        med = med,
                        onEdit = { editing = med },
                        onDelete = {
                            scope.launch {
                                medRepo.delete(med.id)
                                host.showSnackbar("Deleted ${med.name}")
                            }
                        }
                    )
                }
            }
        }
    }

    if (showEditor && editing != null) {
        MedicationEditorDialog(
            initial = editing,
            onDismiss = { editing = null },
            onSave = { med ->
                scope.launch {
                    if (med.id.isBlank()) medRepo.add(med) else medRepo.update(med)
                    host.showSnackbar("Saved ${med.name}")
                }
                editing = null
            },
            onDelete = { id ->
                scope.launch { medRepo.delete(id); host.showSnackbar("Deleted item") }
                editing = null
            }
        )
    }
}

@Composable
private fun MedicationAdminCard(med: Medication, onEdit: () -> Unit, onDelete: () -> Unit) {
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
            Text("Price: ${med.price}", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            Text("Quantity: ${med.quantity}", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(Spacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Edit") }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun MedicationEditorDialog(
    initial: Medication?,
    onDismiss: () -> Unit,
    onSave: (Medication) -> Unit,
    onDelete: (String) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var priceText by remember { mutableStateOf(initial?.price?.toString().orEmpty()) }
    var quantityText by remember { mutableStateOf(initial?.quantity?.toString().orEmpty()) }
    var description by remember { mutableStateOf(initial?.description.orEmpty()) }
    var imageUrl by remember { mutableStateOf(initial?.imageUrl.orEmpty()) }
    var uploading by remember { mutableStateOf(false) }
    val storage = remember { FirebaseStorage.getInstance() }
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            uploading = true
            imageUrl = uploadImage(storage, uri) ?: imageUrl
            uploading = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial?.id.isNullOrBlank()) "Add medication" else "Edit medication") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    enabled = !uploading
                ) { Text(if (uploading) "Uploading..." else "Pick image") }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val med = Medication(
                        id = initial?.id.orEmpty(),
                        name = name,
                        price = priceText.toDoubleOrNull() ?: 0.0,
                        quantity = quantityText.toIntOrNull() ?: 0,
                        description = description,
                        imageUrl = imageUrl.takeIf { it.isNotBlank() }
                    )
                    onSave(med)
                },
                enabled = name.isNotBlank() && !uploading
            ) { Text("Save") }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                if (!initial?.id.isNullOrBlank()) {
                    TextButton(onClick = { onDelete(initial!!.id) }) { Text("Delete") }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

private suspend fun uploadImage(storage: FirebaseStorage, uri: Uri): String? = runCatching {
    val ref = storage.reference.child("medications/${UUID.randomUUID()}.jpg")
    ref.putFile(uri).await()
    ref.downloadUrl.await().toString()
}.getOrNull()
